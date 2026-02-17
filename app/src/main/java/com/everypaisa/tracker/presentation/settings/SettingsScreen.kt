package com.everypaisa.tracker.presentation.settings

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val useBiometric by viewModel.isBiometricEnabled.collectAsState()
    var showNotifications by remember { mutableStateOf(true) }
    var useDynamicColor by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    // Clear data confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all transactions. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearAllData { success ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("✅ All data cleared")
                                } else {
                                    snackbarHostState.showSnackbar("❌ Failed to clear data")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Security",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    title = "App Lock",
                    description = "Use fingerprint or face unlock",
                    icon = Icons.Default.Lock,
                    switchState = useBiometric,
                    onSwitchChanged = { enabled ->
                        if (enabled) {
                            // Check if biometric is available before enabling
                            val biometricManager = BiometricManager.from(context)
                            when (biometricManager.canAuthenticate(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            )) {
                                BiometricManager.BIOMETRIC_SUCCESS -> {
                                    viewModel.setBiometricEnabled(true)
                                }
                                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("❌ No biometric hardware on this device")
                                    }
                                }
                                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("⚠️ No fingerprint or face enrolled. Set up in device Settings first.")
                                    }
                                }
                                else -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("❌ Biometric authentication unavailable")
                                    }
                                }
                            }
                        } else {
                            viewModel.setBiometricEnabled(false)
                        }
                    }
                )
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    title = "Transaction Alerts",
                    description = "Get notified when new transactions are detected",
                    icon = Icons.Default.Notifications,
                    switchState = showNotifications,
                    onSwitchChanged = { showNotifications = it }
                )
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    title = "Dynamic Colors",
                    description = "Use colors from your wallpaper",
                    icon = Icons.Default.Palette,
                    switchState = useDynamicColor,
                    onSwitchChanged = { useDynamicColor = it }
                )
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    "Data",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.exportData { file ->
                            scope.launch {
                                if (file != null) {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Export Transactions"))
                                        snackbarHostState.showSnackbar("✅ Transactions exported")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("❌ Failed to share file")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("⚠️ No transactions to export")
                                }
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Export Data",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Download all transactions as CSV",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showClearDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Clear All Data",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Delete all transactions permanently",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "EveryPaisa",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "100% Privacy-Focused Finance Tracker",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "All data stays on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    switchState: Boolean,
    onSwitchChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = switchState,
                onCheckedChange = onSwitchChanged
            )
        }
    }
}
