package com.everypaisa.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.everypaisa.tracker.presentation.home.HomeScreenNew
import com.everypaisa.tracker.presentation.uae.UAEHomeScreen
import com.everypaisa.tracker.presentation.permission.PermissionScreen
import com.everypaisa.tracker.presentation.transactions.TransactionsScreen
import com.everypaisa.tracker.presentation.settings.SettingsScreen

@Composable
fun EveryPaisaNavHost() {
    val navController = rememberNavController()
    
    val startDestination = "permission"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("permission") {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate("main") {
                        popUpTo("permission") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("main") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            MainScreenWithTabs(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToTransactions = {
                    navController.navigate("transactions")
                }
            )
        }
        
        composable("transactions") {
            TransactionsScreen(
                onNavigateBack = { navController.navigateUp() },
                onTransactionClick = { transactionId ->
                    // TODO: Navigate to transaction detail when screen is implemented
                    // For now, do nothing to prevent crash
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        // TODO: Add TransactionDetail, Analytics, Chat screens
    }
}