package com.everypaisa.tracker.presentation.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.everypaisa.tracker.data.entity.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCountryMenu by remember { mutableStateOf(false) }
    val countries = remember { com.everypaisa.tracker.domain.model.Country.values().toList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showCountryMenu = true }) {
                            Icon(Icons.Default.Public, contentDescription = "Select Country")
                        }
                        DropdownMenu(
                            expanded = showCountryMenu,
                            onDismissRequest = { showCountryMenu = false }
                        ) {
                            countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text("${country.flag} ${country.label}") },
                                    onClick = {
                                        viewModel.setSelectedCountry(country)
                                        showCountryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector chips
            item {
                PeriodChipRow(
                    selected = uiState.chartPeriod,
                    onSelect = { viewModel.selectPeriod(it) }
                )
            }

            // Legend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = Color(0xFFEF5350), label = "Expenses")
                    Spacer(modifier = Modifier.width(24.dp))
                    LegendDot(color = Color(0xFF66BB6A), label = "Income")
                }
            }

            // Bar chart
            item {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.bars.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transaction data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    BarChart(
                        bars = uiState.bars,
                        selectedIndex = uiState.selectedBarIndex,
                        onBarClick = { viewModel.selectBar(it) }
                    )
                }
            }

            // Selected period summary
            if (uiState.selectedBarIndex != null && uiState.bars.isNotEmpty()) {
                val bar = uiState.bars[uiState.selectedBarIndex!!]
                item {
                    SelectedPeriodSummary(bar = bar)
                }

                // Section header
                item {
                    Text(
                        text = "Transactions (Newest First)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Sort options
                item {
                    SortOptionsRow(
                        selectedSort = uiState.sortBy,
                        onSortChange = { viewModel.setSortOption(it) }
                    )
                }

                // Transaction list sorted by amount desc
                if (uiState.selectedBarTransactions.isEmpty()) {
                    item {
                        Text(
                            "No transactions in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(uiState.selectedBarTransactions) { index, txn ->
                        TransactionRankItem(rank = index + 1, transaction = txn)
                    }
                }

            }

            // Bottom padding
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PeriodChipRow(selected: ChartPeriod, onSelect: (ChartPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        ChartPeriod.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelect(period) },
                label = { Text(period.label) },
                leadingIcon = if (selected == period) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionsRow(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sort by: ${selectedSort.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        if (expanded) {
            HorizontalDivider()
            Column {
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortChange(option)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == option,
                            onClick = {
                                onSortChange(option)
                                expanded = false
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChart(
    bars: List<BarData>,
    selectedIndex: Int?,
    onBarClick: (Int) -> Unit
) {
    val expenseColor = Color(0xFFEF5350)
    val incomeColor = Color(0xFF66BB6A)
    val selectedHighlight = Color(0x40FFFFFF)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    // Animate
    var animProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 600),
        label = "chartAnim"
    )
    LaunchedEffect(bars) {
        animProgress = 0f
        animProgress = 1f
    }

    val maxVal = bars.maxOfOrNull {
        maxOf(it.expense, it.income)
    } ?: BigDecimal.ONE

    val barGroupWidth = 80f
    val chartWidth = (bars.size * barGroupWidth).coerceAtLeast(300f)
    val scrollState = rememberScrollState(Int.MAX_VALUE) // scroll to end (latest data)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width((chartWidth + 40).dp)
                    .height(300.dp)
                    .padding(top = 16.dp, bottom = 40.dp, start = 8.dp, end = 8.dp)
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val index = (offset.x / (barGroupWidth * density)).toInt()
                            if (index in bars.indices) {
                                onBarClick(index)
                            }
                        }
                    }
            ) {
                val chartHeight = size.height - 20f
                val barWidth = barGroupWidth * density * 0.35f
                val gap = barGroupWidth * density * 0.1f

                // Grid lines
                val gridPaint = android.graphics.Paint().apply {
                    color = surfaceVariant.toArgb()
                    strokeWidth = 1f
                }
                for (i in 0..4) {
                    val y = chartHeight * (1 - i / 4f)
                    drawContext.canvas.nativeCanvas.drawLine(
                        0f, y, size.width, y, gridPaint
                    )
                }

                bars.forEachIndexed { index, bar ->
                    val groupX = index * barGroupWidth * density

                    // Selected highlight
                    if (index == selectedIndex) {
                        drawRoundRect(
                            color = primary.copy(alpha = 0.1f),
                            topLeft = Offset(groupX, 0f),
                            size = Size(barGroupWidth * density, chartHeight + 20f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }

                    // Expense bar
                    val expenseH = if (maxVal > BigDecimal.ZERO)
                        (bar.expense.toFloat() / maxVal.toFloat() * chartHeight * animatedProgress)
                    else 0f
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(groupX + gap, chartHeight - expenseH),
                        size = Size(barWidth, expenseH.coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Income bar
                    val incomeH = if (maxVal > BigDecimal.ZERO)
                        (bar.income.toFloat() / maxVal.toFloat() * chartHeight * animatedProgress)
                    else 0f
                    drawRoundRect(
                        color = incomeColor,
                        topLeft = Offset(groupX + gap + barWidth + gap * 0.5f, chartHeight - incomeH),
                        size = Size(barWidth, incomeH.coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Label
                    val labelPaint = android.graphics.Paint().apply {
                        color = if (index == selectedIndex) primary.toArgb() else onSurfaceVariant.toArgb()
                        textSize = 9f * density
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = if (index == selectedIndex) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                    }
                    val labelX = groupX + barGroupWidth * density / 2
                    val lines = bar.label.split("\n")
                    lines.forEachIndexed { li, line ->
                        drawContext.canvas.nativeCanvas.drawText(
                            line,
                            labelX,
                            chartHeight + 14f * density + li * 12f * density,
                            labelPaint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPeriodSummary(bar: BarData) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val net = bar.income.subtract(bar.expense)
    val isPositive = net >= BigDecimal.ZERO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = bar.label.replace("\n", " "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Expenses", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Text(
                        formatter.format(bar.expense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF5350)
                    )
                }
                Column {
                    Text("Income", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Text(
                        formatter.format(bar.income),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF66BB6A)
                    )
                }
                Column {
                    Text("Net", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isPositive) Icons.AutoMirrored.Filled.TrendingUp
                            else Icons.AutoMirrored.Filled.TrendingDown,
                            null,
                            tint = if (isPositive) Color(0xFF66BB6A) else Color(0xFFEF5350),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatter.format(net.abs()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) Color(0xFF66BB6A) else Color(0xFFEF5350)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRankItem(
    rank: Int,
    transaction: com.everypaisa.tracker.data.entity.TransactionEntity
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val isExpense = transaction.transactionType == TransactionType.EXPENSE
    val isIncome = transaction.transactionType == TransactionType.INCOME ||
            transaction.transactionType == TransactionType.CREDIT
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    var showSmsDialog by remember { mutableStateOf(false) }

    val amountColor = when {
        isExpense -> Color(0xFFEF5350)
        isIncome -> Color(0xFF66BB6A)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val typeLabel = when {
        isExpense -> "Spent"
        isIncome -> "Received"
        else -> transaction.transactionType.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                            2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                            3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFFB8860B)
                        2 -> Color(0xFF808080)
                        3 -> Color(0xFF8B4513)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "  •  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.dateTime.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (transaction.bankName != null) {
                    Text(
                        text = "${transaction.bankName}${if (transaction.accountLast4 != null) " •• ${transaction.accountLast4}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // SMS icon
                    if (!transaction.smsBody.isNullOrBlank()) {
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
                        text = "${if (isExpense) "-" else if (isIncome) "+" else ""}${formatter.format(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = amountColor.copy(alpha = 0.7f)
                )
            }
        }
    }

    // SMS popup
    if (showSmsDialog && !transaction.smsBody.isNullOrBlank()) {
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
                                transaction.merchantName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${if (isExpense) "-" else "+"}${formatter.format(transaction.amount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                transaction.dateTime.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!transaction.smsSender.isNullOrBlank()) {
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
                                    transaction.smsSender!!,
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
                                transaction.smsBody!!,
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
