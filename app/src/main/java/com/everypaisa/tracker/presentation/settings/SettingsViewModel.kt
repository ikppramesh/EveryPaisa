package com.everypaisa.tracker.presentation.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.dao.TransactionDao
import com.everypaisa.tracker.data.preferences.UserPreferencesManager
import com.everypaisa.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val preferencesManager: UserPreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val TAG = "SettingsViewModel"
    
    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
            Log.d(TAG, "🔒 Biometric lock ${if (enabled) "enabled" else "disabled"}")
        }
    }
    
    fun clearAllData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Clearing all transactions...")
                transactionDao.deleteAll()
                Log.d(TAG, "✅ All transactions deleted")
                onComplete(true)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to clear data: ${e.message}", e)
                onComplete(false)
            }
        }
    }
    
    fun exportData(onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📤 Exporting transactions...")
                
                // Get all transactions
                val transactions = transactionDao.getAllTransactionsSync()
                
                if (transactions.isEmpty()) {
                    Log.d(TAG, "⚠️ No transactions to export")
                    onComplete(null)
                    return@launch
                }
                
                // Create CSV content
                val csv = buildString {
                    // Header
                    appendLine("Date,Merchant,Amount,Category,Type,Bank,Account,SMS Body")
                    
                    // Data rows
                    transactions.forEach { txn ->
                        val smsBody = (txn.smsBody ?: "").replace("\"", "\"\"").replace("\n", " ")
                        val row = listOf(
                            txn.dateTime.toString(),
                            "\"${txn.merchantName.replace("\"", "\"\"")}\"",
                            txn.amount.toPlainString(),
                            txn.category,
                            txn.transactionType.name,
                            txn.bankName ?: "",
                            txn.accountLast4 ?: "",
                            "\"${smsBody}\"",
                        ).joinToString(",")
                        appendLine(row)
                    }
                }
                
                // Save to file
                val fileName = "everypaisa_transactions_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(null), fileName)
                file.writeText(csv)
                
                Log.d(TAG, "✅ Exported ${transactions.size} transactions to ${file.absolutePath}")
                onComplete(file)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to export data: ${e.message}", e)
                onComplete(null)
            }
        }
    }
}
