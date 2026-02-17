package com.everypaisa.tracker.domain.repository

import com.everypaisa.tracker.data.entity.AccountBalanceEntity
import com.everypaisa.tracker.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long
    suspend fun updateSubscription(subscription: SubscriptionEntity)
    suspend fun deleteSubscription(subscription: SubscriptionEntity)
}

interface AccountBalanceRepository {
    fun getAllAccounts(): Flow<List<AccountBalanceEntity>>
    fun getAccount(bankName: String, accountLast4: String): Flow<AccountBalanceEntity?>
    suspend fun insertAccount(account: AccountBalanceEntity): Long
    suspend fun updateAccount(account: AccountBalanceEntity)
    suspend fun deleteAccount(account: AccountBalanceEntity)
}
