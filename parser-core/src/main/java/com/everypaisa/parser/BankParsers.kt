package com.everypaisa.parser

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

// ──────────────────────────────────────────────────────────────────
// Shared utility for all parsers
// ──────────────────────────────────────────────────────────────────
private object ParserUtils {
    
    fun extractCurrency(message: String): String {
        val lower = message.lowercase()
        
        // Check for explicit currency codes first
        val currencyPatterns = mapOf(
            "usd" to "USD",
            "us dollar" to "USD",
            "dollar" to "USD",
            "$" to "USD",
            "eur" to "EUR",
            "euro" to "EUR",
            "€" to "EUR",
            "gbp" to "GBP",
            "pound" to "GBP",
            "£" to "GBP",
            "aed" to "AED",
            "dirham" to "AED",
            "د.إ" to "AED",
            "dubai" to "AED",
            "uae" to "AED",
            "abu dhabi" to "AED",
            "sharjah" to "AED",
            "npr" to "NPR",
            "nepali rupee" to "NPR",
            "₨" to "NPR",
            "etb" to "ETB",
            "birr" to "ETB",
            "ብር" to "ETB",
            "cad" to "CAD",
            "canadian dollar" to "CAD",
            "aud" to "AUD",
            "australian dollar" to "AUD",
            "sgd" to "SGD",
            "singapore dollar" to "SGD",
            "jpy" to "JPY",
            "yen" to "JPY",
            "¥" to "JPY",
            "chf" to "CHF",
            "swiss franc" to "CHF"
        )
        
        for ((pattern, currency) in currencyPatterns) {
            if (lower.contains(pattern)) {
                return currency
            }
        }
        
        // Default to INR if no other currency found
        return "INR"
    }
    
