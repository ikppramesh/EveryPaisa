package com.everypaisa.tracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.everypaisa.tracker.data.preferences.UserPreferencesManager
import com.everypaisa.tracker.navigation.EveryPaisaNavHost
import com.everypaisa.tracker.presentation.applock.AppLockScreen
import com.everypaisa.tracker.ui.theme.EveryPaisaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            EveryPaisaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val biometricEnabled by preferencesManager.isBiometricEnabled
                        .collectAsState(initial = false)
                    // Start locked; use a counter to force AppLockScreen re-creation
                    var isLocked by remember { mutableStateOf(true) }
                    var lockGeneration by remember { mutableIntStateOf(0) }
                    var wasInBackground by remember { mutableStateOf(false) }
                    val lifecycleOwner = LocalLifecycleOwner.current

                    // Track background/foreground transitions
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_PAUSE -> {
                                    // Mark that we went to background
                                    wasInBackground = true
                                }
                                Lifecycle.Event.ON_RESUME -> {
                                    // When coming back from background, lock the app
                                    if (wasInBackground) {
                                        wasInBackground = false
                                        isLocked = true
                                        lockGeneration++ // force new composable instance
                                    }
                                }
                                else -> { /* no-op */ }
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Always render the nav host underneath
                        EveryPaisaNavHost()

                        // Show lock screen overlay when locked + biometric enabled
                        if (biometricEnabled && isLocked) {
                            // key(lockGeneration) forces a fresh composable each time
                            key(lockGeneration) {
                                AppLockScreen(
                                    onUnlocked = {
                                        isLocked = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
