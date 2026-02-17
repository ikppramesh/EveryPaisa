package com.everypaisa.tracker.domain.repository

import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.domain.model.CategorySpending
import com.everypaisa.tracker.domain.model.MonthSummary
import com.everypaisa.tracker.domain.model.Period
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {
    fun getTransactionsForPeriod(period: Period): Flow<List<TransactionEntity>>
    fun getTransactionsByCategory(category: String, period: Period): Flow<List<TransactionEntity>>
    fun getRecentTransactions(limit: Int = 50): Flow<List<TransactionEntity>>
    fun getTransactionById(id: Long): Flow<TransactionEntity?>
    fun getTotalExpenses(period: Period): Flow<BigDecimal>
    fun getTotalIncome(period: Period): Flow<BigDecimal>
    fun getMonthSummary(period: Period): Flow<MonthSummary>
    fun getCategorySpending(period: Period): Flow<List<CategorySpending>>
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(id: Long)
    suspend fun restoreTransaction(id: Long)
    suspend fun getTransactionsByAmountRange(minAmount: Double, maxAmount: Double, afterDate: Long): List<TransactionEntity>
}
