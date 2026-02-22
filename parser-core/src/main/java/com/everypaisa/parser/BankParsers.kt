package com.everypaisa.parser

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

// ──────────────────────────────────────────────────────────────────
// Shared utility for all parsers
// ──────────────────────────────────────────────────────────────────
internal object ParserUtils {
    
    fun extractCurrency(message: String): String {
        val lower = message.lowercase()
        
        // PRIORITY 1: Check for currency symbols directly attached to amounts (₹2172.29, $10.00, €50.00)
        val symbolPatterns = listOf(
            "₹\\s*[\\d,]+\\.?\\d*" to "INR",
            "\\$\\s*[\\d,]+\\.?\\d*" to "USD",
            "€\\s*[\\d,]+\\.?\\d*" to "EUR",
            "£\\s*[\\d,]+\\.?\\d*" to "GBP",
            "¥\\s*[\\d,]+\\.?\\d*" to "JPY",
            "د\\.إ\\s*[\\d,]+\\.?\\d*" to "AED",
            "﷼\\s*[\\d,]+\\.?\\d*" to "SAR",
            "₨\\s*[\\d,]+\\.?\\d*" to "NPR",
            "₦\\s*[\\d,]+\\.?\\d*" to "NGN",
            "₺\\s*[\\d,]+\\.?\\d*" to "TRY",
            "₽\\s*[\\d,]+\\.?\\d*" to "RUB",
            "₱\\s*[\\d,]+\\.?\\d*" to "PHP",
            "₫\\s*[\\d,]+\\.?\\d*" to "VND",
            "₩\\s*[\\d,]+\\.?\\d*" to "KRW",
            "฿\\s*[\\d,]+\\.?\\d*" to "THB",
            "৳\\s*[\\d,]+\\.?\\d*" to "BDT"
        )
        
        for ((pattern, currency) in symbolPatterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return currency
            }
        }
        
        // PRIORITY 2: Check for currency codes directly before amounts (USD 10.00, AED 500.00, EUR 50.00)
        val codeWithAmountPatterns = listOf(
            "USD\\s+[\\d,]+\\.?\\d*" to "USD",
            "AED\\s+[\\d,]+\\.?\\d*" to "AED",
            "EUR\\s+[\\d,]+\\.?\\d*" to "EUR",
            "GBP\\s+[\\d,]+\\.?\\d*" to "GBP",
            "SAR\\s+[\\d,]+\\.?\\d*" to "SAR",
            "QAR\\s+[\\d,]+\\.?\\d*" to "QAR",
            "OMR\\s+[\\d,]+\\.?\\d*" to "OMR",
            "KWD\\s+[\\d,]+\\.?\\d*" to "KWD",
            "BHD\\s+[\\d,]+\\.?\\d*" to "BHD",
            "CAD\\s+[\\d,]+\\.?\\d*" to "CAD",
            "AUD\\s+[\\d,]+\\.?\\d*" to "AUD",
            "SGD\\s+[\\d,]+\\.?\\d*" to "SGD",
            "HKD\\s+[\\d,]+\\.?\\d*" to "HKD",
            "NZD\\s+[\\d,]+\\.?\\d*" to "NZD",
            "JPY\\s+[\\d,]+\\.?\\d*" to "JPY",
            "CNY\\s+[\\d,]+\\.?\\d*" to "CNY",
            "CHF\\s+[\\d,]+\\.?\\d*" to "CHF",
            "NPR\\s+[\\d,]+\\.?\\d*" to "NPR",
            "PKR\\s+[\\d,]+\\.?\\d*" to "PKR",
            "LKR\\s+[\\d,]+\\.?\\d*" to "LKR",
            "BDT\\s+[\\d,]+\\.?\\d*" to "BDT",
            "THB\\s+[\\d,]+\\.?\\d*" to "THB",
            "MYR\\s+[\\d,]+\\.?\\d*" to "MYR",
            "IDR\\s+[\\d,]+\\.?\\d*" to "IDR",
            "PHP\\s+[\\d,]+\\.?\\d*" to "PHP",
            "VND\\s+[\\d,]+\\.?\\d*" to "VND",
            "KRW\\s+[\\d,]+\\.?\\d*" to "KRW",
            "TRY\\s+[\\d,]+\\.?\\d*" to "TRY",
            "RUB\\s+[\\d,]+\\.?\\d*" to "RUB",
            "ZAR\\s+[\\d,]+\\.?\\d*" to "ZAR",
            "NGN\\s+[\\d,]+\\.?\\d*" to "NGN",
            "ETB\\s+[\\d,]+\\.?\\d*" to "ETB",
            "MXN\\s+[\\d,]+\\.?\\d*" to "MXN",
            "ARS\\s+[\\d,]+\\.?\\d*" to "ARS",
            "CLP\\s+[\\d,]+\\.?\\d*" to "CLP",
            "COP\\s+[\\d,]+\\.?\\d*" to "COP",
            "TWD\\s+[\\d,]+\\.?\\d*" to "TWD",
            "KES\\s+[\\d,]+\\.?\\d*" to "KES",
            "EGP\\s+[\\d,]+\\.?\\d*" to "EGP",
            "MMK\\s+[\\d,]+\\.?\\d*" to "MMK",
            "KHR\\s+[\\d,]+\\.?\\d*" to "KHR",
            "LAK\\s+[\\d,]+\\.?\\d*" to "LAK"
        )
        
