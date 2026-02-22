package com.everypaisa.tracker.navigation

/**
 * Country tabs and helper utilities for the top tab row.
 * Keeping this in a single file makes it easy for other components
 * (and viewmodels) to read the current tab configuration.
 */

import com.everypaisa.tracker.domain.model.Country

// Reuse CountryTab data class from MainScreenWithTabs file
// (if needed make CountryTab public there). To avoid duplication, keep the same shape here.

/**
 * Duplicate of CountryTab used by MainScreenWithTabs.kt; kept here so callers can access the
 * tab list without depending on the full UI file.
 */
data class CountryTab(
    val flag: String,
    val name: String,
    val currencies: Set<String>,
    val bankHint: String = ""
)

val countryTabs: List<CountryTab> = listOf(
    CountryTab(
        flag = "🇮🇳",
        name = "India",
        currencies = linkedSetOf("INR"),
        bankHint = "SBI • HDFC • ICICI • Axis • Kotak"
    ),
    CountryTab(
        flag = "🇦🇪",
        name = "UAE",
        currencies = linkedSetOf("AED", "SAR", "QAR", "OMR", "KWD", "BHD"),
        bankHint = "Emirates NBD • ADCB • FAB • Mashreq"
    ),
    CountryTab(
        flag = "🇺🇸",
        name = "USA",
        currencies = linkedSetOf("USD"),
        bankHint = "Chase • Bank of America • Wells Fargo"
    ),
    CountryTab(
        flag = "🇪🇺",
        name = "Europe",
        currencies = linkedSetOf("EUR"),
        bankHint = "HSBC • Deutsche Bank • BNP Paribas"
    ),
    CountryTab(
        flag = "🇬🇧",
        name = "UK",
        currencies = linkedSetOf("GBP"),
        bankHint = "Barclays • HSBC • Lloyds • NatWest"
    ),
    CountryTab(
        flag = "🇸🇬",
        name = "Singapore",
        currencies = linkedSetOf("SGD"),
        bankHint = "DBS • OCBC • UOB"
    ),
    CountryTab(
        flag = "🇦🇺",
        name = "Australia",
        currencies = linkedSetOf("AUD"),
        bankHint = "ANZ • Commonwealth • Westpac • NAB"
    ),
    CountryTab(
        flag = "🇨🇦",
        name = "Canada",
        currencies = linkedSetOf("CAD"),
        bankHint = "RBC • TD • Scotiabank • BMO"
    )
)

fun tabCurrenciesSet(): Set<String> = countryTabs.flatMap { it.currencies }.toSet()
