package com.everypaisa.tracker.di

import com.everypaisa.tracker.data.repository.*
import com.everypaisa.tracker.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
    
    @Binds
    @Singleton
    abstract fun bindMerchantMappingRepository(
        impl: MerchantMappingRepositoryImpl
    ): MerchantMappingRepository
    
    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository
    
    @Binds
    @Singleton
    abstract fun bindAccountBalanceRepository(
        impl: AccountBalanceRepositoryImpl
    ): AccountBalanceRepository
}
