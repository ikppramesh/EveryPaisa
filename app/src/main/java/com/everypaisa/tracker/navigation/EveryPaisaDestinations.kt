package com.everypaisa.tracker.navigation

import kotlinx.serialization.Serializable

sealed interface EveryPaisaDestination {
    @Serializable
    data object Permission : EveryPaisaDestination
    
    @Serializable
    data object Home : EveryPaisaDestination
    
    @Serializable
    data object Transactions : EveryPaisaDestination
    
    @Serializable
    data object Analytics : EveryPaisaDestination
    
    @Serializable
    data object Chat : EveryPaisaDestination
    
    @Serializable
    data object Settings : EveryPaisaDestination
    
    @Serializable
    data class TransactionDetail(val id: Long) : EveryPaisaDestination
}
