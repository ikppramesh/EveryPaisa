package com.everypaisa.tracker.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainTabsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    /**
     * All transactions for the current period.
     */
    val allTransactions = transactionRepository.getTransactionsForPeriod(Period.currentMonth())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Map of currency code to count of transactions.
     */
    val currencyCounts: StateFlow<Map<String, Int>> = allTransactions
        .map { txns -> txns.groupingBy { it.currency.uppercase() }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Currencies found in ALL SMS (not just current month) that don't belong to any known country tab.
     * Drives the 🌐 "Other" tab so it persists across period changes.
     */
    val unmatchedCurrencies: StateFlow<Set<String>> =
        transactionRepository.getDistinctCurrencies()
            .map { currencies ->
                val known = tabCurrenciesSet().map { it.uppercase() }.toSet()
                currencies.map { it.uppercase() }.filter { it !in known }.toSet()
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /**
     * List of tabs to show, each with only the currencies that have transactions this month.
     * Falls back to all countryTabs when no transactions exist yet (avoids empty list crash).
     * The last tab is always "Other" (🌐) when any unmatched currency exists in the DB.
     */
    val visibleTabs: StateFlow<List<CountryTab>> =
        combine(currencyCounts, unmatchedCurrencies) { counts, unmatched ->
            val filtered = countryTabs.mapNotNull { tab ->
                val activeCurrencies = tab.currencies.map { it.uppercase() }.filter { counts.containsKey(it) }.toSet()
                if (activeCurrencies.isNotEmpty()) tab.copy(currencies = activeCurrencies) else null
            }
            // If no matching tabs found yet, show all tabs as default (empty-state)
            val tabs = if (filtered.isEmpty()) countryTabs else filtered
            if (unmatched.isNotEmpty()) {
                tabs + CountryTab(flag = "🌐", name = "Other", currencies = unmatched, bankHint = "")
            } else {
                tabs
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, countryTabs)

}
