package com.everypaisa.tracker.presentation.uae

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.everypaisa.tracker.presentation.home.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UAEHomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: UAEHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("🇦🇪 UAE Banking")
                        Text(
                            "Emirates NBD • ADCB • FAB • ENBD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UAEHomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
            }
            is UAEHomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Period Selector
                    item {
                        PeriodNavigationBar(
                            period = state.currentPeriod,
                            onPrevious = { viewModel.goToPreviousPeriod() },
                            onNext = { viewModel.goToNextPeriod() }
                        )
                    }
                    
                    // Multi-Currency Summary for UAE
                    item {
                        state.multiCurrencySummary.let { summary ->
                            MultiCurrencySummaryCard(
                                multiCurrencySummary = summary,
                                period = state.currentPeriod.format(),
                                periodType = state.currentPeriod.type
                            )
                        }
                    }
                    
                    // Quick Stats Row (using filtered summary)
                    item {
                        val totalExpenses = state.multiCurrencySummary.inrSummary?.totalExpenses ?: java.math.BigDecimal.ZERO
                        val transactionCount = state.transactions.size
                        QuickStatsRow(
                            totalSpent = totalExpenses,
                            transactionCount = transactionCount
                        )
                    }
                    
                    // Transactions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "UAE Transactions (${state.transactions.size})",
                                style = MaterialTheme.typography.titleLarge
                            )
                            TextButton(onClick = onNavigateToTransactions) {
                                Text("View All")
                                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    // Transaction List
                    items(state.transactions) { transaction ->
                        EnhancedTransactionCard(
                            transactionId = transaction.id,
                            merchantName = transaction.merchantName,
                            amount = transaction.amount,
                            category = transaction.category,
                            dateTime = transaction.dateTime.format(
                                DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
                            ),
                            transactionType = transaction.transactionType,
                            bankName = transaction.bankName ?: "Unknown",
                            accountLast4 = transaction.accountLast4,
                            paymentMethod = transaction.description ?: "",
                            currency = transaction.currency,
                            smsId = transaction.smsId,
                            smsBody = transaction.smsBody,
                            smsSender = transaction.smsSender,
                            isAtmWithdrawal = transaction.isAtmWithdrawal,
                            isInterAccountTransfer = transaction.isInterAccountTransfer,
                            onMarkAsAtm = { id, flag -> viewModel.markTransactionAsAtm(id, flag) },
                            onMarkAsInterAccount = { id, flag -> viewModel.markTransactionAsInterAccount(id, flag) }
                        )
                    }
                    
                    // Empty state
                    if (state.transactions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🇦🇪 No UAE Transactions",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Transactions in AED, SAR, QAR, OMR, KWD, BHD currencies\nfrom UAE banks will appear here",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is UAEHomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Text(
                        state.message,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
            }
        }
    }
}
