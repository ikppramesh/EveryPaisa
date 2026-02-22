package com.everypaisa.tracker.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.CurrencySummary
import com.everypaisa.tracker.domain.model.Country
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

    // Current selected country
    private val _selectedCountry = MutableStateFlow(Country.INDIA)
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()

    // Current selected period (drives the dashboard)
    private val _selectedPeriod = MutableStateFlow(Period.currentMonth())
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    // Bank filter — null = show all, non-null = show only that bank
    private val _selectedBank = MutableStateFlow<String?>(null)
    val selectedBank: StateFlow<String?> = _selectedBank.asStateFlow()

    // ATM-only filter
    private val _showAtmOnly = MutableStateFlow(false)
    val showAtmOnly: StateFlow<Boolean> = _showAtmOnly.asStateFlow()

    fun setSelectedCountry(country: Country) {
        _selectedCountry.value = country
        _selectedBank.value = null
        _showAtmOnly.value = false
    }

    fun setSelectedBank(bank: String?) {
        _selectedBank.value = bank
        _showAtmOnly.value = false  // clear ATM filter when switching bank
    }

    fun setShowAtmOnly(enabled: Boolean) {
        _showAtmOnly.value = enabled
        if (enabled) _selectedBank.value = null  // clear bank filter when ATM filter is on
    }

    // Raw transactions (unfiltered by bank, but filtered by country)
    private val _rawTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    // UI State reacts to period & country changes automatically
    val uiState: StateFlow<HomeUiState> = combine(_selectedPeriod, _selectedCountry)
        { period, country -> Pair(period, country) }
        .flatMapLatest { (period, country) ->
            Log.d(TAG, "📅 Period changed: ${period.format()} (${period.type}), Country: ${country.label}")
            combine(
                transactionRepository.getTransactionsForPeriod(period),
                transactionRepository.getMonthSummary(period)
            ) { allTransactions, summary ->
                // Filter transactions by country's supported currencies
                val countryTransactions = allTransactions.filter { txn ->
                    txn.currency.uppercase() in country.supportedCurrencies.map { it.uppercase() }
                }
                // Deduplicate: keep only one per (amount, bankName, dateTime-minute)
                val deduplicated = countryTransactions
                    .groupBy { Triple(it.amount, it.bankName, it.dateTime.withSecond(0).withNano(0)) }
                    .map { (_, group) -> group.first() }
                    .sortedByDescending { it.dateTime }

                Log.d(TAG, "🌍 ${country.label} Transactions: ${deduplicated.size}/${allTransactions.size}")

                _rawTransactions.value = deduplicated

                val multiCurrencySummary = calculateMultiCurrencySummary(deduplicated, country)

                HomeUiState.Success(
                    transactions = deduplicated,
                    monthSummary = summary,
                    currentPeriod = period,
                    multiCurrencySummary = multiCurrencySummary,
                    selectedCountry = country
                ) as HomeUiState
            }
        }
    .catch { e ->
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

    /** Bank-filtered transactions for summary cards (ATM view filter does NOT affect totals) */
    /** Transactions after applying bank + ATM-only filters (for both transaction list and summary tiles) */
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(_rawTransactions, _selectedBank, _showAtmOnly) { txns, bank, atmOnly ->
            val bankFiltered = if (bank == null) txns else txns.filter { it.bankName == bank }
            if (atmOnly) bankFiltered.filter { it.isAtmWithdrawal } else bankFiltered
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Summary calculated from filteredTransactions — ATM filter now affects totals */
    val filteredSummary: StateFlow<MultiCurrencySummary> = combine(
        filteredTransactions, _selectedCountry
    ) { txns, country ->
        if (txns.isEmpty()) {
            MultiCurrencySummary(null, emptyList())
        } else {
            calculateMultiCurrencySummary(txns, country)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MultiCurrencySummary(null, emptyList()))

    /** Available countries */
    val availableCountries: StateFlow<List<Country>> = MutableStateFlow(Country.values().toList())
        .stateIn(viewModelScope, SharingStarted.Eagerly, Country.values().toList())

    private fun calculateMultiCurrencySummary(transactions: List<TransactionEntity>, country: Country): MultiCurrencySummary {
        // Group transactions by currency
        val byCurrency = transactions.groupBy { it.currency }
        val atmFilterActive = showAtmOnly.value

        val currencySummaries = byCurrency.map { (currency, txns) ->
            val income = txns
                .filter { !it.isInterAccountTransfer }
                .filter { it.transactionType == TransactionType.INCOME || it.transactionType == TransactionType.CREDIT }
                .sumOf { it.amount }
            val expenses = txns
                .filter {
                    if (atmFilterActive) true else !it.isAtmWithdrawal && !it.isInterAccountTransfer
                }
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

        // Use country's primary currency as main summary
        val primarySummary = currencySummaries.firstOrNull { it.currency.uppercase() == country.primaryCurrency }
        val internationalSummaries = currencySummaries.filter { it.currency.uppercase() != country.primaryCurrency }

        return MultiCurrencySummary(
            inrSummary = primarySummary,
            internationalSummaries = internationalSummaries
        )
    }
    
    fun selectPeriodType(type: DashboardPeriod) {
        _selectedBank.value = null
        _showAtmOnly.value = false
        _selectedPeriod.value = Period.forType(type)
        Log.d(TAG, "🔄 Switched to: ${type.label}")
    }

    fun goToPreviousPeriod() {
        _selectedBank.value = null
        _showAtmOnly.value = false
        _selectedPeriod.value = _selectedPeriod.value.previous()
    }

    fun goToNextPeriod() {
        val next = _selectedPeriod.value.next()
        if (!next.isFuture() || next.startDate <= java.time.LocalDate.now()) {
            _selectedBank.value = null
            _showAtmOnly.value = false
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

    fun markTransactionAsAtm(id: Long, flag: Boolean) {
        viewModelScope.launch {
            transactionRepository.markAsAtmWithdrawal(id, flag)
        }
    }

    fun markTransactionAsInterAccount(id: Long, flag: Boolean) {
        viewModelScope.launch {
            transactionRepository.markAsInterAccountTransfer(id, flag)
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val transactions: List<TransactionEntity>,
        val monthSummary: MonthSummary,
        val currentPeriod: Period,
        val multiCurrencySummary: MultiCurrencySummary,
        val selectedCountry: Country
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

