package com.everypaisa.tracker.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.everypaisa.tracker.presentation.home.HomeScreenNew
import com.everypaisa.tracker.presentation.regional.RegionalHomeScreen

/**
 * Add a new country here — that's all you need.
 * The flag, name and currencies drive everything automatically.
 *
 * @param flag        Emoji flag shown in the tab
 * @param name        Country/region display name
 * @param currencies  All accepted currency codes (first = primary for summary card)
 * @param bankHint    Sample banks shown on the empty-state card
 */
data class CountryTab(
    val flag: String,
    val name: String,
    val currencies: Set<String>,
    val bankHint: String = ""
)

// ─────────────────────────────────────────────────────────────────────────────
// 👇 ADD NEW COUNTRIES BY APPENDING TO THIS LIST — nothing else to change
// ─────────────────────────────────────────────────────────────────────────────
private val countryTabs = listOf(
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
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithTabs(
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val current = countryTabs[selectedTab]

    Scaffold(
        topBar = {
            Column {
                // ── Shared app bar ──────────────────────────────────────────
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "EveryPaisa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${current.flag} ${current.name}  •  ${current.currencies.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToAnalytics) {
                            Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // ── Scrollable country tabs ─────────────────────────────────
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 4.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    countryTabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    tab.flag,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    tab.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTab == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                // India — uses the full HomeScreenNew (without its own top bar)
                0 -> HomeScreenNew(
                    showTopBar = false,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
                // Every other country — uses the generic RegionalHomeScreen
                else -> RegionalHomeScreen(
                    flag = current.flag,
                    regionName = current.name,
                    currencies = current.currencies,
                    bankHint = current.bankHint,
                    onNavigateToTransactions = onNavigateToTransactions
                )
            }
        }
    }
}
