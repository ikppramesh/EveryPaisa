package com.everypaisa.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.everypaisa.tracker.presentation.analytics.AnalyticsScreen
import com.everypaisa.tracker.presentation.home.HomeScreenNew
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
                    navController.navigate("home") {
                        popUpTo("permission") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("home") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreenNew(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToTransactions = {
                    navController.navigate("transactions")
                },
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                }
            )
        }
        
        composable("analytics") {
            AnalyticsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable("transactions") {
            TransactionsScreen(
                onNavigateBack = { navController.navigateUp() },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
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