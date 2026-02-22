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
import com.everypaisa.tracker.presentation.regional.RegionalHomeScreen
import com.everypaisa.tracker.worker.OptimizedSmsReaderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

/**
 * Add a new country here — that's all you need.
 * The flag, name and currencies drive everything automatically.
 *
 * @param flag        Emoji flag shown in the tab
 * @param name        Country/region display name
 * @param currencies  All accepted currency codes (first = primary for summary card)
 * @param bankHint    Sample banks shown on the empty-state card
 */
// Tab configuration is moved to TabsConfig.kt (countryTabs + helper)
// We will append an "Other" globe tab dynamically based on DB currencies.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithTabs(
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabsVm: MainTabsViewModel = hiltViewModel()
    val tabsList by tabsVm.visibleTabs.collectAsState()

    // Show nothing until the first tab list is ready
    if (tabsList.isEmpty()) return

    // Clamp selectedTab synchronously in composition — no LaunchedEffect race condition.
    // When the list shrinks (e.g. during/after scan) selectedTab may be stale; coerceIn
    // ensures we always read a valid index without mutating state mid-composition.
    val safeIndex = selectedTab.coerceIn(0, tabsList.size - 1)
    val current = tabsList[safeIndex]

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
                    selectedTabIndex = safeIndex,
                    edgePadding = if (showTabLabel) 4.dp else 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabsList.forEachIndexed { index, tab ->
                        Tab(
                            selected = safeIndex == index,
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
                                        color = if (safeIndex == index)
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
            // All tabs — including India — use RegionalHomeScreen so each tab shows ONLY
            // its own currencies (INR for India, USD for USA, etc.).
            // The "Other" 🌐 tab shows every currency from SMS that doesn't match any tab.
            RegionalHomeScreen(
                flag = current.flag,
                regionName = current.name,
                currencies = current.currencies,
                bankHint = current.bankHint,
                onNavigateToTransactions = onNavigateToTransactions
            )
        }
    }
}
