package com.everypaisa.tracker.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everypaisa.tracker.data.dao.TransactionDao
import com.everypaisa.tracker.data.entity.TransactionEntity
import com.everypaisa.tracker.data.entity.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

enum class ChartPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

enum class SortOption(val label: String) {
    AMOUNT_HIGH_TO_LOW("Amount (High → Low)"),
    AMOUNT_LOW_TO_HIGH("Amount (Low → High)"),
    DATE_NEWEST_FIRST("Date (Newest First)"),
    DATE_OLDEST_FIRST("Date (Oldest First)"),
    MERCHANT_A_TO_Z("Merchant (A → Z)"),
    MERCHANT_Z_TO_A("Merchant (Z → A)")
}

data class BarData(
    val label: String,
    val expense: BigDecimal,
    val income: BigDecimal,
    val startMillis: Long,
    val endMillis: Long
)

data class AnalyticsUiState(
    val chartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
    val bars: List<BarData> = emptyList(),
    val selectedBarIndex: Int? = null,
    val selectedBarTransactions: List<TransactionEntity> = emptyList(),
    val sortBy: SortOption = SortOption.AMOUNT_HIGH_TO_LOW,
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadChart(ChartPeriod.MONTHLY)
    }

    fun selectPeriod(period: ChartPeriod) {
        _uiState.update { it.copy(chartPeriod = period, selectedBarIndex = null, selectedBarTransactions = emptyList()) }
        loadChart(period)
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
        // Re-sort current transactions
        val sorted = sortTransactions(_uiState.value.selectedBarTransactions, sortOption)
        _uiState.update { it.copy(selectedBarTransactions = sorted) }
    }

    fun selectBar(index: Int) {
        val bars = _uiState.value.bars
        if (index < 0 || index >= bars.size) return
        val bar = bars[index]
        _uiState.update { it.copy(selectedBarIndex = index) }

        viewModelScope.launch {
            transactionDao.getTransactionsForPeriod(bar.startMillis, bar.endMillis)
                .collect { transactions ->
                    val sorted = sortTransactions(transactions, _uiState.value.sortBy)
                    _uiState.update { it.copy(selectedBarTransactions = sorted) }
                }
        }
    }

    private fun loadChart(period: ChartPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            transactionDao.getAllTransactionsSync().let { allTransactions ->
                if (allTransactions.isEmpty()) {
                    _uiState.update { it.copy(bars = emptyList(), isLoading = false) }
                    return@launch
                }

                val bars = when (period) {
                    ChartPeriod.YEARLY -> aggregateYearly(allTransactions)
                    ChartPeriod.MONTHLY -> aggregateMonthly(allTransactions)
                    ChartPeriod.WEEKLY -> aggregateWeekly(allTransactions)
                }

                _uiState.update { it.copy(bars = bars, isLoading = false) }

                // Auto-select the last bar
                if (bars.isNotEmpty()) {
                    selectBar(bars.lastIndex)
                }
            }
        }
    }

    private fun aggregateYearly(transactions: List<TransactionEntity>): List<BarData> {
        val zone = ZoneId.systemDefault()
        val grouped = transactions.groupBy { it.dateTime.year }
        return grouped.keys.sorted().map { year ->
            val txns = grouped[year] ?: emptyList()
            val startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0)
            val endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59)
            BarData(
                label = year.toString(),
                expense = txns.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount },
                income = txns.filter { it.transactionType in listOf(TransactionType.INCOME, TransactionType.CREDIT) }.sumOf { it.amount },
                startMillis = startOfYear.atZone(zone).toInstant().toEpochMilli(),
                endMillis = endOfYear.atZone(zone).toInstant().toEpochMilli()
            )
        }
    }

    private fun aggregateMonthly(transactions: List<TransactionEntity>): List<BarData> {
        val zone = ZoneId.systemDefault()
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val grouped = transactions.groupBy { Pair(it.dateTime.year, it.dateTime.monthValue) }
        return grouped.keys.sortedWith(compareBy({ it.first }, { it.second })).takeLast(12).map { (year, month) ->
            val txns = grouped[Pair(year, month)] ?: emptyList()
            val startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0)
            val lastDay = startOfMonth.toLocalDate().lengthOfMonth()
            val endOfMonth = LocalDateTime.of(year, month, lastDay, 23, 59, 59)
            BarData(
                label = "${monthNames[month - 1]}\n$year",
                expense = txns.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount },
                income = txns.filter { it.transactionType in listOf(TransactionType.INCOME, TransactionType.CREDIT) }.sumOf { it.amount },
                startMillis = startOfMonth.atZone(zone).toInstant().toEpochMilli(),
                endMillis = endOfMonth.atZone(zone).toInstant().toEpochMilli()
            )
        }
    }

    private fun aggregateWeekly(transactions: List<TransactionEntity>): List<BarData> {
        val zone = ZoneId.systemDefault()
        val weekFields = WeekFields.of(Locale.getDefault())
        val grouped = transactions.groupBy {
            val weekNum = it.dateTime.get(weekFields.weekOfWeekBasedYear())
            val yearNum = it.dateTime.get(weekFields.weekBasedYear())
            Pair(yearNum, weekNum)
        }
        return grouped.keys.sortedWith(compareBy({ it.first }, { it.second })).takeLast(12).map { (year, week) ->
            val txns = grouped[Pair(year, week)] ?: emptyList()
            val firstTxnDate = txns.minOf { it.dateTime }
            val lastTxnDate = txns.maxOf { it.dateTime }
            val startDay = firstTxnDate.toLocalDate()
            val endDay = lastTxnDate.toLocalDate()
            val startOfWeek = firstTxnDate.withHour(0).withMinute(0).withSecond(0)
            val endOfWeek = lastTxnDate.withHour(23).withMinute(59).withSecond(59)
            BarData(
                label = "${startDay.dayOfMonth}/${startDay.monthValue}\n${endDay.dayOfMonth}/${endDay.monthValue}",
                expense = txns.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount },
                income = txns.filter { it.transactionType in listOf(TransactionType.INCOME, TransactionType.CREDIT) }.sumOf { it.amount },
                startMillis = startOfWeek.atZone(zone).toInstant().toEpochMilli(),
                endMillis = endOfWeek.atZone(zone).toInstant().toEpochMilli()
            )
        }
    }

    private fun sortTransactions(
        transactions: List<TransactionEntity>,
        sortOption: SortOption
    ): List<TransactionEntity> {
        return when (sortOption) {
            SortOption.AMOUNT_HIGH_TO_LOW -> transactions.sortedByDescending { it.amount }
            SortOption.AMOUNT_LOW_TO_HIGH -> transactions.sortedBy { it.amount }
            SortOption.DATE_NEWEST_FIRST -> transactions.sortedByDescending { it.dateTime }
            SortOption.DATE_OLDEST_FIRST -> transactions.sortedBy { it.dateTime }
            SortOption.MERCHANT_A_TO_Z -> transactions.sortedBy { it.merchantName.lowercase() }
            SortOption.MERCHANT_Z_TO_A -> transactions.sortedByDescending { it.merchantName.lowercase() }
        }
    }
}
