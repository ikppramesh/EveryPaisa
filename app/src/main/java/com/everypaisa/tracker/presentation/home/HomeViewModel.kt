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
    
    // UI State reacts to period changes automatically
    val uiState: StateFlow<HomeUiState> = _selectedPeriod.flatMapLatest { period ->
        Log.d(TAG, "📅 Period changed: ${period.format()} (${period.type})")
        combine(
            transactionRepository.getTransactionsForPeriod(period),
            transactionRepository.getMonthSummary(period)
        ) { transactions, summary ->
            Log.d(TAG, "📊 UI State updated: ${transactions.size} txns for ${period.format()}")
            
            // Calculate multi-currency summary
            val multiCurrencySummary = calculateMultiCurrencySummary(transactions)
            
            HomeUiState.Success(
                transactions = transactions,
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
        _selectedPeriod.value = Period.forType(type)
        Log.d(TAG, "🔄 Switched to: ${type.label}")
    }
    
    fun goToPreviousPeriod() {
        _selectedPeriod.value = _selectedPeriod.value.previous()
    }
    
    fun goToNextPeriod() {
        val next = _selectedPeriod.value.next()
        if (!next.isFuture() || next.startDate <= java.time.LocalDate.now()) {
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
