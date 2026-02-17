package com.everypaisa.tracker.data.repository

import com.everypaisa.tracker.data.dao.AccountBalanceDao
import com.everypaisa.tracker.data.dao.SubscriptionDao
import com.everypaisa.tracker.data.entity.AccountBalanceEntity
import com.everypaisa.tracker.data.entity.SubscriptionEntity
import com.everypaisa.tracker.domain.repository.AccountBalanceRepository
import com.everypaisa.tracker.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) : SubscriptionRepository {
    override fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getActiveSubscriptions()
    override fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    override fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?> = subscriptionDao.getSubscriptionById(id)
    override suspend fun insertSubscription(subscription: SubscriptionEntity): Long = subscriptionDao.insert(subscription)
    override suspend fun updateSubscription(subscription: SubscriptionEntity) = subscriptionDao.update(subscription)
    override suspend fun deleteSubscription(subscription: SubscriptionEntity) = subscriptionDao.delete(subscription)
}

class AccountBalanceRepositoryImpl @Inject constructor(
    private val accountBalanceDao: AccountBalanceDao
) : AccountBalanceRepository {
    override fun getAllAccounts(): Flow<List<AccountBalanceEntity>> = accountBalanceDao.getAllAccounts()
    override fun getAccount(bankName: String, accountLast4: String): Flow<AccountBalanceEntity?> = accountBalanceDao.getAccount(bankName, accountLast4)
    override suspend fun insertAccount(account: AccountBalanceEntity): Long = accountBalanceDao.insert(account)
    override suspend fun updateAccount(account: AccountBalanceEntity) = accountBalanceDao.update(account)
    override suspend fun deleteAccount(account: AccountBalanceEntity) = accountBalanceDao.delete(account)
}
