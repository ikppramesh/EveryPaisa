package com.everypaisa.tracker.presentation.regional

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.*
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegionalHomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    companion object {
        private const val TAG = "RegionalHomeViewModel"
    }

    // Currencies are set by the screen after creation (keyed per region)
    private val _currencies = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedPeriod = MutableStateFlow(Period.currentMonth())

    fun setCurrencies(currencies: Set<String>) {
        if (_currencies.value != currencies) {
            _currencies.value = currencies
            Log.d(TAG, "💱 Currencies set: $currencies")
        }
    }

    val uiState: StateFlow<RegionalHomeUiState> = combine(
        _currencies, _selectedPeriod
    ) { currencies, period -> currencies to period }
        .flatMapLatest { (currencies, period) ->
            if (currencies.isEmpty()) {
                return@flatMapLatest flowOf(RegionalHomeUiState.Loading)
            }
            combine(
                transactionRepository.getTransactionsForPeriod(period),
                transactionRepository.getMonthSummary(period)
            ) { allTransactions, _ ->
                val filtered = allTransactions.filter { txn ->
                    txn.currency.uppercase() in currencies
                }
                Log.d(TAG, "💱 [${currencies.joinToString(",")}] ${filtered.size}/${allTransactions.size} transactions")
                val summary = calculateSummary(filtered, currencies)
                RegionalHomeUiState.Success(
                    transactions = filtered,
                    currentPeriod = period,
                    multiCurrencySummary = summary
                ) as RegionalHomeUiState
            }
        }
        .catch { e ->
            Log.e(TAG, "❌ Error: ${e.message}", e)
            emit(RegionalHomeUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RegionalHomeUiState.Loading)

    private fun calculateSummary(
        transactions: List<TransactionEntity>,
        currencies: Set<String>
    ): MultiCurrencySummary {
        val byCurrency = transactions.groupBy { it.currency }
        val summaries = byCurrency.map { (currency, txns) ->
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
        // The first currency in the set is treated as "primary" for the summary layout
        val primaryCurrency = currencies.first()
        val primarySummary = summaries.firstOrNull { it.currency.uppercase() == primaryCurrency.uppercase() }
        val others = summaries.filter { it.currency.uppercase() != primaryCurrency.uppercase() }
        return MultiCurrencySummary(
            inrSummary = primarySummary,
            internationalSummaries = others
        )
    }

    fun goToPreviousPeriod() {
        _selectedPeriod.value = _selectedPeriod.value.previous()
    }

    fun goToNextPeriod() {
        _selectedPeriod.value = _selectedPeriod.value.next()
    }

    fun selectPeriodType(type: DashboardPeriod) {
        _selectedPeriod.value = Period.forType(type)
    }
}

sealed class RegionalHomeUiState {
    object Loading : RegionalHomeUiState()
    data class Success(
        val transactions: List<TransactionEntity>,
        val currentPeriod: Period,
        val multiCurrencySummary: MultiCurrencySummary
    ) : RegionalHomeUiState()
    data class Error(val message: String) : RegionalHomeUiState()
}
