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

data class CurrencySummary(
    val currency: String,
    val currencySymbol: String,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val transactionCount: Int
) {
    val netAmount: BigDecimal
        get() = totalIncome.subtract(totalExpenses)
    
    companion object {
        fun getCurrencySymbol(currency: String): String = when (currency.uppercase()) {
            // Indian Rupee
            "INR" -> "₹"
            // US Dollar
            "USD" -> "$"
            // Euro
            "EUR" -> "€"
            // British Pound
            "GBP" -> "£"
            // Japanese Yen
            "JPY" -> "¥"
            // Chinese Yuan
            "CNY" -> "¥"
            // Swiss Franc
            "CHF" -> "CHF"
            // Canadian Dollar
            "CAD" -> "CA$"
            // Australian Dollar
            "AUD" -> "A$"
            // New Zealand Dollar
            "NZD" -> "NZ$"
            // Singapore Dollar
            "SGD" -> "S$"
            // Hong Kong Dollar
            "HKD" -> "HK$"
            // UAE Dirham
            "AED" -> "د.إ"
            // Saudi Riyal
            "SAR" -> "﷼"
            // Qatari Riyal
            "QAR" -> "﷼"
            // Omani Rial
            "OMR" -> "﷼"
            // Kuwaiti Dinar
            "KWD" -> "د.ك"
            // Bahraini Dinar
            "BHD" -> ".د.ب"
            // Nepali Rupee
            "NPR" -> "₨"
            // Pakistani Rupee
            "PKR" -> "₨"
            // Sri Lankan Rupee
            "LKR" -> "₨"
            // Ethiopian Birr
            "ETB" -> "Br"
            // South African Rand
            "ZAR" -> "R"
            // Nigerian Naira
            "NGN" -> "₦"
            // Kenyan Shilling
            "KES" -> "KSh"
            // Egyptian Pound
            "EGP" -> "£"
            // Turkish Lira
            "TRY" -> "₺"
            // Russian Ruble
            "RUB" -> "₽"
            // Brazilian Real
            "BRL" -> "R$"
            // Mexican Peso
            "MXN" -> "$"
            // Argentine Peso
            "ARS" -> "$"
            // Chilean Peso
            "CLP" -> "$"
            // Colombian Peso
            "COP" -> "$"
            // Thai Baht
            "THB" -> "฿"
            // Malaysian Ringgit
            "MYR" -> "RM"
            // Indonesian Rupiah
            "IDR" -> "Rp"
            // Philippine Peso
            "PHP" -> "₱"
            // Vietnamese Dong
            "VND" -> "₫"
            // South Korean Won
            "KRW" -> "₩"
            // Taiwanese Dollar
            "TWD" -> "NT$"
            // Bangladeshi Taka
            "BDT" -> "৳"
            // Myanma Kyat
            "MMK" -> "K"
            // Cambodian Riel
            "KHR" -> "៛"
            // Lao Kip
            "LAK" -> "₭"
            // Default: return currency code
            else -> currency
        }
    }
}

data class MultiCurrencySummary(
    val inrSummary: CurrencySummary?,
    val internationalSummaries: List<CurrencySummary>
) {
    val hasInternational: Boolean
        get() = internationalSummaries.isNotEmpty()
}

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
enum class Country(
    val code: String,
    val label: String,
    val flag: String,
    val region: String,
    val primaryCurrency: String,
    val supportedCurrencies: List<String>
) {
    // Asia
    INDIA("IN", "India", "🇮🇳", "South Asia", "INR", listOf("INR", "USD", "EUR", "GBP")),
    UAE("AE", "UAE", "🇦🇪", "Middle East", "AED", listOf("AED", "SAR", "QAR", "OMR", "KWD", "BHD", "USD", "EUR")),
    NEPAL("NP", "Nepal", "🇳🇵", "South Asia", "NPR", listOf("NPR", "INR", "USD", "EUR", "GBP")),
    THAILAND("TH", "Thailand", "🇹🇭", "Southeast Asia", "THB", listOf("THB", "USD", "EUR", "GBP", "AED")),
    MALAYSIA("MY", "Malaysia", "🇲🇾", "Southeast Asia", "MYR", listOf("MYR", "USD", "EUR", "GBP", "SGD")),
    SINGAPORE("SG", "Singapore", "🇸🇬", "Southeast Asia", "SGD", listOf("SGD", "USD", "EUR", "GBP", "MYR")),
    PAKISTAN("PK", "Pakistan", "🇵🇰", "South Asia", "PKR", listOf("PKR", "USD", "EUR", "GBP", "AED")),
    BANGLADESH("BD", "Bangladesh", "🇧🇩", "South Asia", "BDT", listOf("BDT", "USD", "EUR", "INR")),
    
    // Middle East & Africa
    SAUDI_ARABIA("SA", "Saudi Arabia", "🇸🇦", "Middle East", "SAR", listOf("SAR", "AED", "USD", "EUR", "GBP")),
    EGYPT("EG", "Egypt", "🇪🇬", "Africa", "EGP", listOf("EGP", "USD", "EUR", "AED")),
    KENYA("KE", "Kenya", "🇰🇪", "Africa", "KES", listOf("KES", "USD", "EUR", "GBP")),
    ETHIOPIA("ET", "Ethiopia", "🇪🇹", "Africa", "ETB", listOf("ETB", "USD", "EUR")),
    SOUTH_AFRICA("ZA", "South Africa", "🇿🇦", "Africa", "ZAR", listOf("ZAR", "USD", "EUR", "GBP")),
    
    // Europe
    UK("GB", "United Kingdom", "🇬🇧", "Europe", "GBP", listOf("GBP", "EUR", "USD", "AED")),
    GERMANY("DE", "Germany", "🇩🇪", "Europe", "EUR", listOf("EUR", "GBP", "USD")),
    FRANCE("FR", "France", "🇫🇷", "Europe", "EUR", listOf("EUR", "GBP", "USD")),
    
    // Americas
    USA("US", "United States", "🇺🇸", "Americas", "USD", listOf("USD", "EUR", "GBP", "CAD", "MXN")),
    CANADA("CA", "Canada", "🇨🇦", "Americas", "CAD", listOf("CAD", "USD", "EUR")),
    MEXICO("MX", "Mexico", "🇲🇽", "Americas", "MXN", listOf("MXN", "USD", "EUR"));

    companion object {
        fun fromCode(code: String): Country? = values().find { it.code == code }
        fun getDefault(): Country = INDIA
        fun getByRegion(region: String): List<Country> = values().filter { it.region == region }
        fun getAllRegions(): List<String> = values().map { it.region }.distinct().sorted()
    }
}