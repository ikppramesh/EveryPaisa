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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>
    
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

    // ── SMS inbox synchronization helpers ─────────────────────────
    /**
     * Soft-delete any existing database transaction whose originating SMS is no longer
     * present in the device inbox.  Transactions added manually (sms_id == null) are
     * left untouched.
     */
    @Query("UPDATE transactions SET is_deleted = 1 WHERE sms_id IS NOT NULL AND sms_id NOT IN (:smsIds)")
    suspend fun markSmsTransactionsDeletedExcept(smsIds: List<Long>)

    /**
     * Shortcut used when there are no SMS messages available (e.g. permission revoked).
     * Marks all transactions created from SMS as deleted.
     */
    @Query("UPDATE transactions SET is_deleted = 1 WHERE sms_id IS NOT NULL")
    suspend fun markAllSmsTransactionsDeleted()

    /**
     * Undo the above operation for the given list of SMS IDs.  Used when the
     * observed inbox contains these messages; since we sometimes mark everything as
     * deleted first, this query restores the ones that still exist.
     */
    @Query("UPDATE transactions SET is_deleted = 0 WHERE sms_id IN (:smsIds)")
    suspend fun restoreSmsTransactions(smsIds: List<Long>)

    /**
     * Return all SMS-based transaction ids currently stored (whether deleted or not).
     * Helps with diagnostics and cleanup logic.
     */
    @Query("SELECT sms_id FROM transactions WHERE sms_id IS NOT NULL")
    suspend fun getAllSmsIds(): List<Long>

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
