package com.everypaisa.tracker.data.dao

import androidx.room.*
import com.everypaisa.tracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface TransactionDao {
    
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
        AND date_time BETWEEN :startDate AND :endDate
        ORDER BY date_time DESC
    """)
    fun getTransactionsForPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0 AND category = :category
        AND date_time BETWEEN :startDate AND :endDate
        ORDER BY date_time DESC
    """)
    fun getTransactionsByCategory(
        category: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
        ORDER BY date_time DESC
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int = 50): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<TransactionEntity?>
    
    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE is_deleted = 0 AND transaction_type = 'EXPENSE'
        AND is_atm_withdrawal = 0
        AND is_inter_account_transfer = 0
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<BigDecimal?>
    
    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE is_deleted = 0 AND transaction_type IN ('INCOME', 'CREDIT')
        AND is_inter_account_transfer = 0
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<BigDecimal?>
    
    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE is_deleted = 0
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTransactionCount(startDate: Long, endDate: Long): Flow<Int>
    
    @Query("""
        SELECT DISTINCT currency FROM transactions
        WHERE is_deleted = 0
        ORDER BY currency
    """)
    fun getDistinctCurrencies(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM transactions WHERE transaction_hash = :hash")
    suspend fun existsByHash(hash: String): Int
    
    @Update
    suspend fun update(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)
    
    @Query("UPDATE transactions SET is_deleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE transactions SET is_atm_withdrawal = :flag WHERE id = :id")
    suspend fun markAsAtmWithdrawal(id: Long, flag: Boolean)

    @Query("UPDATE transactions SET is_inter_account_transfer = :flag WHERE id = :id")
    suspend fun markAsInterAccountTransfer(id: Long, flag: Boolean)
    
    @Query("DELETE FROM transactions WHERE is_deleted = 1")
    suspend fun deleteSoftDeleted()
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY date_time DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
        AND transaction_type = 'EXPENSE'
        AND date_time BETWEEN :startDate AND :endDate
        ORDER BY amount DESC
    """)
    fun getTransactionsForPeriodByAmount(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE is_deleted = 0
        AND amount = :amount
        AND bank_name = :bankName
        AND account_last4 = :accountLast4
        AND date_time BETWEEN :startTime AND :endTime
    """)
    suspend fun countDuplicatesInWindow(
        amount: BigDecimal,
        bankName: String,
        accountLast4: String,
        startTime: Long,
        endTime: Long
    ): Int

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE is_deleted = 0
        AND amount = :amount
        AND date_time BETWEEN :startTime AND :endTime
        AND (bank_name = :bankName OR account_last4 IS NULL)
    """)
    suspend fun countDuplicatesByAmountAndTime(
        amount: BigDecimal,
        bankName: String,
        startTime: Long,
        endTime: Long
    ): Int

    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
        AND amount BETWEEN :minAmount AND :maxAmount
        AND date_time >= :afterDate
        ORDER BY date_time DESC
    """)
    suspend fun getTransactionsByAmountRange(
        minAmount: Double,
        maxAmount: Double,
        afterDate: Long
    ): List<TransactionEntity>
}
