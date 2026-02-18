package com.everypaisa.tracker.presentation.regional

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.everypaisa.tracker.presentation.home.EnhancedTransactionCard
import com.everypaisa.tracker.presentation.home.MultiCurrencySummaryCard
import com.everypaisa.tracker.presentation.home.PeriodNavigationBar
import java.time.format.DateTimeFormatter

/**
 * Generic regional screen for any country/currency group.
 * No own TopAppBar — it's embedded inside MainScreenWithTabs which owns the header.
 *
 * Adding a new country is as simple as adding a CountryTab entry in MainScreenWithTabs.
 */
@Composable
fun RegionalHomeScreen(
    flag: String,
    regionName: String,
    currencies: Set<String>,          // e.g. setOf("AED","SAR","QAR") or setOf("USD")
    bankHint: String,                 // e.g. "Emirates NBD • ADCB • FAB"
    onNavigateToTransactions: () -> Unit,
    // Unique ViewModel per region via key — no extra params needed
    viewModel: RegionalHomeViewModel = hiltViewModel(key = currencies.sorted().joinToString(","))
) {
    // Push the currency list into the ViewModel (once / when changed)
    LaunchedEffect(currencies) {
        viewModel.setCurrencies(currencies)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is RegionalHomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is RegionalHomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period navigation bar
                item {
                    PeriodNavigationBar(
                        period = state.currentPeriod,
                        onPrevious = { viewModel.goToPreviousPeriod() },
                        onNext = { viewModel.goToNextPeriod() }
                    )
                }

                // Multi-currency summary card
                item {
                    MultiCurrencySummaryCard(
                        multiCurrencySummary = state.multiCurrencySummary,
                        period = state.currentPeriod.format(),
                        periodType = state.currentPeriod.type
                    )
                }

                // Section header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$flag Transactions (${state.transactions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = onNavigateToTransactions) {
                            Text("View All")
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Transaction list
                if (state.transactions.isNotEmpty()) {
                    items(state.transactions, key = { it.id }) { transaction ->
                        EnhancedTransactionCard(
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
                            smsSender = transaction.smsSender
                        )
                    }
                } else {
                    // Empty state
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    flag,
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No $regionName Transactions",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Transactions in ${currencies.joinToString(" • ")} will appear here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (bankHint.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        bankHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is RegionalHomeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
