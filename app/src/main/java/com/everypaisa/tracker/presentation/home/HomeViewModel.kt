package com.everypaisa.tracker.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.CurrencySummary
import com.everypaisa.tracker.domain.model.DashboardPeriod
import com.everypaisa.tracker.domain.model.MonthSummary
import com.everypaisa.tracker.domain.model.MultiCurrencySummary
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val TAG = "HomeViewModel"

    // Current selected period (drives the dashboard)
    private val _selectedPeriod = MutableStateFlow(Period.currentMonth())
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    // Bank filter — null = show all, non-null = show only that bank
    private val _selectedBank = MutableStateFlow<String?>(null)
    val selectedBank: StateFlow<String?> = _selectedBank.asStateFlow()

    fun setSelectedBank(bank: String?) {
        _selectedBank.value = bank
    }

    // Raw INR transactions (unfiltered by bank)
    private val _rawTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    // UI State reacts to period changes automatically
    val uiState: StateFlow<HomeUiState> = _selectedPeriod.flatMapLatest { period ->
        Log.d(TAG, "📅 Period changed: ${period.format()} (${period.type})")
        combine(
            transactionRepository.getTransactionsForPeriod(period),
            transactionRepository.getMonthSummary(period)
        ) { allTransactions, summary ->
            // Filter only INR transactions for Indian home screen
            val inrTransactions = allTransactions.filter { txn ->
                txn.currency.uppercase() == "INR"
            }
            // Deduplicate: keep only one per (amount, bankName, dateTime-minute)
            val deduplicated = inrTransactions
                .groupBy { Triple(it.amount, it.bankName, it.dateTime.withSecond(0).withNano(0)) }
                .map { (_, group) -> group.first() }
                .sortedByDescending { it.dateTime }

            Log.d(TAG, "🇮🇳 Indian Transactions: ${deduplicated.size}/${allTransactions.size}")

            _rawTransactions.value = deduplicated

            val multiCurrencySummary = calculateMultiCurrencySummary(deduplicated)

            HomeUiState.Success(
                transactions = deduplicated,
                monthSummary = summary,
                currentPeriod = period,
                multiCurrencySummary = multiCurrencySummary
            ) as HomeUiState
        }
    }.catch { e ->
        Log.e(TAG, "❌ Error in UI state: ${e.message}", e)
        emit(HomeUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeUiState.Loading
    )

    /** Distinct bank names for the filter chip row */
    val availableBanks: StateFlow<List<String>> = _rawTransactions
        .map { txns -> txns.mapNotNull { it.bankName }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Transactions after applying the selected bank filter */
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(_rawTransactions, _selectedBank) { txns, bank ->
            if (bank == null) txns else txns.filter { it.bankName == bank }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun calculateMultiCurrencySummary(transactions: List<TransactionEntity>): MultiCurrencySummary {
        // Group transactions by currency
        val byCurrency = transactions.groupBy { it.currency }
        
        val currencySummaries = byCurrency.map { (currency, txns) ->
            val income = txns
                .filter { it.transactionType == TransactionType.INCOME || it.transactionType == TransactionType.CREDIT }
                .sumOf { it.amount }
            val expenses = txns
                .filter { it.transactionType == TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            CurrencySummary(
                currency = currency,
                currencySymbol = CurrencySummary.getCurrencySymbol(currency),
                totalIncome = income,
                totalExpenses = expenses,
                transactionCount = txns.size
            )
        }
        
        // Separate INR from international
        val inrSummary = currencySummaries.firstOrNull { it.currency.uppercase() == "INR" }
        val internationalSummaries = currencySummaries.filter { it.currency.uppercase() != "INR" }
        
        return MultiCurrencySummary(
            inrSummary = inrSummary,
            internationalSummaries = internationalSummaries
        )
    }
    
    fun selectPeriodType(type: DashboardPeriod) {
        _selectedBank.value = null   // reset bank filter on period change
        _selectedPeriod.value = Period.forType(type)
        Log.d(TAG, "🔄 Switched to: ${type.label}")
    }

    fun goToPreviousPeriod() {
        _selectedBank.value = null   // reset bank filter on period change
        _selectedPeriod.value = _selectedPeriod.value.previous()
    }

    fun goToNextPeriod() {
        val next = _selectedPeriod.value.next()
        if (!next.isFuture() || next.startDate <= java.time.LocalDate.now()) {
            _selectedBank.value = null   // reset bank filter on period change
            _selectedPeriod.value = next
        }
    }
    
    fun refreshTransactions() {
        Log.d(TAG, "🔄 Refresh requested (Room auto-updates)")
    }
    
    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val transactions: List<TransactionEntity>,
        val monthSummary: MonthSummary,
        val currentPeriod: Period,
        val multiCurrencySummary: MultiCurrencySummary
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
