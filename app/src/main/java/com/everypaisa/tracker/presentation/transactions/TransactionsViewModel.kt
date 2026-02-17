package com.everypaisa.tracker.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _selectedPeriod = MutableStateFlow(Period.currentMonth())
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    val uiState: StateFlow<TransactionsUiState> = combine(
        _selectedPeriod,
        _selectedCategory,
        _searchQuery
    ) { period, category, query ->
        Triple(period, category, query)
    }.flatMapLatest { (period, category, query) ->
        val baseFlow = if (category != null) {
            transactionRepository.getTransactionsByCategory(category, period)
        } else {
            transactionRepository.getTransactionsForPeriod(period)
        }
        
        baseFlow.map { transactions ->
            val filtered = if (query.isNotBlank()) {
                transactions.filter { 
                    it.merchantName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            } else {
                transactions
            }
            
            TransactionsUiState.Success(
                transactions = filtered,
                totalAmount = filtered.sumOf { it.amount },
                period = period,
                selectedCategory = category
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
}

sealed interface TransactionsUiState {
    object Loading : TransactionsUiState
    data class Success(
        val transactions: List<TransactionEntity>,
        val totalAmount: BigDecimal,
        val period: Period,
        val selectedCategory: String?
    ) : TransactionsUiState
    data class Error(val message: String) : TransactionsUiState
}
