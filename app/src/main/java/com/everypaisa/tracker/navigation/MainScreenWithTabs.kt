package com.everypaisa.tracker.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.everypaisa.tracker.presentation.home.HomeScreenNew
import com.everypaisa.tracker.presentation.uae.UAEHomeScreen

@Composable
fun MainScreenWithTabs(
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "India") },
                    label = { Text("🇮🇳 India") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Public, contentDescription = "UAE") },
                    label = { Text("🇦🇪 UAE") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreenNew(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
                1 -> UAEHomeScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
            }
        }
    }
}
