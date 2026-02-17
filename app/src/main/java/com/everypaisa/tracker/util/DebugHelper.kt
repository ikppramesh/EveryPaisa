package com.everypaisa.tracker.util

import android.util.Log
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.repository.TransactionRepository
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class DebugHelper @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    private val TAG = "DebugHelper"
    
    suspend fun insertTestTransactions() {
        Log.d(TAG, "🧪 Inserting test transactions...")
        
        val testTransactions = listOf(
            TransactionEntity(
                amount = BigDecimal("1250.00"),
                merchantName = "Swiggy",
                category = "Food & Dining",
                transactionType = TransactionType.EXPENSE,
                dateTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                smsBody = "Test transaction 1",
                smsSender = "TEST",
                bankName = "HDFC Bank",
                accountLast4 = "1234",
                transactionHash = "test_hash_1_${System.currentTimeMillis()}"
            ),
            TransactionEntity(
                amount = BigDecimal("50000.00"),
                merchantName = "Salary Credit",
                category = "Salary",
                transactionType = TransactionType.INCOME,
                dateTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().minusDays(1),
                smsBody = "Test transaction 2",
                smsSender = "TEST",
                bankName = "HDFC Bank",
                accountLast4 = "1234",
                transactionHash = "test_hash_2_${System.currentTimeMillis()}"
            ),
            TransactionEntity(
                amount = BigDecimal("3500.00"),
                merchantName = "Amazon",
                category = "Shopping",
                transactionType = TransactionType.EXPENSE,
                dateTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().minusHours(5),
                smsBody = "Test transaction 3",
                smsSender = "TEST",
                bankName = "ICICI Bank",
                accountLast4 = "5678",
                transactionHash = "test_hash_3_${System.currentTimeMillis()}"
            )
        )
        
        testTransactions.forEach { transaction ->
            try {
                val id = transactionRepository.insertTransaction(transaction)
                Log.d(TAG, "✅ Inserted test transaction: ${transaction.merchantName}, ID: $id")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to insert test transaction: ${e.message}", e)
            }
        }
        
        Log.d(TAG, "🧪 Test transactions insertion complete")
    }
}
