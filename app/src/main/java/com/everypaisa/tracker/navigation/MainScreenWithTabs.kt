package com.everypaisa.tracker.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.everypaisa.tracker.presentation.home.HomeScreenNew
import com.everypaisa.tracker.presentation.regional.RegionalHomeScreen
import com.everypaisa.tracker.worker.OptimizedSmsReaderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    onNavigateToTransactions: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val current = countryTabs[selectedTab]

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isScanning by remember { mutableStateOf(false) }

    fun triggerScan() {
        scope.launch {
            isScanning = true
            snackbarHostState.showSnackbar("📱 Scanning SMS messages...")
            val workRequest = OneTimeWorkRequestBuilder<OptimizedSmsReaderWorker>().build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(workRequest)
            var done = false
            var attempts = 0
            while (!done && attempts < 60) {
                delay(1000)
                attempts++
                val info = workManager.getWorkInfoById(workRequest.id).get()
                when (info?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        done = true
                        snackbarHostState.showSnackbar("✅ Scan complete! Transactions updated")
                    }
                    WorkInfo.State.FAILED -> {
                        done = true
                        snackbarHostState.showSnackbar("❌ Scan failed. Try again.")
                    }
                    WorkInfo.State.CANCELLED -> {
                        done = true
                        snackbarHostState.showSnackbar("⚠️ Scan cancelled")
                    }
                    else -> { /* still running */ }
                }
            }
            if (!done) snackbarHostState.showSnackbar("⏰ Still scanning — transactions will appear shortly.")
            isScanning = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true &&
                permissions[Manifest.permission.RECEIVE_SMS] == true
        if (granted) triggerScan()
        else scope.launch {
            snackbarHostState.showSnackbar("⚠️ SMS permission required to scan transactions")
        }
    }

    fun startScan() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) triggerScan()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                // ── Shared app bar ──────────────────────────────────────────
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(id = com.everypaisa.tracker.R.string.app_name),
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
                        // Scan / Sync button
                        IconButton(
                            onClick = { startScan() },
                            enabled = !isScanning
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Scan SMS")
                            }
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
                val screenWidthDp = LocalConfiguration.current.screenWidthDp
                // On narrow phones (< 380dp) show only flag emoji; wider screens show flag + name
                val showTabLabel = screenWidthDp >= 380
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = if (showTabLabel) 4.dp else 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    countryTabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.height(if (showTabLabel) 56.dp else 44.dp)
                        ) {
                            if (showTabLabel) {
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
                            } else {
                                Text(
                                    tab.flag,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 6.dp)
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
                    onNavigateToTransactions = onNavigateToTransactions
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
