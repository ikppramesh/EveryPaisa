package com.everypaisa.tracker.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.domain.model.Country
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _selectedCountry = MutableStateFlow(Country.INDIA)
    private val _selectedPeriod = MutableStateFlow(Period.currentMonth())
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    fun setSelectedCountry(country: Country) {
        _selectedCountry.value = country
    }
    
    val uiState: StateFlow<TransactionsUiState> = combine(
        _selectedCountry,
        _selectedPeriod,
        _selectedCategory,
        _searchQuery
    ) { country, period, category, query ->
        Triple(country, Pair(period, category), query)
    }.flatMapLatest { (country, periodAndCategory, query) ->
        val (period, category) = periodAndCategory
        val baseFlow = if (category != null) {
            transactionRepository.getTransactionsByCategory(category, period)
        } else {
            transactionRepository.getTransactionsForPeriod(period)
        }
        
        baseFlow.map { transactions ->
            // Filter by country's supported currencies
            val countryFiltered = transactions.filter { txn ->
                txn.currency.uppercase() in country.supportedCurrencies.map { it.uppercase() }
            }
            
            val filtered = if (query.isNotBlank()) {
                countryFiltered.filter { 
                    it.merchantName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            } else {
                countryFiltered
            }
            
            TransactionsUiState.Success(
                transactions = filtered,
                totalAmount = filtered.sumOf { it.amount },
                period = period,
                selectedCategory = category,
                selectedCountry = country
            ) as TransactionsUiState
        }
    }.catch {
        emit(TransactionsUiState.Error(it.message ?: "Unknown error") as TransactionsUiState)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TransactionsUiState.Loading
    )
    
    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
    }
    
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun search(query: String) {
        _searchQuery.value = query
    }

    fun markTransactionAsAtm(id: Long, flag: Boolean) {
        viewModelScope.launch { transactionRepository.markAsAtmWithdrawal(id, flag) }
    }

    fun markTransactionAsInterAccount(id: Long, flag: Boolean) {
        viewModelScope.launch { transactionRepository.markAsInterAccountTransfer(id, flag) }
    }
}

sealed interface TransactionsUiState {
    object Loading : TransactionsUiState
    data class Success(
        val transactions: List<TransactionEntity>,
        val totalAmount: BigDecimal,
        val period: Period,
        val selectedCategory: String?,
        val selectedCountry: Country    ) : TransactionsUiState
    data class Error(val message: String) : TransactionsUiState
}