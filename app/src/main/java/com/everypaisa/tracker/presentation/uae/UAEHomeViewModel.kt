package com.everypaisa.tracker.presentation.uae

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.*
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class UAEHomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "UAEHomeViewModel"
        // UAE/GCC currencies
        private val UAE_CURRENCIES = setOf("AED", "SAR", "QAR", "OMR", "KWD", "BHD")
    }
    
    private val _selectedPeriod = MutableStateFlow(Period.forType(DashboardPeriod.DAILY))
    
    val uiState: StateFlow<UAEHomeUiState> = _selectedPeriod.flatMapLatest { period ->
        Log.d(TAG, "📅 Period changed: ${period.format()} (${period.type})")
        combine(
            transactionRepository.getTransactionsForPeriod(period),
            transactionRepository.getMonthSummary(period)
        ) { allTransactions, _ ->
            // Filter only UAE/GCC currency transactions
            val uaeTransactions = allTransactions.filter { txn ->
                txn.currency.uppercase() in UAE_CURRENCIES
            }
            
            Log.d(TAG, "🇦🇪 UAE Transactions: ${uaeTransactions.size}/${allTransactions.size}")
            
            // Calculate multi-currency summary for UAE currencies only
            val multiCurrencySummary = calculateUAEMultiCurrencySummary(uaeTransactions)
            
            UAEHomeUiState.Success(
                transactions = uaeTransactions,
                currentPeriod = period,
                multiCurrencySummary = multiCurrencySummary
            ) as UAEHomeUiState
        }
    }.catch { e ->
        Log.e(TAG, "❌ Error in UI state: ${e.message}", e)
        emit(UAEHomeUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        UAEHomeUiState.Loading
    )
    
    private fun calculateUAEMultiCurrencySummary(transactions: List<TransactionEntity>): MultiCurrencySummary {
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
        
        // For UAE view, treat AED as "primary" and others as international
        val aedSummary = currencySummaries.firstOrNull { it.currency.uppercase() == "AED" }
        val otherGccSummaries = currencySummaries.filter { it.currency.uppercase() != "AED" }
        
        return MultiCurrencySummary(
            inrSummary = aedSummary, // Use AED as primary for UAE view
            internationalSummaries = otherGccSummaries
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
        _selectedPeriod.value = _selectedPeriod.value.next()
    }
}

sealed class UAEHomeUiState {
    object Loading : UAEHomeUiState()
    data class Success(
        val transactions: List<TransactionEntity>,
        val currentPeriod: Period,
        val multiCurrencySummary: MultiCurrencySummary
    ) : UAEHomeUiState()
    data class Error(val message: String) : UAEHomeUiState()
}
