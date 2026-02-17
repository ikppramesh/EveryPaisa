package com.everypaisa.tracker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String,
    
    val color: String, // Hex color code
    
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = true,
    
    @ColumnInfo(name = "is_income")
    val isIncome: Boolean = false,
    
    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0
)

@Entity(tableName = "merchant_mappings")
data class MerchantMappingEntity(
    @PrimaryKey
    @ColumnInfo(name = "merchant_name")
    val merchantName: String,
    
    val category: String
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "merchant_name")
    val merchantName: String,
    
    val amount: BigDecimal,
    
    @ColumnInfo(name = "next_payment_date")
    val nextPaymentDate: LocalDate,
    
    val state: SubscriptionState,
    
    val umn: String? = null, // Unique Mandate Number for e-mandates
    
    val currency: String = "INR",
    
    val category: String = "Subscriptions"
)

enum class SubscriptionState {
    ACTIVE,
    PAUSED,
    CANCELLED,
    HIDDEN
}

@Entity(tableName = "account_balances")
data class AccountBalanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "bank_name")
    val bankName: String,
    
    @ColumnInfo(name = "account_last4")
    val accountLast4: String,
    
    val balance: BigDecimal,
    
    @ColumnInfo(name = "account_type")
    val accountType: AccountType = AccountType.SAVINGS,
    
    val currency: String = "INR"
)

enum class AccountType {
    SAVINGS,
    CURRENT,
    CREDIT_CARD
}