    fun extractAmount(message: String): BigDecimal? {
        val currency = extractCurrency(message)
        
        // Try currency-specific patterns first
        val currencyPatterns = when (currency) {
            "USD" -> listOf(
                Pattern.compile("USD\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\$\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?USD\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            "EUR" -> listOf(
                Pattern.compile("EUR\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("€\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            "GBP" -> listOf(
                Pattern.compile("GBP\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("£\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            "AED" -> listOf(
                Pattern.compile("AED\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            else -> listOf()
        }
        
        // Try currency-specific patterns
        for (p in currencyPatterns) {
            val m = p.matcher(message)
            if (m.find()) {
                val s = m.group(1)?.replace(",", "") ?: continue
                val bd = try { BigDecimal(s) } catch (_: Exception) { continue }
                if (bd > BigDecimal.ZERO && bd < BigDecimal("5000000")) return bd
            }
        }
        
        // Fallback to INR patterns
        val patterns = listOf(
            Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:txn|transaction)\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        )
        for (p in patterns) {
            val m = p.matcher(message)
            if (m.find()) {
                val s = m.group(1)?.replace(",", "") ?: continue
                val bd = try { BigDecimal(s) } catch (_: Exception) { continue }
                if (bd > BigDecimal.ZERO && bd < BigDecimal("5000000")) return bd
            }
        }
        return null
    }
    
    fun extractAccountLast4(message: String): String? {
        val patterns = listOf(
            Pattern.compile("a/c\\s*(?:no\\.?)?\\s*[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("account\\s*(?:no\\.?)?\\s*[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("A/C\\s*[xX*]+([\\d]{4})"),
            Pattern.compile("XX(\\d{4})")
        )
        for (p in patterns) {
            val m = p.matcher(message)
            if (m.find()) return m.group(1)
        }
        return null
    }
    
    fun extractCardLast4(message: String): String? {
        val patterns = listOf(
            Pattern.compile("card\\s*(?:no\\.?)?\\s*(?:ending\\s*)?[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:debit|credit)\\s*card\\s*[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("card\\s+ending\\s+([\\d]{4})", Pattern.CASE_INSENSITIVE)
        )
        for (p in patterns) {
            val m = p.matcher(message)
            if (m.find()) return m.group(1)
        }
        return null
    }
    
    /** Determines if money is going OUT (DEBIT) or coming IN (CREDIT). 
     *  "credit card" context = DEBIT (spending via credit card). */
    fun determineType(message: String): TransactionType {
        val lower = message.lowercase()
        
        val debitSignals = listOf(
            "debited", "debit", "spent", "paid", "withdrawn", "purchase",
            "payment", "charged", "used at", "deducted", "sent", "txn of",
            "transaction of", "your txn"
        )
        val creditSignals = listOf(
            "credited to", "credited your", "credit to", "received",
            "deposited", "refund", "cashback", "salary credited", "reversed"
        )
        
        val isCreditCardContext = lower.contains("credit card") || 
                                  lower.contains("cr card") ||
                                  lower.contains("cc ending")
        val hasDebit = debitSignals.any { lower.contains(it) }
        val hasCredit = creditSignals.any { lower.contains(it) }
        
        return when {
            hasDebit && hasCredit -> TransactionType.DEBIT
            isCreditCardContext && !hasCredit -> TransactionType.DEBIT
            hasDebit -> TransactionType.DEBIT
            hasCredit -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }
    }
    
    fun extractMerchant(message: String, fallback: String): String {
        val patterns = listOf(
            Pattern.compile("(?:at|@)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)\\s+(?:on|for|using|via|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:to|towards)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)\\s+(?:on|via|using|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:paid|spent|payment)\\s+(?:at|to|for)?\\s*([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\s+on|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:purchase|txn|transaction)\\s+(?:at|from|of)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\s+on|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Info[:\\-]\\s*([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\.|,|$)", Pattern.CASE_INSENSITIVE)
        )
        for (p in patterns) {
            val m = p.matcher(message)
            if (m.find()) {
                val merchant = m.group(1)?.trim()?.take(40) ?: continue
                if (merchant.length > 2 &&
                    !merchant.matches(Regex("\\d+")) &&
                    !merchant.lowercase().let { it.startsWith("your") || it.contains("avl bal") || it.contains("available") }) {
                    return merchant.trimEnd('.', ',', ' ')
                }
            }
        }
        return fallback
    }
}

// ──────────────────────────────────────────────────────────────────
// Emirates NBD Bank (UAE)
// ──────────────────────────────────────────────────────────────────
class EmiratesNBDParser : BankParser {
    
    private val senders = listOf("EMIRATESNBD", "ENBDBANK", "ENBD", "Emirates")
    
    override fun canParse(sender: String, message: String): Boolean {
        val senderMatch = senders.any { sender.contains(it, ignoreCase = true) }
        val msgMatch = message.contains("EmiratesNBD", ignoreCase = true) ||
                      message.contains("Emirates NBD", ignoreCase = true) ||
                      message.contains("ENBD", ignoreCase = true)
        return senderMatch || msgMatch
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = ParserUtils.extractCurrency(message)
        val type = ParserUtils.determineType(message)
        
        // Extract account number (XX1234 format)
        val accountPattern = Pattern.compile("A/C\\s+XX(\\d{4})", Pattern.CASE_INSENSITIVE)
        val accountMatcher = accountPattern.matcher(message)
        val accountLast4 = if (accountMatcher.find()) accountMatcher.group(1) else null
        
        // Extract merchant from reference
        val merchant = if (message.contains("Ref:", ignoreCase = true)) {
            val refPattern = Pattern.compile("Ref:\\s*([^.]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(message)
            if (refMatcher.find()) {
                refMatcher.group(1)?.trim()?.replace("Avl Bal", "")?.trim() ?: "Emirates NBD"
            } else {
                "Emirates NBD"
            }
        } else {
            ParserUtils.extractMerchant(message, "Emirates NBD")
        }
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "Emirates NBD",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            accountLast4 = accountLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// HDFC Bank
// ──────────────────────────────────────────────────────────────────
class HDFCBankParser : BankParser {
    
    private val senders = listOf("HDFCBK", "HDFC", "HDFCBANK")
    
    override fun canParse(sender: String, message: String): Boolean {
        return senders.any { sender.contains(it, ignoreCase = true) }
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "HDFC Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        val currency = ParserUtils.extractCurrency(message)
        
        val balancePattern = Pattern.compile("(?:avl\\s*)?bal[:\\s]*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
        val balanceMatcher = balancePattern.matcher(message)
        val balance = if (balanceMatcher.find()) {
            try { BigDecimal(balanceMatcher.group(1).replace(",", "")) } catch (_: Exception) { null }
        } else null
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            dateTime = LocalDateTime.now(),
            bankName = "HDFC Bank",
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            balance = balance,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// ICICI Bank
// ──────────────────────────────────────────────────────────────────
class ICICIBankParser : BankParser {
    
    private val senders = listOf("ICICIB", "ICICI")
    
    override fun canParse(sender: String, message: String): Boolean {
        return senders.any { sender.contains(it, ignoreCase = true) }
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "ICICI Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        val currency = ParserUtils.extractCurrency(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            dateTime = LocalDateTime.now(),
            bankName = "ICICI Bank",
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// SBI
// ──────────────────────────────────────────────────────────────────
class SBIParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("SBI", ignoreCase = true) || 
               message.contains("State Bank", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "SBI Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val currency = ParserUtils.extractCurrency(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            dateTime = LocalDateTime.now(),
            bankName = "SBI",
            accountLast4 = accountLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// Axis Bank
// ──────────────────────────────────────────────────────────────────
class AxisBankParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("AXIS", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Axis Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        val currency = ParserUtils.extractCurrency(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            dateTime = LocalDateTime.now(),
            bankName = "Axis Bank",
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// Kotak Mahindra Bank
// ──────────────────────────────────────────────────────────────────
class KotakBankParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("KOTAK", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Kotak Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        val currency = ParserUtils.extractCurrency(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            dateTime = LocalDateTime.now(),
            bankName = "Kotak Bank",
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// PNB, BOB, Canara, Union Bank
// ──────────────────────────────────────────────────────────────────
class PNBParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("PNB", ignoreCase = true) || message.contains("Punjab National", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        return ParsedTransaction(
            amount = amount, merchantName = ParserUtils.extractMerchant(message, "PNB Transaction"),
            transactionType = ParserUtils.determineType(message), dateTime = LocalDateTime.now(),
            bankName = "PNB", accountLast4 = ParserUtils.extractAccountLast4(message),
            cardLast4 = ParserUtils.extractCardLast4(message), rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

class BOBParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("BOB", ignoreCase = true) || message.contains("Bank of Baroda", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        return ParsedTransaction(
            amount = amount, merchantName = ParserUtils.extractMerchant(message, "BOB Transaction"),
            transactionType = ParserUtils.determineType(message), dateTime = LocalDateTime.now(),
            bankName = "Bank of Baroda", accountLast4 = ParserUtils.extractAccountLast4(message),
            cardLast4 = ParserUtils.extractCardLast4(message), rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

class CanaraParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("CANARA", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        return ParsedTransaction(
            amount = amount, merchantName = ParserUtils.extractMerchant(message, "Canara Transaction"),
            transactionType = ParserUtils.determineType(message), dateTime = LocalDateTime.now(),
            bankName = "Canara Bank", accountLast4 = ParserUtils.extractAccountLast4(message),
            cardLast4 = ParserUtils.extractCardLast4(message), rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

class UnionBankParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("UNION", ignoreCase = true) || message.contains("Union Bank", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        return ParsedTransaction(
            amount = amount, merchantName = ParserUtils.extractMerchant(message, "Union Bank Transaction"),
            transactionType = ParserUtils.determineType(message), dateTime = LocalDateTime.now(),
            bankName = "Union Bank", accountLast4 = ParserUtils.extractAccountLast4(message),
            cardLast4 = ParserUtils.extractCardLast4(message), rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// IDFC First Bank (user has this bank)
// ──────────────────────────────────────────────────────────────────
class IDFCFirstBankParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("IDFC", ignoreCase = true) || message.contains("IDFC", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        
        // Skip non-transaction messages (e-statement reminders, etc.)
        val lower = message.lowercase()
        if (lower.contains("estatement") || lower.contains("e-statement") || 
            lower.contains("otp") || lower.contains("password")) return null
        
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "IDFC First Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "IDFC First", accountLast4 = accountLast4,
            cardLast4 = cardLast4, rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// Federal Bank (user has this - sender: AX-FEDSCP-S)
// ──────────────────────────────────────────────────────────────────
class FederalBankParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean =
        sender.contains("FEDER", ignoreCase = true) || sender.contains("FEDSCP", ignoreCase = true) ||
        message.contains("Federal Bank", ignoreCase = true)
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Federal Bank Transaction")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "Federal Bank", accountLast4 = accountLast4,
            cardLast4 = cardLast4, rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// UPI Apps: Google Pay, PhonePe, Paytm, Amazon Pay
// ──────────────────────────────────────────────────────────────────
class GooglePayParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("GPAY", ignoreCase = true) || 
               message.contains("Google Pay", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        
        // "to MERCHANT via" or "from MERCHANT via"
        val merchantPattern = Pattern.compile("(?:to|from)\\s+([^v]+?)\\s+via", Pattern.CASE_INSENSITIVE)
        val merchantMatcher = merchantPattern.matcher(message)
        val merchant = if (merchantMatcher.find()) {
            merchantMatcher.group(1).trim().take(40)
        } else ParserUtils.extractMerchant(message, "Google Pay")
        
        val type = ParserUtils.determineType(message)
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "Google Pay (UPI)", accountLast4 = accountLast4,
            rawMessage = message, currency = ParserUtils.extractCurrency(message)
        )
    }
}

class PhonePeParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("PHONEPE", ignoreCase = true) || 
               message.contains("PhonePe", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        
        val merchantPattern = Pattern.compile("(?:to|from)\\s+([^.]+?)\\.", Pattern.CASE_INSENSITIVE)
        val merchantMatcher = merchantPattern.matcher(message)
        val merchant = if (merchantMatcher.find()) {
            merchantMatcher.group(1).trim().take(40)
        } else ParserUtils.extractMerchant(message, "PhonePe")
        
        val type = ParserUtils.determineType(message)
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "PhonePe (UPI)", accountLast4 = accountLast4,
            rawMessage = message, currency = ParserUtils.extractCurrency(message)
        )
    }
}

class PaytmParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("PAYTM", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Paytm")
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "Paytm (UPI)", rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}

class AmazonPayParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("AMAZON", ignoreCase = true) && 
               message.contains("Pay", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Amazon Pay")
        
        return ParsedTransaction(
            amount = amount, merchantName = merchant,
            transactionType = type, dateTime = LocalDateTime.now(),
            bankName = "Amazon Pay", rawMessage = message,
            currency = ParserUtils.extractCurrency(message)
        )
    }
}
