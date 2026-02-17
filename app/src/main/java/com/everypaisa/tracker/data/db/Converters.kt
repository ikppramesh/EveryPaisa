package com.everypaisa.tracker.data.db

import androidx.room.TypeConverter
import com.everypaisa.tracker.data.entity.AccountType
import com.everypaisa.tracker.data.entity.SubscriptionState
import com.everypaisa.tracker.data.entity.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    
    // BigDecimal converters
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()
    
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.toBigDecimalOrNull()
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? =
        value?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? =
        value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? =
        value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    
    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? =
        value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    
    // Enum converters
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
    
    @TypeConverter
    fun fromSubscriptionState(state: SubscriptionState): String = state.name
    
    @TypeConverter
    fun toSubscriptionState(value: String): SubscriptionState = SubscriptionState.valueOf(value)
    
    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name
    
    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)
}
