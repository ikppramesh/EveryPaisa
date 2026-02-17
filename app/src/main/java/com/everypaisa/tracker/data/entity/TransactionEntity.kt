package com.everypaisa.tracker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transaction_hash"], unique = true),
        Index(value = ["date_time"]),
        Index(value = ["category"]),
        Index(value = ["merchant_name"]),
        Index(value = ["currency"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val amount: BigDecimal,
    
    @ColumnInfo(name = "merchant_name")
    val merchantName: String,
    
    val category: String,
    
    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType,
    
    @ColumnInfo(name = "date_time")
    val dateTime: LocalDateTime,
    
    val description: String? = null,
    
    @ColumnInfo(name = "sms_body")
    val smsBody: String? = null,
    
    @ColumnInfo(name = "sms_sender")
    val smsSender: String? = null,
    
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,
    
    @ColumnInfo(name = "account_last4")
    val accountLast4: String? = null,
    
    @ColumnInfo(name = "transaction_hash")
    val transactionHash: String,
    
    val currency: String = "INR",
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "from_account")
    val fromAccount: String? = null,
    
    @ColumnInfo(name = "to_account")
    val toAccount: String? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    CREDIT,
    TRANSFER,
    INVESTMENT
}
