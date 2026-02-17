package com.everypaisa.tracker.domain.model

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class MonthSummary(
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val transactionCount: Int,
    val topCategory: String? = null,
    val topCategoryAmount: BigDecimal? = null
)

data class CategorySpending(
    val category: String,
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val percentage: Float
)

enum class DashboardPeriod(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class Period(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: DashboardPeriod = DashboardPeriod.MONTHLY
) {
    fun format(): String = when (type) {
        DashboardPeriod.DAILY -> {
            val today = LocalDate.now()
            when {
                startDate == today -> "Today"
                startDate == today.minusDays(1) -> "Yesterday"
                else -> startDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy"))
            }
        }
        DashboardPeriod.WEEKLY -> {
            val startFmt = startDate.format(DateTimeFormatter.ofPattern("MMM dd"))
            val endFmt = endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            "Week: $startFmt – $endFmt"
        }
        DashboardPeriod.MONTHLY -> {
            startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
        DashboardPeriod.YEARLY -> {
            startDate.format(DateTimeFormatter.ofPattern("yyyy"))
        }
    }

    fun previous(): Period = when (type) {
        DashboardPeriod.DAILY -> today(startDate.minusDays(1))
        DashboardPeriod.WEEKLY -> thisWeek(startDate.minusWeeks(1))
        DashboardPeriod.MONTHLY -> {
            val prev = startDate.minusMonths(1)
            currentMonth(prev)
        }
        DashboardPeriod.YEARLY -> thisYear(startDate.minusYears(1))
    }

    fun next(): Period = when (type) {
        DashboardPeriod.DAILY -> today(startDate.plusDays(1))
        DashboardPeriod.WEEKLY -> thisWeek(startDate.plusWeeks(1))
        DashboardPeriod.MONTHLY -> {
            val nxt = startDate.plusMonths(1)
            currentMonth(nxt)
        }
        DashboardPeriod.YEARLY -> thisYear(startDate.plusYears(1))
    }

    fun isFuture(): Boolean = endDate.isAfter(LocalDate.now())

    companion object {
        fun today(date: LocalDate = LocalDate.now()): Period {
            return Period(
                startDate = date,
                endDate = date,
                type = DashboardPeriod.DAILY
            )
        }

        fun thisWeek(referenceDate: LocalDate = LocalDate.now()): Period {
            val start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            return Period(
                startDate = start,
                endDate = end,
                type = DashboardPeriod.WEEKLY
            )
        }

        fun currentMonth(referenceDate: LocalDate = LocalDate.now()): Period {
            return Period(
                startDate = referenceDate.withDayOfMonth(1),
                endDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth()),
                type = DashboardPeriod.MONTHLY
            )
        }
        
        fun lastMonth(): Period {
            val lastMonth = LocalDate.now().minusMonths(1)
            return currentMonth(lastMonth)
        }
        
        fun thisYear(referenceDate: LocalDate = LocalDate.now()): Period {
            return Period(
                startDate = referenceDate.withDayOfYear(1),
                endDate = referenceDate.withMonth(12).withDayOfMonth(31),
                type = DashboardPeriod.YEARLY
            )
        }

        fun last30Days(): Period {
            val now = LocalDate.now()
            return Period(
                startDate = now.minusDays(30),
                endDate = now,
                type = DashboardPeriod.MONTHLY
            )
        }

        fun forType(type: DashboardPeriod): Period = when (type) {
            DashboardPeriod.DAILY -> today()
            DashboardPeriod.WEEKLY -> thisWeek()
            DashboardPeriod.MONTHLY -> currentMonth()
            DashboardPeriod.YEARLY -> thisYear()
        }
    }
}
