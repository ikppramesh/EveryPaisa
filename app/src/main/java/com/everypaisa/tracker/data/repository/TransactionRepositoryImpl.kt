package com.everypaisa.tracker.data.repository

import com.everypaisa.tracker.data.dao.TransactionDao
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.domain.model.CategorySpending
import com.everypaisa.tracker.domain.model.MonthSummary
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.ZoneId
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    
    override fun getTransactionsForPeriod(period: Period): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForPeriod(
            startDate = period.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endDate = period.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    
    override fun getTransactionsByCategory(
        category: String,
        period: Period
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(
            category = category,
            startDate = period.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endDate = period.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }
    
    override fun getTransactionById(id: Long): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id)
    }
    
    override fun getTotalExpenses(period: Period): Flow<BigDecimal> {
        return transactionDao.getTotalExpenses(
            startDate = period.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endDate = period.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ).map { it ?: BigDecimal.ZERO }
    }
    
    override fun getTotalIncome(period: Period): Flow<BigDecimal> {
        return transactionDao.getTotalIncome(
            startDate = period.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endDate = period.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ).map { it ?: BigDecimal.ZERO }
    }
    
    override fun getMonthSummary(period: Period): Flow<MonthSummary> {
        val startMillis = period.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = period.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return combine(
            transactionDao.getTotalIncome(startMillis, endMillis),
            transactionDao.getTotalExpenses(startMillis, endMillis),
            transactionDao.getTransactionCount(startMillis, endMillis)
        ) { income, expenses, count ->
            MonthSummary(
                totalIncome = income ?: BigDecimal.ZERO,
                totalExpenses = expenses ?: BigDecimal.ZERO,
                transactionCount = count
            )
        }
    }
    
    override fun getCategorySpending(period: Period): Flow<List<CategorySpending>> {
        return getTransactionsForPeriod(period).map { transactions ->
            val total = transactions.sumOf { it.amount }
            
            transactions
                .groupBy { it.category }
                .map { (category, txns) ->
                    val categoryTotal = txns.sumOf { it.amount }
                    CategorySpending(
                        category = category,
                        totalAmount = categoryTotal,
                        transactionCount = txns.size,
                        percentage = if (total > BigDecimal.ZERO) {
                            (categoryTotal.divide(total, 4, java.math.RoundingMode.HALF_UP) * BigDecimal(100)).toFloat()
                        } else 0f
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }
    
    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }
    
    override suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> {
        return transactionDao.insertAll(transactions)
    }
    
    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }
    
    override suspend fun deleteTransaction(id: Long) {
        transactionDao.softDelete(id)
    }
    
    override suspend fun restoreTransaction(id: Long) {
        transactionDao.restore(id)
    }

    override suspend fun markAsAtmWithdrawal(id: Long, flag: Boolean) {
        transactionDao.markAsAtmWithdrawal(id, flag)
    }

    override suspend fun markAsInterAccountTransfer(id: Long, flag: Boolean) {
        transactionDao.markAsInterAccountTransfer(id, flag)
    }

    override fun getDistinctCurrencies(): kotlinx.coroutines.flow.Flow<List<String>> {
        return transactionDao.getDistinctCurrencies()
    }
    
    override suspend fun getTransactionsByAmountRange(
        minAmount: Double,
        maxAmount: Double,
        afterDate: Long
    ): List<TransactionEntity> {
        return transactionDao.getTransactionsByAmountRange(minAmount, maxAmount, afterDate)
    }

    override suspend fun countDuplicatesInWindow(
        amount: java.math.BigDecimal,
        bankName: String,
        startTime: Long,
        endTime: Long
    ): Int {
        return transactionDao.countDuplicatesByAmountAndTime(amount, bankName, startTime, endTime)
    }

    // SMS sync helpers
    override suspend fun markSmsTransactionsDeletedExcept(smsIds: List<Long>) {
        // SQLite limits the number of host parameters in a single query (usually 999).
        // If the SMS inbox is large we can't safely pass the full list to a NOT IN
        // clause, because the statement would fail and the cleanup would be skipped
        // entirely (which is what the user reported).  To avoid this we take a two‑step
        // approach:
        //   1. mark *all* SMS‑derived transactions deleted
        //   2. restore (undelete) only those whose IDs we still observe, processing the
        //      kept set in chunks small enough to avoid the parameter limit.
        if (smsIds.isEmpty()) {
            transactionDao.markAllSmsTransactionsDeleted()
            return
        }

        transactionDao.markAllSmsTransactionsDeleted()
        val chunkSize = 800 // safe margin under 999
        smsIds.chunked(chunkSize).forEach { chunk ->
            transactionDao.restoreSmsTransactions(chunk)
        }
    }

    override suspend fun markAllSmsTransactionsDeleted() {
        transactionDao.markAllSmsTransactionsDeleted()
    }

    override suspend fun getAllSmsIds(): List<Long> {
        return transactionDao.getAllSmsIds()
    }
}
