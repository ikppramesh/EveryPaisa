package com.everypaisa.tracker.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.everypaisa.tracker.R
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
    showTopBar: Boolean = true,
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
        contentWindowInsets = if (showTopBar) ScaffoldDefaults.contentWindowInsets else WindowInsets(0),
        topBar = {
            if (showTopBar) LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.app_name),
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
            if (showTopBar) FloatingActionButton(
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
                val availableBanks by viewModel.availableBanks.collectAsState()
                val selectedBank by viewModel.selectedBank.collectAsState()
                val filteredTxns by viewModel.filteredTransactions.collectAsState()
                val filteredSummary by viewModel.filteredSummary.collectAsState()

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
                                multiCurrencySummary = filteredSummary,
                                period = state.currentPeriod.format(),
                                periodType = state.currentPeriod.type
                            )
                        }
                        
                        // Quick Stats Row (using filtered summary respecting bank selection)
                        item {
                            val totalExpenses = filteredSummary.inrSummary?.totalExpenses ?: BigDecimal.ZERO
                            val transactionCount = filteredTxns.size
                            QuickStatsRow(
                                totalSpent = totalExpenses,
                                transactionCount = transactionCount
                            )
                        }

                        // Bank Filter Chips
                        if (availableBanks.isNotEmpty()) {
                            item {
                                BankFilterChips(
                                    banks = availableBanks,
                                    selectedBank = selectedBank,
                                    onBankSelected = { viewModel.setSelectedBank(it) }
                                )
                            }
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
                                    val label = if (selectedBank != null)
                                        "$selectedBank (${filteredTxns.size})"
                                    else
                                        "Transactions (${state.transactions.size})"
                                    Text(
                                        label,
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

                            // Transaction List (filtered by selected bank)
                            items(filteredTxns) { transaction ->
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BankFilterChips(
    banks: List<String>,
    selectedBank: String?,
    onBankSelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedBank == null,
                onClick = { onBankSelected(null) },
                label = { Text("All Banks") }
            )
        }
        items(banks) { bank ->
            FilterChip(
                selected = selectedBank == bank,
                onClick = { onBankSelected(if (selectedBank == bank) null else bank) },
                label = { Text(bank) }
            )
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
                    
                    // Right: International Summaries with Currency Symbols
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        multiCurrencySummary.internationalSummaries.forEach { intlSummary ->
                            // Show currency symbol instead of "International" text
                            Text(
                                "${intlSummary.currencySymbol} ${intlSummary.currency}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedTransactionCard(
    transactionId: Long = 0L,
    merchantName: String,
    amount: BigDecimal,
    category: String,
    dateTime: String,
    transactionType: TransactionType,
    bankName: String,
    accountLast4: String? = null,
    paymentMethod: String = "",
    currency: String = "INR",
    smsId: Long? = null,
    smsBody: String? = null,
    smsSender: String? = null,
    isAtmWithdrawal: Boolean = false,
    isInterAccountTransfer: Boolean = false,
    onMarkAsAtm: (Long, Boolean) -> Unit = { _, _ -> },
    onMarkAsInterAccount: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val isExpense = transactionType == TransactionType.EXPENSE
    val isTransfer = transactionType == TransactionType.TRANSFER
    val currencySymbol = CurrencySummary.getCurrencySymbol(currency)
    
    var showSmsDialog by remember { mutableStateOf(false) }
    var showMarkMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMarkMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAtmWithdrawal -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                isInterAccountTransfer -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        // Long-press context menu
        DropdownMenu(
            expanded = showMarkMenu,
            onDismissRequest = { showMarkMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalAtm,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isAtmWithdrawal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAtmWithdrawal) "Unmark ATM Withdrawal" else "Mark as ATM Withdrawal")
                    }
                },
                onClick = {
                    onMarkAsAtm(transactionId, !isAtmWithdrawal)
                    showMarkMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isInterAccountTransfer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isInterAccountTransfer) "Unmark Inter-account Transfer" else "Mark as Inter-account Transfer")
                    }
                },
                onClick = {
                    onMarkAsInterAccount(transactionId, !isInterAccountTransfer)
                    showMarkMenu = false
                }
            )
        }

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

                    // ATM / Inter-account badges
                    if (isAtmWithdrawal || isInterAccountTransfer) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isAtmWithdrawal) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocalAtm,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "ATM",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            if (isInterAccountTransfer) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SwapHoriz,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "Transfer",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
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
                    // SMS Details Icon
                    if (smsBody != null) {
                        IconButton(
                            onClick = { showSmsDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = "View SMS Details",
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
    
    // SMS Details Dialog
    if (showSmsDialog && smsBody != null) {
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SMS Details", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Transaction Info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                merchantName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${if (isExpense) "-" else "+"}$currencySymbol${String.format("%,.2f", amount.toDouble())}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                dateTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // SMS Sender
                    if (smsSender != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "From",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    smsSender,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // SMS Body
                    Column {
                        Text(
                            "Message",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                smsBody,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSmsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

fun getCategoryIcon(category: String) = when (category.lowercase()) {
    "food & dining", "food", "dining" -> Icons.Default.Restaurant
    "groceries" -> Icons.Default.LocalGroceryStore
    "shopping" -> Icons.Default.ShoppingBag
    "transportation", "fuel", "transport" -> Icons.Default.DirectionsCar
    "bills & utilities", "bills", "utilities" -> Icons.Default.Receipt
    "entertainment" -> Icons.Default.Movie
    "healthcare", "medical", "health" -> Icons.Default.LocalHospital
    "travel", "vacation" -> Icons.Default.Flight
    "education", "courses" -> Icons.Default.School
    "subscriptions", "subscription" -> Icons.Default.Subscriptions
    "personal care", "beauty", "salon" -> Icons.Default.Spa
    "investments", "investment", "stocks" -> Icons.Default.TrendingUp
    "salary", "income" -> Icons.Default.AccountBalance
    "refunds", "refund" -> Icons.Default.MoneyOff
    "cashback" -> Icons.Default.Paid
    "transfers", "transfer" -> Icons.Default.SwapHoriz
    "rent", "housing" -> Icons.Default.Home
    "insurance" -> Icons.Default.Security
    "gym", "fitness", "sports" -> Icons.Default.FitnessCenter
    "gifts" -> Icons.Default.CardGiftcard
    "charity", "donation" -> Icons.Default.VolunteerActivism
    "pets" -> Icons.Default.Pets
    "coffee", "tea" -> Icons.Default.LocalCafe
    "alcohol", "drinks", "bar" -> Icons.Default.LocalBar
    "phone", "mobile", "recharge" -> Icons.Default.PhoneAndroid
    "internet", "wifi", "broadband" -> Icons.Default.Wifi
    "streaming", "ott" -> Icons.Default.LiveTv
    "games", "gaming" -> Icons.Default.SportsEsports
    "books", "reading" -> Icons.Default.MenuBook
    "clothes", "fashion" -> Icons.Default.Checkroom
    "electronics" -> Icons.Default.Devices
    "furniture", "home decor" -> Icons.Default.Weekend
    "pharmacy", "medicine" -> Icons.Default.LocalPharmacy
    "parking" -> Icons.Default.LocalParking
    "laundry", "dry cleaning" -> Icons.Default.LocalLaundryService
    "others", "other" -> Icons.Default.EmojiEmotions
    else -> Icons.Default.EmojiEmotions
}

fun getCategoryColor(category: String) = when (category.lowercase()) {
    "food & dining", "food", "dining" -> Color(0xFFFC8019) // Orange
    "groceries" -> Color(0xFF5AC85A) // Green
    "shopping" -> Color(0xFFE91E63) // Pink
    "transportation", "fuel", "transport" -> Color(0xFF29B6F6) // Blue
    "bills & utilities", "bills", "utilities" -> Color(0xFFFFA726) // Amber
    "entertainment" -> Color(0xFFAB47BC) // Purple
    "healthcare", "medical", "health" -> Color(0xFFEF5350) // Red
    "travel", "vacation" -> Color(0xFF26C6DA) // Cyan
    "education", "courses" -> Color(0xFF42A5F5) // Light Blue
    "subscriptions", "subscription" -> Color(0xFF7E57C2) // Deep Purple
    "personal care", "beauty", "salon" -> Color(0xFFEC407A) // Hot Pink
    "investments", "investment", "stocks" -> Color(0xFF66BB6A) // Light Green
    "salary", "income" -> Color(0xFF4CAF50) // Strong Green
    "refunds", "refund" -> Color(0xFF8BC34A) // Light Green
    "cashback" -> Color(0xFFCDDC39) // Lime
    "transfers", "transfer" -> Color(0xFF78909C) // Blue Grey
    "rent", "housing" -> Color(0xFF9C27B0) // Purple
    "insurance" -> Color(0xFF3F51B5) // Indigo
    "gym", "fitness", "sports" -> Color(0xFFFF5722) // Deep Orange
    "gifts" -> Color(0xFFE91E63) // Pink
    "charity", "donation" -> Color(0xFF9C27B0) // Purple
    "pets" -> Color(0xFFFF9800) // Orange
    "coffee", "tea" -> Color(0xFF795548) // Brown
    "alcohol", "drinks", "bar" -> Color(0xFFFFC107) // Amber
    "phone", "mobile", "recharge" -> Color(0xFF00BCD4) // Cyan
    "internet", "wifi", "broadband" -> Color(0xFF2196F3) // Blue
    "streaming", "ott" -> Color(0xFF673AB7) // Deep Purple
    "games", "gaming" -> Color(0xFF9C27B0) // Purple
    "books", "reading" -> Color(0xFF607D8B) // Blue Grey
    "clothes", "fashion" -> Color(0xFFE91E63) // Pink
    "electronics" -> Color(0xFF00BCD4) // Cyan
    "furniture", "home decor" -> Color(0xFF795548) // Brown
    "pharmacy", "medicine" -> Color(0xFFF44336) // Red
    "parking" -> Color(0xFF9E9E9E) // Grey
    "laundry", "dry cleaning" -> Color(0xFF03A9F4) // Light Blue
    "others", "other" -> Color(0xFFBDBDBD) // Light Grey
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
