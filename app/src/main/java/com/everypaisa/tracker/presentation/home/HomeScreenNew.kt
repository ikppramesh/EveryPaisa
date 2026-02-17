package com.everypaisa.tracker.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.CurrencySummary
import com.everypaisa.tracker.domain.model.DashboardPeriod
import com.everypaisa.tracker.domain.model.MultiCurrencySummary
import com.everypaisa.tracker.domain.model.Period
import com.everypaisa.tracker.worker.OptimizedSmsReaderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNew(
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true &&
                     permissions[Manifest.permission.RECEIVE_SMS] == true
        
        if (granted) {
            startSmsScanning(context, scope, snackbarHostState, viewModel) { scanning ->
                isScanning = scanning
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("⚠️ SMS permissions required to scan transactions")
            }
        }
    }
    
    fun startScan() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            startSmsScanning(context, scope, snackbarHostState, viewModel) { scanning ->
                isScanning = scanning
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
                )
            )
        }
    }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "EveryPaisa",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    if (isScanning || isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(onClick = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.refreshTransactions()
                            kotlinx.coroutines.delay(500)
                            isRefreshing = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.BarChart, "Dashboard")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { startScan() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    if (isScanning) Icons.Default.HourglassEmpty else Icons.Default.Sync,
                    contentDescription = "Scan SMS"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading transactions...")
                    }
                }
            }
            
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.refreshTransactions() }) {
                            Text("Try Again")
                        }
                    }
                }
            }
            
            is HomeUiState.Success -> {
                val currentPeriod by viewModel.selectedPeriod.collectAsState()

                if (state.transactions.isEmpty() && currentPeriod.type == DashboardPeriod.MONTHLY && currentPeriod == Period.currentMonth()) {
                    EmptyState(
                        onScanClick = { startScan() },
                        isScanning = isScanning,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Period Type Selector (Daily / Weekly / Monthly / Yearly)
                        item {
                            PeriodTypeSelector(
                                selectedType = currentPeriod.type,
                                onTypeSelected = { viewModel.selectPeriodType(it) }
                            )
                        }

                        // Period Navigation (← Feb 2026 →)
                        item {
                            PeriodNavigationBar(
                                period = currentPeriod,
                                onPrevious = { viewModel.goToPreviousPeriod() },
                                onNext = { viewModel.goToNextPeriod() }
                            )
                        }

                        // Hero Summary Card - Multi-Currency
                        item {
                            MultiCurrencySummaryCard(
                                multiCurrencySummary = state.multiCurrencySummary,
                                period = state.currentPeriod.format(),
                                periodType = state.currentPeriod.type
                            )
                        }
                        
                        // Quick Stats Row
                        item {
                            QuickStatsRow(
                                totalSpent = state.monthSummary.totalExpenses,
                                transactionCount = state.monthSummary.transactionCount
                            )
                        }

                        if (state.transactions.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "No transactions for this period",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            // Section Header
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Transactions (${state.transactions.size})",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = onNavigateToTransactions) {
                                        Text("View All")
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Transaction List
                            items(state.transactions) { transaction ->
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
                                    smsId = transaction.smsId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    onScanClick: () -> Unit,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap the scan button to read your bank SMS and automatically track your expenses",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onScanClick,
                enabled = !isScanning,
                modifier = Modifier.height(56.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Scanning SMS...")
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan SMS Now")
                }
            }
        }
    }
}

@Composable
fun PeriodTypeSelector(
    selectedType: DashboardPeriod,
    onTypeSelected: (DashboardPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DashboardPeriod.entries.forEach { type ->
            val isSelected = type == selectedType
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        type.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun PeriodNavigationBar(
    period: Period,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = period.format(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(
            onClick = onNext,
            enabled = !period.isFuture()
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = if (!period.isFuture()) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MultiCurrencySummaryCard(
    multiCurrencySummary: MultiCurrencySummary,
    period: String,
    periodType: DashboardPeriod = DashboardPeriod.MONTHLY
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                period,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // If we have international currencies, show split view
            if (multiCurrencySummary.hasInternational) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left: Indian (INR) Summary
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "🇮🇳 Indian",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        multiCurrencySummary.inrSummary?.let { inr ->
                            CurrencySummaryView(
                                summary = inr,
                                isCompact = true
                            )
                        } ?: run {
                            Text(
                                "No transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(120.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    
                    // Right: International Summaries
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "🌍 International",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        multiCurrencySummary.internationalSummaries.forEach { intlSummary ->
                            CurrencySummaryView(
                                summary = intlSummary,
                                isCompact = true
                            )
                            if (intlSummary != multiCurrencySummary.internationalSummaries.last()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            } else {
                // Only INR - show single currency view
                multiCurrencySummary.inrSummary?.let { inr ->
                    CurrencySummaryView(
                        summary = inr,
                        isCompact = false,
                        periodType = periodType
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencySummaryView(
    summary: CurrencySummary,
    isCompact: Boolean,
    periodType: DashboardPeriod = DashboardPeriod.MONTHLY
) {
    Column {
        // Net Amount
        Text(
            "${summary.currencySymbol} ${String.format("%,.2f", summary.netAmount.toDouble())}",
            style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        if (!isCompact) {
            val netLabel = when (periodType) {
                DashboardPeriod.DAILY -> "Net balance today"
                DashboardPeriod.WEEKLY -> "Net balance this week"
                DashboardPeriod.MONTHLY -> "Net balance this month"
                DashboardPeriod.YEARLY -> "Net balance this year"
            }
            Text(
                netLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Income/Expense Row
        if (!isCompact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${summary.currencySymbol}${String.format("%,.2f", summary.totalIncome.toDouble())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Expenses",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${summary.currencySymbol}${String.format("%,.2f", summary.totalExpenses.toDouble())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            // Compact view
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${summary.currencySymbol}${String.format("%,.0f", summary.totalIncome.toDouble())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${summary.currencySymbol}${String.format("%,.0f", summary.totalExpenses.toDouble())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Transaction count for compact view
        if (isCompact) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${summary.transactionCount} transactions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun HeroSummaryCard(
    income: BigDecimal,
    expense: BigDecimal,
    count: Int,
    period: String,
    periodType: DashboardPeriod = DashboardPeriod.MONTHLY
) {
    val netLabel = when (periodType) {
        DashboardPeriod.DAILY -> "Net balance today"
        DashboardPeriod.WEEKLY -> "Net balance this week"
        DashboardPeriod.MONTHLY -> "Net balance this month"
        DashboardPeriod.YEARLY -> "Net balance this year"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                period,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            val netAmount = income.subtract(expense)
            Text(
                "₹ ${String.format("%,.2f", netAmount.toDouble())}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                netLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "₹${String.format("%,.2f", income.toDouble())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Expenses",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "₹${String.format("%,.2f", expense.toDouble())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "$count transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QuickStatsRow(totalSpent: BigDecimal, transactionCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$transactionCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "₹${String.format("%.0f", totalSpent.toDouble())}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "Total Spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EnhancedTransactionCard(
    merchantName: String,
    amount: BigDecimal,
    category: String,
    dateTime: String,
    transactionType: TransactionType,
    bankName: String,
    accountLast4: String? = null,
    paymentMethod: String = "",
    currency: String = "INR",
    smsId: Long? = null
) {
    val context = LocalContext.current
    val isExpense = transactionType == TransactionType.EXPENSE
    val isTransfer = transactionType == TransactionType.TRANSFER
    val currencySymbol = CurrencySummary.getCurrencySymbol(currency)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(category).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCategoryIcon(category),
                        contentDescription = null,
                        tint = getCategoryColor(category),
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Merchant name
                    Text(
                        merchantName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Category pill
                    Text(
                        category,
                        style = MaterialTheme.typography.labelSmall,
                        color = getCategoryColor(category),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Bank + Account/Card info row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val bankDetail = buildString {
                            append(bankName)
                            if (!accountLast4.isNullOrBlank()) {
                                append(" •• $accountLast4")
                            }
                        }
                        Text(
                            bankDetail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Payment method + date row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (paymentMethod.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 3.dp)
                            ) {
                                Text(
                                    paymentMethod,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            dateTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
            
            // Amount + SMS Link
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // SMS Link Icon
                    if (smsId != null) {
                        IconButton(
                            onClick = {
                                try {
                                    // Try to open specific SMS message
                                    val smsIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(android.net.Uri.parse("content://sms/$smsId"), "vnd.android-dir/mms-sms")
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }
                                    context.startActivity(smsIntent)
                                } catch (e: Exception) {
                                    android.util.Log.e("SMSLink", "Failed to open SMS: ${e.message}")
                                    // Fallback: Open SMS inbox
                                    try {
                                        val fallbackIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.messaging")
                                            ?: android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                                                addCategory(android.content.Intent.CATEGORY_APP_MESSAGING)
                                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                        context.startActivity(fallbackIntent)
                                    } catch (e2: Exception) {
                                        android.util.Log.e("SMSLink", "Fallback failed: ${e2.message}")
                                        // Last resort: Open default SMS app
                                        try {
                                            val defaultIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                setType("vnd.android-dir/mms-sms")
                                            }
                                            context.startActivity(defaultIntent)
                                        } catch (_: Exception) { }
                                    }
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = "View SMS",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        "${if (isExpense) "-" else if (isTransfer) "" else "+"}$currencySymbol${String.format("%,.2f", amount.toDouble())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isExpense -> Color(0xFFC62828)
                            isTransfer -> MaterialTheme.colorScheme.onSurface
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
                // Show currency code if not INR
                if (currency.uppercase() != "INR") {
                    Text(
                        currency.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    when {
                        isExpense -> "Spent"
                        isTransfer -> "Transfer"
                        else -> "Received"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun getCategoryIcon(category: String) = when (category.lowercase()) {
    "food & dining" -> Icons.Default.Restaurant
    "groceries" -> Icons.Default.LocalGroceryStore
    "shopping" -> Icons.Default.ShoppingBag
    "transportation" -> Icons.Default.DirectionsCar
    "bills & utilities" -> Icons.Default.Receipt
    "entertainment" -> Icons.Default.Movie
    "healthcare" -> Icons.Default.LocalHospital
    "travel" -> Icons.Default.Flight
    "salary" -> Icons.Default.AccountBalance
    else -> Icons.Default.Category
}

fun getCategoryColor(category: String) = when (category.lowercase()) {
    "food & dining" -> Color(0xFFFC8019)
    "groceries" -> Color(0xFF5AC85A)
    "shopping" -> Color(0xFFE91E63)
    "transportation" -> Color(0xFF29B6F6)
    "bills & utilities" -> Color(0xFFFFA726)
    "entertainment" -> Color(0xFFAB47BC)
    "healthcare" -> Color(0xFFEF5350)
    "travel" -> Color(0xFF26C6DA)
    "salary" -> Color(0xFF4CAF50)
    else -> Color(0xFFBDBDBD)
}

private fun startSmsScanning(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel,
    onScanningChange: (Boolean) -> Unit
) {
    scope.launch {
        onScanningChange(true)
        snackbarHostState.showSnackbar("📱 Scanning SMS messages...")
        
        val workRequest = OneTimeWorkRequestBuilder<OptimizedSmsReaderWorker>().build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(workRequest)
        
        // Poll WorkManager status until done (instead of hardcoded delay)
        var isComplete = false
        var attempts = 0
        while (!isComplete && attempts < 60) { // max 60 seconds
            delay(1000)
            attempts++
            
            val workInfos = workManager.getWorkInfoById(workRequest.id).get()
            if (workInfos != null) {
                when (workInfos.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        isComplete = true
                        onScanningChange(false)
                        snackbarHostState.showSnackbar("✅ Scan complete! Transactions updated automatically")
                    }
                    WorkInfo.State.FAILED -> {
                        isComplete = true
                        onScanningChange(false)
                        snackbarHostState.showSnackbar("❌ Scan failed. Try again.")
                    }
                    WorkInfo.State.CANCELLED -> {
                        isComplete = true
                        onScanningChange(false)
                        snackbarHostState.showSnackbar("⚠️ Scan cancelled")
                    }
                    else -> {
                        // Still running - continue polling
                    }
                }
            }
        }
        
        if (!isComplete) {
            onScanningChange(false)
            snackbarHostState.showSnackbar("⏰ Scan is taking long. Transactions will appear shortly.")
        }
    }
}
