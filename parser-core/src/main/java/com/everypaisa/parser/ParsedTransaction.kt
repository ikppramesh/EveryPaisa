package com.everypaisa.parser

import java.math.BigDecimal
import java.time.LocalDateTime

data class ParsedTransaction(
    val amount: BigDecimal,
    val merchantName: String,
    val transactionType: TransactionType,
    val dateTime: LocalDateTime,
    val bankName: String,
    val accountLast4: String? = null,
    val cardLast4: String? = null,
    val balance: BigDecimal? = null,
    val mandateInfo: MandateInfo? = null,
    val rawMessage: String,
    val currency: String = "INR"  // Default to INR for backward compatibility
)

enum class TransactionType {
    DEBIT,
    CREDIT,
    REFUND,
    TRANSFER,
    MANDATE_CREATED,
    MANDATE_EXECUTED,
    FAILED
}

data class MandateInfo(
    val umn: String,
    val frequency: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val maxAmount: BigDecimal
)
