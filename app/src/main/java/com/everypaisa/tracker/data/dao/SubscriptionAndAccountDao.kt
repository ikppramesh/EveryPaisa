package com.everypaisa.tracker.data.dao

import androidx.room.*
import com.everypaisa.tracker.data.entity.AccountBalanceEntity
import com.everypaisa.tracker.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscriptions WHERE state = 'ACTIVE' ORDER BY next_payment_date")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions ORDER BY next_payment_date DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long
    
    @Update
    suspend fun update(subscription: SubscriptionEntity)
    
    @Delete
    suspend fun delete(subscription: SubscriptionEntity)
    
    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}

@Dao
interface AccountBalanceDao {
    
    @Query("SELECT * FROM account_balances ORDER BY bank_name")
    fun getAllAccounts(): Flow<List<AccountBalanceEntity>>
    
    @Query("SELECT * FROM account_balances WHERE bank_name = :bankName AND account_last4 = :accountLast4")
    fun getAccount(bankName: String, accountLast4: String): Flow<AccountBalanceEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountBalanceEntity): Long
    
    @Update
    suspend fun update(account: AccountBalanceEntity)
    
    @Delete
    suspend fun delete(account: AccountBalanceEntity)
    
    @Query("DELETE FROM account_balances")
    suspend fun deleteAll()
}
