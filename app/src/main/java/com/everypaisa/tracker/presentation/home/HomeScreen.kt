package com.everypaisa.tracker.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.everypaisa.tracker.domain.model.Country
import com.everypaisa.tracker.worker.OptimizedSmsReaderWorker
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var showCountrySelector by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true &&
                     permissions[Manifest.permission.RECEIVE_SMS] == true
        
        if (granted) {
            // Start scanning
            scope.launch {
                isScanning = true
                snackbarHostState.showSnackbar("Scanning SMS messages...")
                
                val workRequest = OneTimeWorkRequestBuilder<OptimizedSmsReaderWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)
                
                // Observe work status
                WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(workRequest.id)
                    .observeForever { workInfo ->
                        when (workInfo?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                isScanning = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("✅ SMS scan complete!")
                                    viewModel.refreshTransactions()
                                }
                            }
                            WorkInfo.State.FAILED -> {
                                isScanning = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("❌ Scan failed. Try again.")
                                }
                            }
                            WorkInfo.State.RUNNING -> {
                                isScanning = true
                            }
                            else -> {}
                        }
                    }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("SMS permissions required to scan transactions")
            }
        }
    }
    
    fun startScan() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            scope.launch {
                isScanning = true
                snackbarHostState.showSnackbar("Scanning SMS messages...")
                
                val workRequest = OneTimeWorkRequestBuilder<OptimizedSmsReaderWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)
                
                // Show success after a delay (WorkManager is async)
                kotlinx.coroutines.delay(3000)
                isScanning = false
                snackbarHostState.showSnackbar("✅ SMS scan complete! Refresh to see transactions.")
                viewModel.refreshTransactions()
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
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("EveryPaisa")
                        Spacer(modifier = Modifier.weight(1f))
                        // Country selector in top bar
                        Button(
                            onClick = { showCountrySelector = true },
                            modifier = Modifier.heightIn(max = 40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${selectedCountry.flag} ${selectedCountry.code}", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                        }
                    }
                },
                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { startScan() },
                icon = { 
                    Icon(
                        if (isScanning) Icons.Default.HourglassEmpty else Icons.Default.Sync,
                        null
                    ) 
                },
                text = { Text(if (isScanning) "Scanning..." else "Scan SMS") }
            )
        }
    ) { paddingValues ->
        // Country selector dropdown
        if (showCountrySelector) {
            AlertDialog(
                onDismissRequest = { showCountrySelector = false },
                title = { Text("Select Country") },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCountries) { country ->
                            TextButton(
                                onClick = {
                                    viewModel.setSelectedCountry(country)
                                    showCountrySelector = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${country.flag} ${country.label} (${country.code})")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCountrySelector = false }) {
                        Text("Close")
                    }
                }
            )
        }
        
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            is HomeUiState.Success -> {
                if (state.transactions.isEmpty()) {
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
                            Text(
                                text = "No transactions for ${state.selectedCountry.label}",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tap 'Scan SMS' to import your financial transactions",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Summary Card
                        item {
                            MonthSummaryCard(
                                income = state.monthSummary.totalIncome,
                                expense = state.monthSummary.totalExpenses,
                                count = state.monthSummary.transactionCount,
                                currency = state.selectedCountry.primaryCurrency
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Recent Transactions
                        item {
                            Text(
                                "Recent Transactions (${state.selectedCountry.label})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(state.transactions) { transaction ->
                            TransactionCard(
                                merchantName = transaction.merchantName,
                                amount = transaction.amount,
                                category = transaction.category,
                                currency = transaction.currency,
                                dateTime = transaction.dateTime.format(
                                    DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
                                ),
                                isExpense = transaction.transactionType.name == "EXPENSE"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSummaryCard(income: BigDecimal, expense: BigDecimal, count: Int, currency: String = "INR") {
    val currencySymbol = com.everypaisa.tracker.domain.model.CurrencySummary.getCurrencySymbol(currency)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "This Month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$currencySymbol${income.toPlainString()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        "Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$currencySymbol${expense.toPlainString()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$count transactions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TransactionCard(
    merchantName: String,
    amount: BigDecimal,
    category: String,
    currency: String = "INR",
    dateTime: String,
    isExpense: Boolean
) {
    val currencySymbol = com.everypaisa.tracker.domain.model.CurrencySummary.getCurrencySymbol(currency)
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    merchantName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "$category • $dateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${if (isExpense) "-" else "+"}$currencySymbol${amount.toPlainString()}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpense) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}
