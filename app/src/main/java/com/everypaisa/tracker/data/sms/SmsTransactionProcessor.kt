package com.everypaisa.tracker.data.sms

import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.everypaisa.parser.BankParserFactory
import com.everypaisa.parser.TransactionType as ParserTransactionType
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.repository.CategoryRepository
import com.everypaisa.tracker.domain.repository.MerchantMappingRepository
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsTransactionProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantMappingRepository: MerchantMappingRepository
) {
    
    private val parserFactory = BankParserFactory()
    private val TAG = "SmsTransactionProcessor"
    
    suspend fun processAllSms(): Int {
        Log.d(TAG, "🚀 Starting SMS scan...")
        var totalSmsCount = 0
        var parsedCount = 0
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            ),
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            
            Log.d(TAG, "📱 SMS cursor count: ${it.count}")
            
            while (it.moveToNext()) {
                totalSmsCount++
                val smsId = it.getLong(idIndex)
                val sender = it.getString(addressIndex) ?: continue
                val body = it.getString(bodyIndex) ?: continue
                val dateMillis = it.getLong(dateIndex)
                
                if (totalSmsCount <= 5) {
                    Log.d(TAG, "Sample SMS $totalSmsCount - Sender: $sender, Body: ${body.take(50)}...")
                }
                
                val parsed = processMessage(sender, body, dateMillis, smsId)
                if (parsed) parsedCount++
            }
        }
        
        Log.d(TAG, "✅ SMS scan complete. Total: $totalSmsCount, Parsed: $parsedCount")
        return parsedCount
    }
    
    suspend fun processMessage(sender: String, message: String, dateMillis: Long? = null, smsId: Long? = null): Boolean {
        // Filter out non-transactional SMS
        if (isNonTransactionalSms(message)) {
            Log.d(TAG, "⏭️ Skipping non-transactional SMS from $sender: ${message.take(50)}...")
            return false
        }
        
        // Check for failed/reversed transactions
        if (isFailedTransaction(message)) {
            Log.d(TAG, "🔄 Detected failed/reversed transaction from $sender")
            handleFailedTransaction(sender, message, dateMillis, smsId)
            return false
        }
        
        val parsedTxn = parserFactory.parse(sender, message)
        
        if (parsedTxn == null) {
            return false
        }
        
        Log.d(TAG, "✅ Parsed: ${parsedTxn.transactionType} | ${parsedTxn.bankName} | ${parsedTxn.merchantName} | ₹${parsedTxn.amount} | acct:${parsedTxn.accountLast4} card:${parsedTxn.cardLast4}")
        
        try {
            // Generate hash to avoid duplicates
            val hash = generateHash(parsedTxn.rawMessage)
            
            // Auto-categorize
            val category = categorizeMerchant(parsedTxn.merchantName, parsedTxn.transactionType)
            
            // Detect payment method from raw SMS
            val paymentMethod = detectPaymentMethod(parsedTxn.rawMessage)
            
            // Use SMS timestamp if available, otherwise use parser's dateTime
            val transactionDateTime = if (dateMillis != null && dateMillis > 0) {
                java.time.Instant.ofEpochMilli(dateMillis)
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
            } else {
                parsedTxn.dateTime
            }
            
            val entity = TransactionEntity(
                amount = parsedTxn.amount,
                merchantName = parsedTxn.merchantName,
                category = category,
                transactionType = mapTransactionType(parsedTxn.transactionType),
                dateTime = transactionDateTime,
                description = paymentMethod,
                smsBody = parsedTxn.rawMessage,
                smsSender = sender,
                smsId = smsId,
                bankName = parsedTxn.bankName,
                accountLast4 = parsedTxn.accountLast4 ?: parsedTxn.cardLast4,
                transactionHash = hash,
                currency = parsedTxn.currency
            )
            
            transactionRepository.insertTransaction(entity)
            Log.d(TAG, "💾 Saved transaction to database, date: $transactionDateTime, smsId: $smsId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving transaction: ${e.message}", e)
            return false
        }
    }
    
    private suspend fun categorizeMerchant(merchantName: String, type: ParserTransactionType): String {
        // First check merchant mappings
        val mappedCategory = merchantMappingRepository.getCategoryForMerchant(merchantName)
        if (mappedCategory != null) return mappedCategory
        
        // Use keyword-based categorization
        return when {
            // Food & Dining
            merchantName.contains("SWIGGY", ignoreCase = true) ||
            merchantName.contains("ZOMATO", ignoreCase = true) ||
            merchantName.contains("RESTAURANT", ignoreCase = true) ||
            merchantName.contains("CAFE", ignoreCase = true) ||
            merchantName.contains("DOMINOS", ignoreCase = true) ||
            merchantName.contains("PIZZA", ignoreCase = true) -> "Food & Dining"
            
            // Shopping
            merchantName.contains("AMAZON", ignoreCase = true) ||
            merchantName.contains("FLIPKART", ignoreCase = true) ||
            merchantName.contains("MYNTRA", ignoreCase = true) ||
            merchantName.contains("SHOP", ignoreCase = true) -> "Shopping"
            
            // Groceries
            merchantName.contains("BLINKIT", ignoreCase = true) ||
            merchantName.contains("BIGBASKET", ignoreCase = true) ||
            merchantName.contains("INSTAMART", ignoreCase = true) ||
            merchantName.contains("ZEPTO", ignoreCase = true) -> "Groceries"
            
            // Transportation
            merchantName.contains("UBER", ignoreCase = true) ||
            merchantName.contains("OLA", ignoreCase = true) ||
            merchantName.contains("RAPIDO", ignoreCase = true) ||
            merchantName.contains("PETROL", ignoreCase = true) ||
            merchantName.contains("FUEL", ignoreCase = true) -> "Transportation"
            
            // Entertainment
            merchantName.contains("NETFLIX", ignoreCase = true) ||
            merchantName.contains("PRIME", ignoreCase = true) ||
            merchantName.contains("SPOTIFY", ignoreCase = true) ||
            merchantName.contains("HOTSTAR", ignoreCase = true) ||
            merchantName.contains("BOOKMYSHOW", ignoreCase = true) -> "Entertainment"
            
            // Bills & Utilities
            merchantName.contains("ELECTRICITY", ignoreCase = true) ||
            merchantName.contains("WATER", ignoreCase = true) ||
            merchantName.contains("GAS", ignoreCase = true) ||
            merchantName.contains("BROADBAND", ignoreCase = true) ||
            merchantName.contains("AIRTEL", ignoreCase = true) ||
            merchantName.contains("JIO", ignoreCase = true) ||
            merchantName.contains("VODAFONE", ignoreCase = true) -> "Bills & Utilities"
            
            // Income types
            type == ParserTransactionType.CREDIT -> {
                when {
                    merchantName.contains("SALARY", ignoreCase = true) -> "Salary"
                    merchantName.contains("REFUND", ignoreCase = true) -> "Refunds"
                    merchantName.contains("CASHBACK", ignoreCase = true) -> "Cashback"
                    merchantName.contains("INTEREST", ignoreCase = true) -> "Interest"
                    else -> "Income"
                }
            }
            
            else -> "Others"
        }
    }
    
    private fun mapTransactionType(type: ParserTransactionType): TransactionType {
        return when (type) {
            ParserTransactionType.DEBIT -> TransactionType.EXPENSE
            ParserTransactionType.CREDIT -> TransactionType.INCOME
            ParserTransactionType.REFUND -> TransactionType.CREDIT
            ParserTransactionType.TRANSFER -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }
    }
    
    private fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun detectPaymentMethod(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("upi") -> "UPI"
            lower.contains("credit card") || lower.contains("cr card") || lower.contains("cc ending") -> "Credit Card"
            lower.contains("debit card") || lower.contains("dr card") -> "Debit Card"
            lower.contains("neft") -> "NEFT"
            lower.contains("imps") -> "IMPS"
            lower.contains("rtgs") -> "RTGS"
            lower.contains("netbanking") || lower.contains("net banking") -> "Net Banking"
            lower.contains("atm") || lower.contains("cash withdrawal") -> "ATM"
            lower.contains("card") -> "Card"
            lower.contains("nach") || lower.contains("mandate") -> "Auto-debit"
            else -> ""
        }
    }
    
    /**
     * Filter out non-transactional SMS like OTP, balance inquiries, limit changes, promotional messages
     */
    private fun isNonTransactionalSms(message: String): Boolean {
        val lower = message.lowercase()
        
        // OTP and verification codes
        if (lower.contains("otp") || 
            lower.contains("one time password") || 
            lower.contains("verification code") ||
            lower.contains("verify") ||
            lower.contains("authentication code") ||
            lower.contains("security code") ||
            lower.contains("do not share")) {
            return true
        }
        
        // Credit limit changes (not actual transactions)
        if ((lower.contains("limit") && (
                lower.contains("increased") || 
                lower.contains("decreased") || 
                lower.contains("changed") ||
                lower.contains("enhanced") ||
                lower.contains("revised") ||
                lower.contains("updated") ||
                lower.contains("new limit") ||
                lower.contains("credit limit is") ||
                lower.contains("limit has been") ||
                lower.contains("limit now")
            ))) {
            return true
        }
        
        // Balance inquiry (not a transaction)
        if ((lower.contains("balance") || lower.contains("bal")) && (
                lower.contains("available") || 
                lower.contains("current") ||
                lower.contains("is rs") ||
                lower.contains("is inr") ||
                lower.contains("avl bal") ||
                lower.contains("bal is") ||
                lower.contains("outstanding") ||
                lower.contains("minimum balance")
            )) {
            return true
        }
        
        // Account statements
        if (lower.contains("statement") || 
            lower.contains("e-statement") ||
            lower.contains("monthly statement") ||
            lower.contains("account summary")) {
            return true
        }
        
        // Promotional and marketing messages
        if (lower.contains("download app") || 
            lower.contains("install app") ||
            lower.contains("click here") ||
            lower.contains("visit us") ||
            lower.contains("offer valid") ||
            lower.contains("promotional") ||
            lower.contains("earn rewards") ||
            lower.contains("cashback on") ||
            lower.contains("special offer") ||
            lower.contains("limited time") ||
            lower.contains("subscribe") ||
            lower.contains("t&c apply") ||
            lower.contains("terms apply")) {
            return true
        }
        
        // Reminders and notifications (not transactions)
        if (lower.contains("reminder") || 
            lower.contains("due date") ||
            lower.contains("payment due") ||
            lower.contains("bill due") ||
            lower.contains("overdue") ||
            lower.contains("please pay") ||
            lower.contains("pay now")) {
            return true
        }
        
        // Welcome messages
        if (lower.contains("welcome to") || 
            lower.contains("thank you for") ||
            lower.contains("congratulations")) {
            return true
        }
        
        return false
    }
    
    /**
     * Check if transaction failed or was reversed
     */
    private fun isFailedTransaction(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("failed") || 
               lower.contains("declined") || 
               lower.contains("unsuccessful") ||
               lower.contains("could not be") ||
               lower.contains("not successful") ||
               lower.contains("transaction failed") ||
               lower.contains("payment failed") ||
               lower.contains("reversed") ||
               lower.contains("reversal") ||
               lower.contains("refunded") ||
               lower.contains("credited back")
    }
    
    /**
     * Handle failed transaction by finding and removing the original expense,
     * then adding a refund transaction
     */
    private suspend fun handleFailedTransaction(sender: String, message: String, dateMillis: Long?, smsId: Long?) {
        try {
            // Try to parse the failed transaction to get amount and merchant details
            val parsedTxn = parserFactory.parse(sender, message)
            if (parsedTxn == null) {
                Log.d(TAG, "⚠️ Could not parse failed transaction details")
                return
            }
            
            Log.d(TAG, "🔍 Looking for original transaction: ${parsedTxn.merchantName} ₹${parsedTxn.amount}")
            
            // Search for matching expense in last 30 days
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val amountDouble = parsedTxn.amount.toDouble()
            val matchingTransactions = transactionRepository.getTransactionsByAmountRange(
                amountDouble - 0.01, 
                amountDouble + 0.01,
                thirtyDaysAgo
            )
            
            // Find exact match by amount, bank, and account
            val originalTxn = matchingTransactions.firstOrNull { txn ->
                txn.amount == parsedTxn.amount &&
                txn.bankName == parsedTxn.bankName &&
                txn.transactionType == TransactionType.EXPENSE &&
                (txn.accountLast4 == parsedTxn.accountLast4 || txn.accountLast4 == parsedTxn.cardLast4)
            }
            
            if (originalTxn != null) {
                // Delete the original failed transaction
                transactionRepository.deleteTransaction(originalTxn.id)
                Log.d(TAG, "🗑️ Deleted original failed transaction: ${originalTxn.merchantName} ₹${originalTxn.amount}")
                
                // Create refund transaction
                val transactionDateTime = if (dateMillis != null && dateMillis > 0) {
                    java.time.Instant.ofEpochMilli(dateMillis)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
                } else {
                    parsedTxn.dateTime
                }
                
                val refundEntity = TransactionEntity(
                    amount = parsedTxn.amount,
                    merchantName = "${parsedTxn.merchantName} - Refund",
                    category = "Refunds",
                    transactionType = TransactionType.INCOME,
                    dateTime = transactionDateTime,
                    description = "Failed transaction refund",
                    smsBody = message,
                    smsSender = sender,
                    smsId = smsId,
                    bankName = parsedTxn.bankName,
                    accountLast4 = parsedTxn.accountLast4 ?: parsedTxn.cardLast4,
                    transactionHash = generateHash(message + "_refund"),
                    currency = parsedTxn.currency
                )
                
                transactionRepository.insertTransaction(refundEntity)
                Log.d(TAG, "✅ Created refund transaction: ${refundEntity.merchantName} ₹${refundEntity.amount}")
            } else {
                Log.d(TAG, "⚠️ Could not find matching original transaction to reverse")
                // Still create refund transaction even if original not found
                val transactionDateTime = if (dateMillis != null && dateMillis > 0) {
                    java.time.Instant.ofEpochMilli(dateMillis)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
                } else {
                    parsedTxn.dateTime
                }
                
                val refundEntity = TransactionEntity(
                    amount = parsedTxn.amount,
                    merchantName = "${parsedTxn.merchantName} - Refund",
                    category = "Refunds",
                    transactionType = TransactionType.INCOME,
                    dateTime = transactionDateTime,
                    description = "Refund/Reversal",
                    smsBody = message,
                    smsSender = sender,
                    smsId = smsId,
                    bankName = parsedTxn.bankName,
                    accountLast4 = parsedTxn.accountLast4 ?: parsedTxn.cardLast4,
                    transactionHash = generateHash(message + "_refund"),
                    currency = parsedTxn.currency
                )
                
                transactionRepository.insertTransaction(refundEntity)
                Log.d(TAG, "✅ Created standalone refund transaction: ${refundEntity.merchantName} ₹${refundEntity.amount}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling failed transaction: ${e.message}", e)
        }
    }
}
