package com.everypaisa.tracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.everypaisa.tracker.data.dao.*
import com.everypaisa.tracker.data.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        MerchantMappingEntity::class,
        SubscriptionEntity::class,
        AccountBalanceEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EveryPaisaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun accountBalanceDao(): AccountBalanceDao
}