        for ((pattern, currency) in codeWithAmountPatterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return currency
            }
        }
        
        // PRIORITY 3: Check for explicit currency keywords and location hints
        val currencyPatterns = mapOf(
            // Middle East (GCC Countries)
            "aed" to "AED",
            "dirham" to "AED",
            "dubai" to "AED",
            "uae" to "AED",
            "abu dhabi" to "AED",
            "sharjah" to "AED",
            "sar" to "SAR",
            "riyal" to "SAR",
            "saudi" to "SAR",
            "qar" to "QAR",
            "qatar" to "QAR",
            "omr" to "OMR",
            "oman" to "OMR",
            "kwd" to "KWD",
            "kuwait" to "KWD",
            "bhd" to "BHD",
            "bahrain" to "BHD",
            // Major Currencies
            "us dollar" to "USD",
            "euro" to "EUR",
            "pound sterling" to "GBP",
            "yen" to "JPY",
            "yuan" to "CNY",
            "swiss franc" to "CHF",
            // Commonwealth
            "canadian dollar" to "CAD",
            "australian dollar" to "AUD",
            "singapore dollar" to "SGD",
            // South Asia
            "nepali rupee" to "NPR",
            "pakistani rupee" to "PKR",
            "sri lankan rupee" to "LKR",
            "taka" to "BDT",
            // Southeast Asia
            "baht" to "THB",
            "ringgit" to "MYR",
            "rupiah" to "IDR",
            "peso" to "PHP",
            "dong" to "VND",
            "won" to "KRW",
            // Others
            "lira" to "TRY",
            "ruble" to "RUB",
            "real" to "BRL",
            "rand" to "ZAR",
            "naira" to "NGN",
            "birr" to "ETB",
            "shilling" to "KES",
            "dirham" to "AED"
        )
        
        for ((pattern, currency) in currencyPatterns) {
            if (lower.contains(pattern)) {
                return currency
            }
        }
        
        // PRIORITY 4: Check for Rs/INR which is common in Indian SMS
        if (lower.contains("rs.") || lower.contains("rs ") || lower.contains("inr")) {
            return "INR"
        }
        
        // Default to INR if no other currency found
        return "INR"
    }
    
    fun extractAmount(message: String): BigDecimal? {
        val currency = extractCurrency(message)
        
        // Try currency-specific patterns first
        val currencyPatterns = when (currency) {
            "USD", "CAD", "AUD", "NZD", "SGD", "HKD" -> listOf(
                Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\$\\s*([\\d,]+\\.?\\d*)"),
                Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            "EUR" -> listOf(
                Pattern.compile("EUR\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("€\\s*([\\d,]+\\.?\\d*)")
            )
            "GBP" -> listOf(
                Pattern.compile("GBP\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("£\\s*([\\d,]+\\.?\\d*)")
            )
            "AED", "SAR", "QAR", "OMR", "KWD", "BHD" -> listOf(
                Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:credited|debited)(?:\\s+to)?(?:\\s+A/C)?\\s+[^0-9]*${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            "JPY", "CNY", "KRW" -> listOf(
                Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("¥\\s*([\\d,]+\\.?\\d*)"),
                Pattern.compile("₩\\s*([\\d,]+\\.?\\d*)")
            )
            "NPR", "PKR", "LKR" -> listOf(
                Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₨\\s*([\\d,]+\\.?\\d*)")
            )
            "THB" -> listOf(
                Pattern.compile("THB\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("฿\\s*([\\d,]+\\.?\\d*)")
            )
            "PHP" -> listOf(
                Pattern.compile("PHP\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₱\\s*([\\d,]+\\.?\\d*)")
            )
            "VND" -> listOf(
                Pattern.compile("VND\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₫\\s*([\\d,]+\\.?\\d*)")
            )
            "TRY" -> listOf(
                Pattern.compile("TRY\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₺\\s*([\\d,]+\\.?\\d*)")
            )
            "RUB" -> listOf(
                Pattern.compile("RUB\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₽\\s*([\\d,]+\\.?\\d*)")
            )
            "BDT" -> listOf(
                Pattern.compile("BDT\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("৳\\s*([\\d,]+\\.?\\d*)")
            )
            "NGN" -> listOf(
                Pattern.compile("NGN\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("₦\\s*([\\d,]+\\.?\\d*)")
            )
            "CHF", "ZAR", "MYR", "IDR", "BRL", "MXN", "ARS", "CLP", "COP", "TWD", 
            "ETB", "KES", "EGP", "MMK", "KHR", "LAK" -> listOf(
                Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            )
            else -> listOf()
        }
        
        // Try currency-specific patterns
        for (p in currencyPatterns) {
            val m = p.matcher(message)
            if (m.find()) {
                val s = m.group(1)?.replace(",", "") ?: continue
                val bd = try { BigDecimal(s) } catch (_: Exception) { continue }
                if (bd > BigDecimal.ZERO && bd < BigDecimal("10000000")) return bd
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
// Citi Bank (International)
// ──────────────────────────────────────────────────────────────────
class CitiBankParser : BankParser {
    
    private val senders = listOf("CITI", "CITIBANK")
    
    override fun canParse(sender: String, message: String): Boolean {
        val senderMatch = senders.any { sender.contains(it, ignoreCase = true) }
        val msgMatch = message.contains("Citi", ignoreCase = true) || 
                      message.contains("Citibank", ignoreCase = true)
        return senderMatch || msgMatch
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = ParserUtils.extractCurrency(message)
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Citibank")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "Citibank",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// HSBC (International)
// ──────────────────────────────────────────────────────────────────
class HSBCParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("HSBC", ignoreCase = true) ||
               message.contains("HSBC", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = ParserUtils.extractCurrency(message)
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "HSBC")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "HSBC",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// Standard Chartered (International)
// ──────────────────────────────────────────────────────────────────
class StandardCharteredParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("SC", ignoreCase = true) && sender.contains("BANK", ignoreCase = true) ||
               message.contains("Standard Chartered", ignoreCase = true) ||
               message.contains("StanChart", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = ParserUtils.extractCurrency(message)
        val type = ParserUtils.determineType(message)
        val merchant = ParserUtils.extractMerchant(message, "Standard Chartered")
        val accountLast4 = ParserUtils.extractAccountLast4(message)
        val cardLast4 = ParserUtils.extractCardLast4(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "Standard Chartered",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            accountLast4 = accountLast4,
            cardLast4 = cardLast4,
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
