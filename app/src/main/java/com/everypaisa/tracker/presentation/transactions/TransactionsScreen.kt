package com.everypaisa.tracker.presentation.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.everypaisa.tracker.data.entity.TransactionType
import com.everypaisa.tracker.domain.model.Country
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showCountrySelector by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Transactions")
                        Spacer(modifier = Modifier.weight(1f))
                        // Country selector in top bar
                        when (val state = uiState) {
                            is TransactionsUiState.Success -> {
                                Button(
                                    onClick = { showCountrySelector = true },
                                    modifier = Modifier.heightIn(max = 40.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${state.selectedCountry.flag}", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                                }
                            }
                            else -> {}
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Country selector dialog
        if (showCountrySelector && uiState is TransactionsUiState.Success) {
            val state = uiState as TransactionsUiState.Success
            AlertDialog(
                onDismissRequest = { showCountrySelector = false },
                title = { Text("Select Country") },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(Country.values().toList()) { country ->
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
            is TransactionsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is TransactionsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            
            is TransactionsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search transactions...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.search("")
                                }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )
                    
                    // Summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "₹${state.totalAmount.toPlainString()}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Count",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${state.transactions.size}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                    
                    // Transaction List
                    if (state.transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions found")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.transactions) { transaction ->
                                TransactionItem(
                                    transactionId = transaction.id,
                                    merchantName = transaction.merchantName,
                                    amount = transaction.amount,
                                    category = transaction.category,
                                    dateTime = transaction.dateTime.format(
                                        DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
                                    ),
                                    transactionType = transaction.transactionType,
                                    bankName = transaction.bankName,
                                    smsBody = transaction.smsBody,
                                    smsSender = transaction.smsSender,
                                    isAtmWithdrawal = transaction.isAtmWithdrawal,
                                    isInterAccountTransfer = transaction.isInterAccountTransfer,
                                    onMarkAsAtm = { id, flag -> viewModel.markTransactionAsAtm(id, flag) },
                                    onMarkAsInterAccount = { id, flag -> viewModel.markTransactionAsInterAccount(id, flag) },
                                    onMarkAsCredited = { id -> viewModel.markTransactionAsCredited(id) },
                                    onMarkAsDebited = { id -> viewModel.markTransactionAsDebited(id) },
                                    onClick = { onTransactionClick(transaction.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transactionId: Long = 0L,
    merchantName: String,
    amount: java.math.BigDecimal,
    category: String,
    dateTime: String,
    transactionType: TransactionType,
    bankName: String? = null,
    smsBody: String? = null,
    smsSender: String? = null,
    isAtmWithdrawal: Boolean = false,
    isInterAccountTransfer: Boolean = false,
    onMarkAsAtm: (Long, Boolean) -> Unit = { _, _ -> },
    onMarkAsInterAccount: (Long, Boolean) -> Unit = { _, _ -> },
    onMarkAsCredited: (Long) -> Unit = { _ -> },
    onMarkAsDebited: (Long) -> Unit = { _ -> },
    onClick: () -> Unit
) {
    val isExpense = transactionType == TransactionType.EXPENSE
    val isIncome = transactionType == TransactionType.INCOME || transactionType == TransactionType.CREDIT
    val amountColor = when {
        isExpense -> MaterialTheme.colorScheme.error
        isIncome -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurface
    }
    var showSmsDialog by remember { mutableStateOf(false) }
    var showMarkMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = { showMarkMenu = true }),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAtmWithdrawal -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                isInterAccountTransfer -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        DropdownMenu(
            expanded = showMarkMenu,
            onDismissRequest = { showMarkMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalAtm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAtmWithdrawal) "Unmark ATM Withdrawal" else "Mark as ATM Withdrawal")
                    }
                },
                onClick = { onMarkAsAtm(transactionId, !isAtmWithdrawal); showMarkMenu = false }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isInterAccountTransfer) "Unmark Inter-account Transfer" else "Mark as Inter-account Transfer")
                    }
                },
                onClick = { onMarkAsInterAccount(transactionId, !isInterAccountTransfer); showMarkMenu = false }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp),
                            tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Credited", color = Color(0xFF2E7D32))
                    }
                },
                onClick = { onMarkAsCredited(transactionId); showMarkMenu = false }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Debited", color = MaterialTheme.colorScheme.error)
                    }
                },
                onClick = { onMarkAsDebited(transactionId); showMarkMenu = false }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(merchantName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    buildString {
                        append(category)
                        if (!bankName.isNullOrBlank()) append(" • $bankName")
                        append(" • $dateTime")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                    Icon(Icons.Default.LocalAtm, null, modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("ATM", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.SemiBold)
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
                                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Transfer", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // SMS icon
                if (!smsBody.isNullOrBlank()) {
                    IconButton(
                        onClick = { showSmsDialog = true },
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
                    "${if (isExpense) "-" else if (isIncome) "+" else ""}₹${amount.abs().toPlainString()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
            }
        }
    }

    // SMS popup dialog
    if (showSmsDialog && !smsBody.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                "${if (isExpense) "-" else "+"}₹${amount.abs().toPlainString()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
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
                    if (!smsSender.isNullOrBlank()) {
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
                TextButton(onClick = { showSmsDialog = false }) { Text("Close") }
            }
        )
    }
}
