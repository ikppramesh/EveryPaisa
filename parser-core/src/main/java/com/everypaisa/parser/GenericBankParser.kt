package com.everypaisa.parser

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * Generic parser that attempts to parse transaction SMS from any bank
 * using common patterns in Indian banking SMS
 */
class GenericBankParser : BankParser {
    
    private val amountPatterns = listOf(
        // Multi-currency patterns
        Pattern.compile("AED\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("USD\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("EUR\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("GBP\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        // INR patterns
        Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("amt\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("amount\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("txn\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
    )
    
    // Debit keywords — money going OUT
    private val debitKeywords = listOf(
        "debited", "debit", "spent", "withdrawn", "purchase",
        "charged", "used at", "deducted", "sent to", "txn of",
        "transaction of", "your txn", "payment made", "paid to",
        "payment done", "amount paid", "using",
        "for aed", "for usd", "for eur", "for gbp"
    )

    // Strong credit keywords — money definitely coming IN (high confidence)
    private val strongCreditKeywords = listOf(
        "credited to your", "credited to acct", "credited to a/c",
        "credited in your", "amount credited", "has been credited",
        "salary credited", "cashback credited", "refund credited",
        "interest credited", "reward credited", "credited by"
    )

    // Weak credit keywords — may appear in debit SMS too
    private val weakCreditKeywords = listOf(
        "received", "deposited", "refund", "cashback", "reversed",
        "credit to your", "credited your"
    )
    
    override fun canParse(sender: String, message: String): Boolean {
        val messageLower = message.lowercase()

        val hasAmount = amountPatterns.any { it.matcher(message).find() }

        val hasTransactionKeyword = debitKeywords.any { messageLower.contains(it) } ||
                strongCreditKeywords.any { messageLower.contains(it) } ||
                weakCreditKeywords.any { messageLower.contains(it) }

        val hasAccountRef = messageLower.contains("a/c") ||
                messageLower.contains("account") ||
                messageLower.contains("card") ||
                messageLower.contains("xx") ||
                messageLower.contains("upi")

        return hasAmount && hasTransactionKeyword && hasAccountRef
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        try {
            val amount = extractAmount(message) ?: return null
            
            // Skip very large amounts likely misparse (e.g., balance statements)
            if (amount > BigDecimal("5000000")) return null
            
            val type = determineTransactionType(message)
            val merchantName = extractMerchant(message)
            val accountLast4 = extractAccountNumber(message)
            val cardLast4 = extractCardNumber(message)
            val paymentMethod = detectPaymentMethod(message)
            val currency = extractCurrency(message)
            
            return ParsedTransaction(
                amount = amount,
                merchantName = merchantName,
                bankName = extractBankName(sender, message),
                transactionType = type,
                dateTime = LocalDateTime.now(),
                accountLast4 = accountLast4,
                cardLast4 = cardLast4,
                rawMessage = message,
                currency = currency
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun extractAmount(message: String): BigDecimal? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                return try {
                    val amt = BigDecimal(amountStr)
                    if (amt > BigDecimal.ZERO) amt else null
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }
    
    private fun determineTransactionType(message: String): TransactionType {
        val messageLower = message.lowercase()

        // "credit card" context = money going OUT via credit card
        val isCreditCardContext = messageLower.contains("credit card") ||
                messageLower.contains("cr card") ||
                messageLower.contains("cc ending") ||
                messageLower.contains("credit/debit card")

        val hasDebitKeyword = debitKeywords.any { messageLower.contains(it) }
        val hasStrongCreditKeyword = strongCreditKeywords.any { messageLower.contains(it) }
        val hasWeakCreditKeyword = weakCreditKeywords.any { messageLower.contains(it) }

        return when {
            // Strong credit signal always wins (e.g. "amount credited to your a/c")
            hasStrongCreditKeyword && !isCreditCardContext -> TransactionType.CREDIT
            // Credit card context = debit regardless of "credit" word in the message
            isCreditCardContext -> TransactionType.DEBIT
            // Explicit debit with no strong credit signal
            hasDebitKeyword && !hasStrongCreditKeyword -> TransactionType.DEBIT
            // Weak credit with no debit signal
            hasWeakCreditKeyword && !hasDebitKeyword -> TransactionType.CREDIT
            // Both debit and weak credit → debit wins (e.g. "payment received" on debit SMS)
            hasDebitKeyword -> TransactionType.DEBIT
            hasWeakCreditKeyword -> TransactionType.CREDIT
            // Default
            else -> TransactionType.DEBIT
        }
    }
    
    private fun extractMerchant(message: String): String {
        val patterns = listOf(
            // "at MERCHANT on" / "at MERCHANT."
            Pattern.compile("(?:at|@)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)\\s+(?:on|for|using|via|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            // "to MERCHANT on" / "to MERCHANT via"
            Pattern.compile("(?:to|towards)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)\\s+(?:on|via|using|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            // "paid MERCHANT" / "spent at MERCHANT"
            Pattern.compile("(?:paid|spent|payment)\\s+(?:at|to|for)?\\s*([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\s+on|\\s+using|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            // "purchase at MERCHANT"
            Pattern.compile("(?:purchase|txn|transaction)\\s+(?:at|from|of)\\s+([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\s+on|\\.|,|Ref)", Pattern.CASE_INSENSITIVE),
            // "Info: MERCHANT" pattern (IDFC style)
            Pattern.compile("Info[:\\-]\\s*([A-Za-z][A-Za-z0-9\\s&.,'-]+?)(?:\\.|,|$)", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()?.take(40) ?: continue
                // Filter out noise
                if (merchant.length > 2 && 
                    !merchant.matches(Regex("\\d+")) &&
                    !merchant.lowercase().startsWith("your") &&
                    !merchant.lowercase().startsWith("the ") &&
                    !merchant.lowercase().contains("avl bal") &&
                    !merchant.lowercase().contains("available")) {
                    return merchant.trimEnd('.', ',', ' ')
                }
            }
        }
        
        return "Transaction"
    }
    
    private fun extractBankName(sender: String, message: String): String {
        val banks = mapOf(
            "HDFC" to "HDFC Bank",
            "ICICI" to "ICICI Bank", 
            "SBI" to "SBI",
            "AXIS" to "Axis Bank",
            "KOTAK" to "Kotak Bank",
            "PNB" to "PNB",
            "BOB" to "Bank of Baroda",
            "CANARA" to "Canara Bank",
            "UNION" to "Union Bank",
            "INDUS" to "IndusInd Bank",
            "YES" to "Yes Bank",
            "IDBI" to "IDBI Bank",
            "IDFC" to "IDFC First",
            "FEDER" to "Federal Bank",
            "GPAY" to "Google Pay",
            "PHONEPE" to "PhonePe",
            "PAYTM" to "Paytm"
        )
        
        for ((key, name) in banks) {
            if (sender.contains(key, ignoreCase = true) || 
                message.contains(key, ignoreCase = true)) {
                return name
            }
        }
        
        return "Bank"
    }
    
    private fun detectPaymentMethod(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("upi") -> "UPI"
            lower.contains("credit card") || lower.contains("cr card") -> "Credit Card"
            lower.contains("debit card") || lower.contains("dr card") -> "Debit Card"
            lower.contains("neft") -> "NEFT"
            lower.contains("imps") -> "IMPS"
            lower.contains("rtgs") -> "RTGS"
            lower.contains("netbanking") || lower.contains("net banking") -> "Net Banking"
            lower.contains("atm") -> "ATM"
            lower.contains("card") -> "Card"
            else -> ""
        }
    }
    
    private fun extractAccountNumber(message: String): String? {
        val patterns = listOf(
            Pattern.compile("a/c\\s*(?:no\\.?)?\\s*[xX*]*([\\d]{4})"),
            Pattern.compile("account\\s*(?:no\\.?)?\\s*[xX*]*([\\d]{4})"),
            Pattern.compile("A/C\\s*[xX*]+([\\d]{4})"),
            Pattern.compile("[xX]{2,}([\\d]{4})")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    private fun extractCardNumber(message: String): String? {
        val patterns = listOf(
            Pattern.compile("card\\s*(?:no\\.?)?\\s*(?:ending\\s*)?[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:debit|credit)\\s*card\\s*[xX*]*([\\d]{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("card\\s+ending\\s+([\\d]{4})", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    private fun extractCurrency(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("aed") || lower.contains("dirham") || lower.contains("د.إ") -> "AED"
            lower.contains("usd") || lower.contains("dollar") && !lower.contains("australian") -> "USD"
            lower.contains("eur") || lower.contains("euro") || lower.contains("€") -> "EUR"
            lower.contains("gbp") || lower.contains("pound") || lower.contains("£") -> "GBP"
            lower.contains("dubai") || lower.contains("uae") -> "AED"
            else -> "INR"
        }
    }
}
