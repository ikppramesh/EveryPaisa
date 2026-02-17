package com.everypaisa.tracker.di

import android.content.Context
import androidx.room.Room
import com.everypaisa.tracker.data.dao.*
import com.everypaisa.tracker.data.db.DatabaseSeedCallback
import com.everypaisa.tracker.data.db.EveryPaisaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EveryPaisaDatabase {
        return Room.databaseBuilder(
            context,
            EveryPaisaDatabase::class.java,
            "everypaisa_db"
        )
        .addCallback(DatabaseSeedCallback())
        .fallbackToDestructiveMigration() // TODO: Add proper migrations for production
        .build()
    }
    
    @Provides
    fun provideTransactionDao(database: EveryPaisaDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideCategoryDao(database: EveryPaisaDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideMerchantMappingDao(database: EveryPaisaDatabase): MerchantMappingDao {
        return database.merchantMappingDao()
    }
    
    @Provides
    fun provideSubscriptionDao(database: EveryPaisaDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
    
    @Provides
    fun provideAccountBalanceDao(database: EveryPaisaDatabase): AccountBalanceDao {
        return database.accountBalanceDao()
    }
}
