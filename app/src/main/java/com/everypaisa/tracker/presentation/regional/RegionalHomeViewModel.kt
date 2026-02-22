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
import kotlinx.coroutines.launch
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
    private val _selectedBank = MutableStateFlow<String?>(null)
    val selectedBank: StateFlow<String?> = _selectedBank.asStateFlow()

    fun setCurrencies(currencies: Set<String>) {
        if (_currencies.value != currencies) {
            _currencies.value = currencies
            Log.d(TAG, "💱 Currencies set: $currencies")
        }
    }

    // Raw transactions (unfiltered by bank, but filtered by currency)
    private val _rawTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    /** Distinct bank names for the filter chip row */
    val availableBanks: StateFlow<List<String>> = _rawTransactions
        .map { txns -> txns.mapNotNull { it.bankName }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Transactions after applying the selected bank filter */
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(_rawTransactions, _selectedBank) { txns, bank ->
            if (bank == null) txns else txns.filter { it.bankName == bank }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Summary calculated from filtered transactions (respects bank filter) */
    val filteredSummary: StateFlow<MultiCurrencySummary> = combine(
        filteredTransactions, _currencies
    ) { txns, currencies ->
        if (currencies.isEmpty() || txns.isEmpty()) {
            MultiCurrencySummary(null, emptyList())
        } else {
            calculateSummary(txns, currencies)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MultiCurrencySummary(null, emptyList()))

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
                // Update raw transactions for bank filtering
                _rawTransactions.value = filtered
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
                .filter { !it.isInterAccountTransfer }
                .filter { it.transactionType == TransactionType.INCOME || it.transactionType == TransactionType.CREDIT }
                .sumOf { it.amount }
            val expenses = txns
                .filter { !it.isAtmWithdrawal && !it.isInterAccountTransfer }
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
        _selectedBank.value = null  // reset bank filter on period change
        _selectedPeriod.value = _selectedPeriod.value.previous()
    }

    fun goToNextPeriod() {
        _selectedBank.value = null  // reset bank filter on period change
        _selectedPeriod.value = _selectedPeriod.value.next()
    }

    fun selectPeriodType(type: DashboardPeriod) {
        _selectedBank.value = null  // reset bank filter on period change
        _selectedPeriod.value = Period.forType(type)
    }

    fun setSelectedBank(bank: String?) {
        _selectedBank.value = bank
    }

    fun markTransactionAsAtm(id: Long, flag: Boolean) {
        viewModelScope.launch { transactionRepository.markAsAtmWithdrawal(id, flag) }
    }

    fun markTransactionAsInterAccount(id: Long, flag: Boolean) {
        viewModelScope.launch { transactionRepository.markAsInterAccountTransfer(id, flag) }
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
