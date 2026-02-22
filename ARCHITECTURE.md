# Technical Architecture Document
# Everypaisa — Multi-Currency Expense Tracker System Architecture

**Version:** 2.0  
**Date:** February 22, 2026  
**Package:** `com.everypaisa.tracker`  
**Supported Regions:** 🇮🇳 India | 🇦🇪 UAE | 🇺🇸 USA | 🇬🇧 UK | 🇸🇦 Saudi Arabia | Global  
**Supported Currencies:** 30+ (AED, INR, USD, SAR, EUR, GBP, JPY, CNY, AUD, CAD, etc.)  
**Supported Banks:** 40+ (Indian & International)

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Module Structure](#2-module-structure)
3. [Layer Architecture (MVVM + Clean)](#3-layer-architecture)
4. [Dependency Injection (Hilt)](#4-dependency-injection)
5. [Navigation Architecture](#5-navigation-architecture)
6. [Data Layer](#6-data-layer)
7. [SMS Pipeline](#7-sms-pipeline)
8. [AI Integration](#8-ai-integration)
9. [State Management (UDF)](#9-state-management)
10. [Background Processing](#10-background-processing)
11. [Security Architecture](#11-security-architecture)
12. [Build Configuration](#12-build-configuration)
13. [Testing Strategy](#13-testing-strategy)
14. [File-by-File Manifest](#14-file-by-file-manifest)

---

## 1. Architecture Overview

### 1.1 High-Level System Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Everypaisa App                          │
│                                                                 │
│  ┌───────────────────── Presentation Layer ───────────────────┐ │
│  │  Screens (Compose)  ←→  ViewModels (StateFlow)            │ │
│  │  Components          ←→  UiState data classes              │ │
│  │  Navigation Host     ←→  Events / Actions                  │ │
│  └──────────────────────────────┬────────────────────────────┘ │
│                                 │                               │
│  ┌──────────────────── Domain Layer ─────────────────────────┐ │
│  │  Use Cases (optional)                                      │ │
│  │  Domain Models                                             │ │
│  │  Repository Interfaces                                     │ │
│  └──────────────────────────────┬────────────────────────────┘ │
│                                 │                               │
│  ┌──────────────────── Data Layer ───────────────────────────┐ │
│  │  Repository Implementations                                │ │
│  │  Room Database  ←→  DAOs  ←→  Entities                    │ │
│  │  DataStore (Preferences)                                   │ │
│  │  SMS Content Resolver                                      │ │
│  │  parser-core module                                        │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌──────────────────── Infrastructure ───────────────────────┐ │
│  │  WorkManager (Background SMS scan)                         │ │
│  │  BroadcastReceiver (Real-time SMS)                         │ │
│  │  MediaPipe LLM (AI inference)                              │ │
│  │  BiometricPrompt (App lock)                                │ │
│  │  Glance (Widgets)                                          │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────── parser-core module ───────────────────┐ │
│  │  BankParserFactory → BankParser implementations            │ │
│  │  ParsedTransaction, MandateInfo models                     │ │
│  │  Pure Kotlin — no Android dependencies                     │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Key Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Architecture pattern | MVVM + Clean + UDF | Google-recommended, testable, scalable |
| UI framework | Jetpack Compose | Modern, declarative, less boilerplate |
| DI framework | Hilt | Official Google DI, lifecycle-aware |
| State management | StateFlow + UDF | Predictable, testable, Compose-friendly |
| Database | Room | Official ORM, compile-time SQL checks, Flow support |
| Navigation | Compose Navigation (type-safe) | Type-safe args with Kotlin Serialization |
| AI | MediaPipe LLM | On-device, privacy-first, no API keys |
| SMS parsing | Separate module (parser-core) | Testable independently, no Android deps |
| Background work | WorkManager | Battery-friendly, system-managed |
| Async | Coroutines + Flow | First-class Kotlin support, structured concurrency |
| Preferences | DataStore (Proto) | Type-safe, non-blocking, coroutine-native |
| Charts | Vico | Compose-native charts, Material 3 styled |
| Activity count | Single Activity | Modern Compose architecture standard |

---

## 2. Module Structure

### 2.1 Gradle Modules

```
everypaisa/                            # Root project
├── app/                               # Main application module
│   ├── build.gradle.kts               # App-level dependencies
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/everypaisa/tracker/
│       │   │   └── ... (see §14 for full manifest)
│       │   └── res/
│       │       ├── values/
│       │       │   ├── strings.xml
│       │       │   ├── colors.xml
│       │       │   └── themes.xml
│       │       ├── drawable/
│       │       ├── mipmap-*/           # App icon (adaptive)
│       │       └── xml/
│       │           └── backup_rules.xml
│       ├── test/                       # Unit tests
│       └── androidTest/                # Instrumentation tests
│
├── parser-core/                       # SMS parser module (pure Kotlin)
│   ├── build.gradle.kts               # Kotlin JVM plugin only
│   └── src/
│       ├── main/kotlin/com/everypaisa/parser/core/
│       │   └── ... (see §7 for details)
│       └── test/kotlin/               # Parser unit tests
│
├── build.gradle.kts                   # Root build config
├── settings.gradle.kts                # include(":app", ":parser-core")
├── gradle.properties                  # Build properties
├── gradle/
│   └── libs.versions.toml            # Version catalog
├── .gitignore
├── PRD.md
├── DESIGN_SPECIFICATIONS.md
└── ARCHITECTURE.md                    # This document
```

### 2.2 Module Dependency Graph

```
app ──depends-on──→ parser-core

parser-core: Zero Android dependencies (pure Kotlin)
app: All Android + Hilt + Compose + Room + MediaPipe
```

### 2.3 Version Catalog (`libs.versions.toml`)

```toml
[versions]
kotlin = "1.9.22"
agp = "8.2.2"
compose-bom = "2024.02.00"
compose-compiler = "1.5.9"
hilt = "2.50"
room = "2.6.1"
navigation = "2.7.7"
lifecycle = "2.7.0"
coroutines = "1.8.0"
datastore = "1.0.0"
mediapipe = "0.10.10"
biometric = "1.2.0-alpha05"
glance = "1.0.0"
vico = "2.0.0-alpha.12"
serialization = "1.6.2"
work = "2.9.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.1.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# DataStore
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Serialization
serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# MediaPipe
mediapipe-llm = { group = "com.google.mediapipe", name = "tasks-genai", version.ref = "mediapipe" }

# WorkManager
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Biometric
biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }

# Glance (Widgets)
glance = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }

# Charts
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# Testing
junit = { group = "junit", name = "junit", version = "4.13.2" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.9" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
compose-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }
room = { id = "androidx.room", version.ref = "room" }
```

---

## 3. Layer Architecture

### 3.1 Layer Responsibilities

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                     │
│  ┌─────────────┐    ┌───────────────┐    ┌───────────┐ │
│  │   Screens    │←──→│  ViewModels   │    │ UI State  │ │
│  │  (Compose)   │    │  (@Hilt)      │←──→│  (data    │ │
│  │              │    │               │    │  classes)  │ │
│  └─────────────┘    └───────┬───────┘    └───────────┘ │
│                             │                            │
├─────────────────────────────┼────────────────────────────┤
│                    DOMAIN LAYER                           │
│  ┌─────────────┐    ┌──────┴────────┐                   │
│  │  Use Cases   │    │  Repository   │                   │
│  │  (optional)  │    │  Interfaces   │                   │
│  └─────────────┘    └──────┬────────┘                   │
│                             │                            │
├─────────────────────────────┼────────────────────────────┤
│                      DATA LAYER                           │
│  ┌─────────────┐    ┌──────┴────────┐    ┌───────────┐ │
│  │  Repository  │    │    Room DB    │    │ DataStore  │ │
│  │  Impls       │←──→│  DAOs         │    │ Prefs     │ │
│  │              │    │  Entities     │    │           │ │
│  └─────────────┘    └──────────────┘    └───────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Data Flow (UDF — Unidirectional Data Flow)

```
                    ┌──────────┐
                    │  Screen  │
                    │ (Compose)│
                    └────┬─────┘
              observes   │   sends events
           ┌─────────────┴──────────────┐
           ↓                            ↓
    ┌──────────┐                 ┌────────────┐
    │ UiState  │ ←─── updates ── │   Events   │
    │(StateFlow│                 │  (sealed   │
    │  <T>)    │                 │   class)   │
    └──────────┘                 └─────┬──────┘
           ↑                           │
           │                           ↓
    ┌──────┴───────────────────────────────┐
    │           ViewModel                   │
    │   combine flows → emit UiState        │
    │   handle events → call repositories   │
    └──────────────────────┬───────────────┘
                           │
                           ↓
    ┌──────────────────────────────────────┐
    │         Repository (Flow)             │
    │   wraps DAO queries as Flow<T>        │
    │   handles data mapping                │
    └──────────────────────┬───────────────┘
                           │
                           ↓
    ┌──────────────────────────────────────┐
    │         Room DAO (Flow)               │
    │   @Query returns Flow<List<Entity>>   │
    │   auto-notifies on data changes       │
    └──────────────────────────────────────┘
```

### 3.3 Example: Full Data Flow for HomeScreen

```kotlin
// 1. UI STATE
data class HomeUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val monthlyTotal: BigDecimal = BigDecimal.ZERO,
    val previousMonthTotal: BigDecimal = BigDecimal.ZERO,
    val transactionCount: Int = 0,
    val budgetSummary: BudgetOverallSummary? = null,
    val accounts: List<AccountBalanceEntity> = emptyList(),
    val selectedCategory: String? = null,
    val selectedPeriod: Period = Period.THIS_MONTH,
    val isLoading: Boolean = true,
    val isScanningInProgress: Boolean = false,
    val error: String? = null,
    val showSpotlightTutorial: Boolean = false
)

// 2. EVENTS
sealed interface HomeEvent {
    data class SelectCategory(val category: String?) : HomeEvent
    data class SelectPeriod(val period: Period) : HomeEvent
    data object ScanSms : HomeEvent
    data class DeleteTransaction(val id: Long) : HomeEvent
    data class UndoDelete(val id: Long) : HomeEvent
    data object DismissSpotlight : HomeEvent
}

// 3. VIEWMODEL
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetGroupRepository,
    private val accountRepository: AccountRepository,
    private val smsScanner: SmsScanner,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectCategory -> filterByCategory(event.category)
            is HomeEvent.SelectPeriod -> changePeriod(event.period)
            is HomeEvent.ScanSms -> triggerSmsScan()
            is HomeEvent.DeleteTransaction -> softDelete(event.id)
            is HomeEvent.UndoDelete -> restoreTransaction(event.id)
            is HomeEvent.DismissSpotlight -> dismissSpotlight()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getTransactionsForPeriod(Period.THIS_MONTH),
                budgetRepository.getOverallSummary(),
                accountRepository.getAllAccounts()
            ) { transactions, budget, accounts ->
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        monthlyTotal = transactions.sumOf { t -> t.amount },
                        transactionCount = transactions.size,
                        budgetSummary = budget,
                        accounts = accounts,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }
}

// 4. SCREEN (Compose)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAccountDetail: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EveryPaisaScaffold(
        title = "Everypaisa",
        actions = { SettingsIconButton(onClick = onNavigateToSettings) },
        fab = {
            ScanFab(
                isScanning = uiState.isScanningInProgress,
                onClick = { viewModel.onEvent(HomeEvent.ScanSms) }
            )
        }
    ) {
        HomeContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onTransactionClick = onNavigateToTransactionDetail,
            onViewAllClick = onNavigateToTransactions,
            onAccountClick = onNavigateToAccountDetail
        )
    }
}
```

---

## 4. Dependency Injection (Hilt)

### 4.1 Hilt Module Graph

```
@HiltAndroidApp
EveryPaisaApp
    │
    ├── @Singleton ── DatabaseModule
    │                  ├── EveryPaisaDatabase
    │                  ├── TransactionDao
    │                  ├── SubscriptionDao
    │                  ├── CategoryDao
    │                  ├── MerchantMappingDao
    │                  ├── AccountBalanceDao
    │                  ├── ChatMessageDao
    │                  ├── UnrecognizedSmsDao
    │                  ├── RuleDao
    │                  ├── ExchangeRateDao
    │                  ├── BudgetDao
    │                  ├── TransactionSplitDao
    │                  └── CategoryBudgetLimitDao
    │
    ├── @Singleton ── RepositoryModule
    │                  ├── TransactionRepository
    │                  ├── SubscriptionRepository
    │                  ├── CategoryRepository
    │                  ├── BudgetGroupRepository
    │                  ├── AccountRepository
    │                  ├── ChatRepository
    │                  ├── RuleRepository
    │                  ├── ExchangeRateRepository
    │                  └── PreferencesRepository
    │
    ├── @Singleton ── ParserModule
    │                  ├── BankParserFactory
    │                  └── SmsTransactionProcessor
    │
    ├── @Singleton ── AiModule
    │                  └── AiChatService (MediaPipe LLM)
    │
    └── @Singleton ── WorkerModule
                       └── SmsScanner (WorkManager enqueue)
```

### 4.2 Module Implementations

```kotlin
// DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EveryPaisaDatabase {
        return Room.databaseBuilder(
            context,
            EveryPaisaDatabase::class.java,
            "everypaisa_db"
        )
        .addCallback(DatabaseSeedCallback())  // Seed default categories
        .addMigrations(*ALL_MIGRATIONS)
        .build()
    }

    @Provides fun provideTransactionDao(db: EveryPaisaDatabase) = db.transactionDao()
    @Provides fun provideSubscriptionDao(db: EveryPaisaDatabase) = db.subscriptionDao()
    @Provides fun provideCategoryDao(db: EveryPaisaDatabase) = db.categoryDao()
    // ... all 14 DAOs
}

// RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    // ... all repositories
}
```

---

## 5. Navigation Architecture

### 5.1 Route Definitions (Type-Safe with Kotlin Serialization)

```kotlin
// EveryPaisaDestinations.kt
sealed interface EveryPaisaDestination {

    @Serializable data object AppLock : EveryPaisaDestination
    @Serializable data object Permission : EveryPaisaDestination
    @Serializable data object Home : EveryPaisaDestination
    @Serializable data class HomeWithCategoryFilter(val category: String) : EveryPaisaDestination
    @Serializable data object Transactions : EveryPaisaDestination
    @Serializable data object Settings : EveryPaisaDestination
    @Serializable data object Categories : EveryPaisaDestination
    @Serializable data object Analytics : EveryPaisaDestination
    @Serializable data object Chat : EveryPaisaDestination
    @Serializable data class TransactionDetail(val id: Long) : EveryPaisaDestination
    @Serializable data object AddTransaction : EveryPaisaDestination
    @Serializable data class AccountDetail(
        val bankName: String,
        val accountLast4: String
    ) : EveryPaisaDestination
    @Serializable data object ManageAccounts : EveryPaisaDestination
    @Serializable data object AddAccount : EveryPaisaDestination
    @Serializable data object UnrecognizedSms : EveryPaisaDestination
    @Serializable data object Faq : EveryPaisaDestination
    @Serializable data object Rules : EveryPaisaDestination
    @Serializable data class CreateRule(val ruleId: Long? = null) : EveryPaisaDestination
    @Serializable data object BudgetGroups : EveryPaisaDestination
    @Serializable data object MonthlyBudgetSettings : EveryPaisaDestination
    @Serializable data object ExchangeRates : EveryPaisaDestination
    @Serializable data object Subscriptions : EveryPaisaDestination
}
```

### 5.2 NavHost Structure

```kotlin
// EveryPaisaNavHost.kt
@Composable
fun EveryPaisaNavHost(
    navController: NavHostController,
    startDestination: EveryPaisaDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable<EveryPaisaDestination.AppLock> { AppLockScreen(...) }
        composable<EveryPaisaDestination.Permission> { PermissionScreen(...) }

        // Main (with bottom nav)
        composable<EveryPaisaDestination.Home> {
            MainScreen(rootNavController = navController)
        }

        // Detail screens (no bottom nav)
        composable<EveryPaisaDestination.TransactionDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<EveryPaisaDestination.TransactionDetail>()
            TransactionDetailScreen(transactionId = detail.id, ...)
        }

        composable<EveryPaisaDestination.AddTransaction> { AddTransactionScreen(...) }
        composable<EveryPaisaDestination.Rules> { RulesScreen(...) }
        composable<EveryPaisaDestination.CreateRule> { ... }
        composable<EveryPaisaDestination.BudgetGroups> { BudgetGroupsScreen(...) }
        composable<EveryPaisaDestination.MonthlyBudgetSettings> { ... }
        composable<EveryPaisaDestination.ExchangeRates> { ... }
        composable<EveryPaisaDestination.AccountDetail> { ... }
    }
}

// MainScreen.kt (nested NavHost with BottomNav)
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val nestedNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            EveryPaisaBottomBar(
                navController = nestedNavController,
                items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Analytics,
                    BottomNavItem.Chat
                )
            )
        }
    ) { padding ->
        NavHost(
            navController = nestedNavController,
            startDestination = EveryPaisaDestination.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable<EveryPaisaDestination.Home> {
                HomeScreen(
                    onNavigateToTransactionDetail = { id ->
                        rootNavController.navigate(EveryPaisaDestination.TransactionDetail(id))
                    },
                    onNavigateToSettings = {
                        nestedNavController.navigate(EveryPaisaDestination.Settings)
                    },
                    ...
                )
            }
            composable<EveryPaisaDestination.Analytics> { AnalyticsScreen(...) }
            composable<EveryPaisaDestination.Chat> { ChatScreen(...) }
            composable<EveryPaisaDestination.Transactions> { TransactionsScreen(...) }
            composable<EveryPaisaDestination.Subscriptions> { SubscriptionsScreen(...) }
            composable<EveryPaisaDestination.Settings> { SettingsScreen(...) }
            composable<EveryPaisaDestination.Categories> { CategoriesScreen(...) }
            composable<EveryPaisaDestination.ManageAccounts> { ManageAccountsScreen(...) }
            composable<EveryPaisaDestination.AddAccount> { AddAccountScreen(...) }
            composable<EveryPaisaDestination.UnrecognizedSms> { UnrecognizedSmsScreen(...) }
            composable<EveryPaisaDestination.Faq> { FaqScreen(...) }
        }
    }
}
```

### 5.3 Start Destination Logic

```kotlin
// Determine start destination in MainActivity
val startDestination = when {
    biometricEnabled -> EveryPaisaDestination.AppLock
    !smsPermissionGranted -> EveryPaisaDestination.Permission
    else -> EveryPaisaDestination.Home
}
```

---

## 6. Data Layer

### 6.1 Room Database

```kotlin
@Database(
    entities = [
        TransactionEntity::class,
        SubscriptionEntity::class,
        CategoryEntity::class,
        MerchantMappingEntity::class,
        AccountBalanceEntity::class,
        CardEntity::class,
        ChatMessage::class,
        UnrecognizedSmsEntity::class,
        RuleEntity::class,
        RuleApplicationEntity::class,
        ExchangeRateEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        TransactionSplitEntity::class,
        CategoryBudgetLimitEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class EveryPaisaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun accountBalanceDao(): AccountBalanceDao
    abstract fun cardDao(): CardDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun unrecognizedSmsDao(): UnrecognizedSmsDao
    abstract fun ruleDao(): RuleDao
    abstract fun ruleApplicationDao(): RuleApplicationDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun budgetDao(): BudgetDao
    abstract fun budgetCategoryDao(): BudgetCategoryDao
    abstract fun transactionSplitDao(): TransactionSplitDao
    abstract fun categoryBudgetLimitDao(): CategoryBudgetLimitDao
}
```

### 6.2 Type Converters

```kotlin
class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.toBigDecimalOrNull()

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? =
        value?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? =
        value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)
}
```

### 6.3 Key DAO Examples

```kotlin
@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
        AND date_time BETWEEN :startDate AND :endDate
        ORDER BY date_time DESC
    """)
    fun getTransactionsForPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0 AND category = :category
        AND date_time BETWEEN :startDate AND :endDate
        ORDER BY date_time DESC
    """)
    fun getTransactionsByCategory(
        category: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT category, SUM(amount) as total, COUNT(*) as count
        FROM transactions
        WHERE is_deleted = 0 AND transaction_type = 'EXPENSE'
        AND date_time BETWEEN :startDate AND :endDate
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategoryBreakdown(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategorySpending>>

    @Query("""
        SELECT merchant_name, SUM(amount) as total, COUNT(*) as count
        FROM transactions
        WHERE is_deleted = 0 AND transaction_type = 'EXPENSE'
        AND date_time BETWEEN :startDate AND :endDate
        GROUP BY merchant_name
        ORDER BY total DESC
        LIMIT :limit
    """)
    fun getTopMerchants(
        startDate: Long,
        endDate: Long,
        limit: Int = 10
    ): Flow<List<MerchantSpending>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE transactions SET is_deleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE is_deleted = 0 AND transaction_type = 'EXPENSE'
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<BigDecimal?>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE is_deleted = 0 AND transaction_type IN ('INCOME', 'SALARY')
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<BigDecimal?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<TransactionEntity?>

    @Query("""
        SELECT DISTINCT currency FROM transactions
        WHERE is_deleted = 0
        ORDER BY currency
    """)
    fun getDistinctCurrencies(): Flow<List<String>>
}
```

### 6.4 Repository Pattern

```kotlin
// Interface (Domain layer)
interface TransactionRepository {
    fun getTransactionsForPeriod(period: Period): Flow<List<TransactionEntity>>
    fun getTransactionById(id: Long): Flow<TransactionEntity?>
    fun getCategoryBreakdown(period: Period): Flow<List<CategorySpending>>
    fun getTopMerchants(period: Period, limit: Int): Flow<List<MerchantSpending>>
    fun getTotalExpenses(period: Period): Flow<BigDecimal>
    fun getTotalIncome(period: Period): Flow<BigDecimal>
    fun getDistinctCurrencies(): Flow<List<String>>
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun softDelete(id: Long)
    suspend fun restore(id: Long)
}

// Implementation (Data layer)
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dateUtils: DateUtils
) : TransactionRepository {

    override fun getTransactionsForPeriod(period: Period): Flow<List<TransactionEntity>> {
        val (start, end) = dateUtils.getDateRange(period)
        return transactionDao.getTransactionsForPeriod(
            startDate = start.toEpochMillis(),
            endDate = end.toEpochMillis()
        )
    }

    // ... other implementations
}
```

### 6.5 DataStore Preferences

```kotlin
// Keys
object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")           // "system", "light", "dark"
    val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    val AMOLED_DARK = booleanPreferencesKey("amoled_dark")
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    val SMS_SCAN_RANGE_DAYS = intPreferencesKey("sms_scan_range_days")
    val MONTHLY_BUDGET = stringPreferencesKey("monthly_budget")    // BigDecimal as String
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val SPOTLIGHT_SHOWN = booleanPreferencesKey("spotlight_shown")
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    val AI_MODEL_DOWNLOADED = booleanPreferencesKey("ai_model_downloaded")
    val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
    val LAST_SMS_SCAN_TIMESTAMP = longPreferencesKey("last_sms_scan_ts")
}
```

---

## 7. SMS Pipeline

### 7.1 Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        SMS Pipeline                              │
│                                                                  │
│  ┌──────────────┐     ┌──────────────────┐                      │
│  │ SmsBroadcast  │────→│ SmsTransaction   │                      │
│  │ Receiver      │     │ Processor        │                      │
│  │ (real-time)   │     │                  │                      │
│  └──────────────┘     │                  │                      │
│                        │  1. Filter bank  │     ┌──────────┐    │
│  ┌──────────────┐     │     SMS           │────→│  Room    │    │
│  │ Optimized    │────→│  2. Parse via     │     │  Database │    │
│  │ SmsReader    │     │     BankParser    │     └──────────┘    │
│  │ Worker       │     │  3. Deduplicate   │                      │
│  │ (WorkManager)│     │  4. Categorize    │     ┌──────────┐    │
│  └──────────────┘     │  5. Store         │────→│Unrecog.  │    │
│                        │                  │     │SMS Table │    │
│  ┌──────────────┐     │                  │     └──────────┘    │
│  │ ContentRes.  │────→│                  │                      │
│  │ (history     │     └──────────────────┘                      │
│  │  scan)       │                                                │
│  └──────────────┘                                                │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              parser-core module                           │   │
│  │                                                           │   │
│  │  BankParserFactory.parse(body, sender)                    │   │
│  │      ↓                                                    │   │
│  │  senderMap["HDFCBK"] → HDFCBankParser                     │   │
│  │  senderMap["ICICIB"] → ICICIBankParser                    │   │
│  │  senderMap["SBIINB"] → SBIBankParser                      │   │
│  │  ...35+ parsers                                           │   │
│  │      ↓                                                    │   │
│  │  parser.parse(body) → ParsedTransaction? or null          │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 parser-core Module Files

```
parser-core/src/main/kotlin/com/everypaisa/parser/core/
├── ParsedTransaction.kt         # Data class with amount, merchant, type, etc.
├── TransactionType.kt           # INCOME, EXPENSE, CREDIT, TRANSFER, INVESTMENT
├── MandateInfo.kt               # UMN, merchant, amount for subscriptions
├── BankParser.kt                # Interface: parse(smsBody: String): ParsedTransaction?
├── BankParserFactory.kt         # Maps SMS sender → BankParser, dispatches
├── GenericParser.kt             # Fallback regex-based parser
├── ParserUtils.kt               # Amount parsing, date parsing utilities
└── bank/                        # Bank-specific parsers
    ├── HDFCBankParser.kt
    ├── ICICIBankParser.kt
    ├── SBIBankParser.kt
    ├── AxisBankParser.kt
    ├── KotakBankParser.kt
    ├── IDFCFirstBankParser.kt
    ├── YesBankParser.kt
    ├── IndusIndBankParser.kt
    ├── PNBBankParser.kt
    ├── CanaraParser.kt
    ├── BoBParser.kt
    ├── BOIParser.kt
    ├── UnionBankParser.kt
    ├── FederalBankParser.kt
    ├── JupiterParser.kt
    ├── AmexParser.kt
    ├── OneCardParser.kt
    ├── AUBankParser.kt
    ├── HSBCParser.kt
    ├── IDBIParser.kt
    ├── GooglePayParser.kt
    ├── PhonePeParser.kt
    ├── PaytmParser.kt
    ├── AmazonPayParser.kt
    └── BHIMParser.kt
```

### 7.3 Sender-to-Parser Mapping

```kotlin
// BankParserFactory.kt
class BankParserFactory {

    private val senderMap: Map<String, BankParser> = mapOf(
        // HDFC variants
        "HDFCBK" to HDFCBankParser(),
        "HDFCBN" to HDFCBankParser(),
        "HDFCCC" to HDFCBankParser(),

        // ICICI
        "ICICIB" to ICICIBankParser(),
        "ICICIT" to ICICIBankParser(),

        // SBI
        "SBIINB" to SBIBankParser(),
        "SBICRD" to SBIBankParser(),

        // UPI
        "GPAY" to GooglePayParser(),
        "PHNEPE" to PhonePeParser(),
        "PYTM" to PaytmParser(),

        // ... 35+ more
    )

    private val genericParser = GenericParser()

    fun parse(smsBody: String, sender: String): ParsedTransaction? {
        // Normalize sender: strip country code, keep alpha portion
        val normalizedSender = sender.replace(Regex("[^A-Za-z]"), "").uppercase()

        // Try specific parser first
        val parser = senderMap.entries.find { (key, _) ->
            normalizedSender.contains(key)
        }?.value

        return parser?.parse(smsBody) ?: genericParser.parse(smsBody)
    }

    fun isBankSms(sender: String): Boolean {
        val normalized = sender.replace(Regex("[^A-Za-z]"), "").uppercase()
        return senderMap.keys.any { normalized.contains(it) } ||
               normalized.matches(Regex(".*[A-Z]{2}[A-Z]+.*"))  // Bank-like sender pattern
    }
}
```

### 7.4 Deduplication

```kotlin
// TransactionHash generation
fun generateTransactionHash(
    amount: BigDecimal,
    merchantName: String,
    dateTime: LocalDateTime,
    bankName: String?,
    accountLast4: String?
): String {
    val raw = "${amount.toPlainString()}|${merchantName.lowercase()}|" +
              "${dateTime.toEpochSecond(ZoneOffset.UTC)}|${bankName ?: ""}|${accountLast4 ?: ""}"
    return MessageDigest.getInstance("SHA-256")
        .digest(raw.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
```

Room uses `@Index(value = ["transaction_hash"], unique = true)` and `OnConflictStrategy.IGNORE` to silently skip duplicates.

### 7.5 Auto-Categorization Pipeline

```
ParsedTransaction.merchantName
    ↓
1. Check MerchantMappingEntity table (user overrides)
    ↓ (miss)
2. Check RuleEntity matches (smart rules)
    ↓ (miss)
3. Keyword-based matching:
   "swiggy|zomato|dominos|pizza" → Food & Dining
   "amazon|flipkart|myntra" → Shopping
   "uber|ola|metro" → Transportation
   "netflix|spotify|hotstar" → Subscriptions
   "salary|credited your" → Salary
    ↓ (miss)
4. Default: "Others"
```

---

## 8. AI Integration

### 8.1 MediaPipe LLM Setup

```kotlin
@Singleton
class AiChatService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetGroupRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    private var llmInference: LlmInference? = null

    sealed class ModelState {
        data object NotDownloaded : ModelState()
        data class Downloading(val progress: Float) : ModelState()
        data object Ready : ModelState()
        data class Error(val message: String) : ModelState()
    }

    val modelState = MutableStateFlow<ModelState>(ModelState.NotDownloaded)

    suspend fun initialize() {
        val modelPath = getModelPath()
        if (!File(modelPath).exists()) {
            modelState.value = ModelState.NotDownloaded
            return
        }

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(4096)
            .setTemperature(0.7f)
            .setTopK(40)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        modelState.value = ModelState.Ready
    }

    suspend fun generateResponse(
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): Flow<String> = flow {
        val context = buildContext()
        val prompt = buildPrompt(context, chatHistory, userMessage)

        llmInference?.generateResponseAsync(prompt)?.collect { partialResult ->
            emit(partialResult)
        }
    }

    private suspend fun buildContext(): ChatContext {
        return ChatContext(
            currentDate = LocalDate.now(),
            monthSummary = transactionRepository.getMonthSummary(),
            recentTransactions = transactionRepository.getRecentSummary(50),
            activeSubscriptions = subscriptionRepository.getActiveSummary(),
            topCategories = transactionRepository.getTopCategories(5),
            quickStats = transactionRepository.getQuickStats()
        )
    }

    private fun buildPrompt(
        context: ChatContext,
        history: List<ChatMessage>,
        userMessage: String
    ): String = """
        You are Everypaisa AI, a personal finance assistant. You help users understand
        their spending patterns. All data is from their bank SMS messages processed
        on their phone.

        Current Date: ${context.currentDate}

        FINANCIAL CONTEXT:
        - This month total expenses: ₹${context.monthSummary.totalExpenses}
        - This month total income: ₹${context.monthSummary.totalIncome}
        - Transaction count: ${context.monthSummary.count}

        Top Categories:
        ${context.topCategories.joinToString("\n") { "- ${it.name}: ₹${it.total} (${it.count} txns)" }}

        Active Subscriptions:
        ${context.activeSubscriptions.joinToString("\n") { "- ${it.name}: ₹${it.amount}/${it.cycle}" }}

        Quick Stats:
        - Average daily spending: ₹${context.quickStats.avgDaily}
        - Highest single expense: ₹${context.quickStats.highestSingle} (${context.quickStats.highestMerchant})

        Recent Transactions (last 50):
        ${context.recentTransactions.joinToString("\n") { "- ${it.date}: ${it.merchant} ₹${it.amount} [${it.category}]" }}

        CONVERSATION:
        ${history.joinToString("\n") { "${it.role}: ${it.content}" }}
        User: $userMessage
        Assistant:
    """.trimIndent()
}
```

---

## 9. State Management

### 9.1 ViewModel Pattern (All ViewModels)

Every screen's ViewModel follows the same pattern:

```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val repository: XxxRepository
) : ViewModel() {

    // 1. Private mutable state
    private val _uiState = MutableStateFlow(XxxUiState())
    // 2. Public immutable state
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    // 3. Event handler (single entry point for all user actions)
    fun onEvent(event: XxxEvent) {
        when (event) {
            is XxxEvent.Load -> loadData()
            is XxxEvent.Action -> performAction(event)
        }
    }

    // 4. Internal state mutations always via _uiState.update { ... }
    private fun loadData() {
        viewModelScope.launch {
            repository.getData()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { data ->
                    _uiState.update { it.copy(data = data, isLoading = false) }
                }
        }
    }
}
```

### 9.2 ViewModels Required

| ViewModel | Screen | Key State |
|---|---|---|
| `HomeViewModel` | HomeScreen | transactions, monthlyTotal, budgetSummary, accounts |
| `TransactionsViewModel` | TransactionsScreen | filteredTransactions, filters, searchQuery |
| `TransactionDetailViewModel` | TransactionDetailScreen | transaction, splits, isEditing |
| `AnalyticsViewModel` | AnalyticsScreen | trendData, categoryBreakdown, topMerchants |
| `ChatViewModel` | ChatScreen | messages, modelState, isGenerating |
| `SettingsViewModel` | SettingsScreen | preferences, modelInfo |
| `CategoriesViewModel` | CategoriesScreen | categories, isReordering |
| `SubscriptionsViewModel` | SubscriptionsScreen | subscriptions, monthlyTotal |
| `BudgetGroupsViewModel` | BudgetGroupsScreen | budgets, categoryBudgets, progress |
| `RulesViewModel` | RulesScreen | rules, ruleApplications |
| `AccountsViewModel` | ManageAccountsScreen | accounts, totalBalance |
| `AccountDetailViewModel` | AccountDetailScreen | account, transactions |
| `ExchangeRatesViewModel` | ExchangeRatesScreen | rates |
| `UnrecognizedSmsViewModel` | UnrecognizedSmsScreen | smsList, count |

---

## 10. Background Processing

### 10.1 WorkManager Workers

```kotlin
// OptimizedSmsReaderWorker.kt
@HiltWorker
class OptimizedSmsReaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val smsTransactionProcessor: SmsTransactionProcessor,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lastScanTimestamp = preferencesRepository.getLastSmsScanTimestamp()
        val scanRangeDays = preferencesRepository.getSmsScanRangeDays()

        val startDate = if (lastScanTimestamp > 0) {
            lastScanTimestamp
        } else {
            System.currentTimeMillis() - (scanRangeDays * 24 * 60 * 60 * 1000L)
        }

        // Read SMS via ContentResolver
        val smsList = readSmsFromContentResolver(startDate)

        // Process through parser pipeline
        val result = smsTransactionProcessor.processBatch(smsList)

        // Update last scan timestamp
        preferencesRepository.setLastSmsScanTimestamp(System.currentTimeMillis())

        // Emit result via WorkInfo data
        return Result.success(
            workDataOf(
                "new_transactions" to result.newTransactions,
                "duplicates_skipped" to result.duplicatesSkipped,
                "unrecognized" to result.unrecognized
            )
        )
    }
}
```

### 10.2 BroadcastReceiver (Real-time SMS)

```kotlin
// SmsBroadcastReceiver.kt
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var smsTransactionProcessor: SmsTransactionProcessor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val smsBody = messages.joinToString("") { it.messageBody }
        val sender = messages.firstOrNull()?.originatingAddress ?: return

        // Check if it's a bank SMS
        if (!BankParserFactory().isBankSms(sender)) return

        // Enqueue one-time worker to process this SMS
        val workRequest = OneTimeWorkRequestBuilder<OptimizedSmsReaderWorker>()
            .setInputData(workDataOf("sms_body" to smsBody, "sender" to sender))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
```

### 10.3 Widget Update Worker

```kotlin
// WidgetUpdateWorker.kt
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Update Glance widgets with latest data
        EveryPaisaDailyWidget().update(applicationContext)
        EveryPaisaRecentWidget().update(applicationContext)
        return Result.success()
    }
}
```

---

## 11. Security Architecture

### 11.1 Biometric Auth Flow

```
App Launch
    ↓
Check DataStore: biometric_enabled?
    ├── No → Permission → Home
    └── Yes → AppLockScreen
                 ↓
         BiometricPrompt.authenticate()
                 ↓
         ├── Success → Permission → Home
         ├── Error → Show error, retry
         └── Fallback → Device PIN/Pattern
```

### 11.2 Implementation

```kotlin
// AppLockScreen.kt
@Composable
fun AppLockScreen(
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)

    LaunchedEffect(Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Everypaisa")
            .setSubtitle("Authenticate to view your finances")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthenticated()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Show error and allow retry
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
```

### 11.3 Data Safety

| Aspect | Implementation |
|---|---|
| Data at rest | Encrypted via Android device encryption (FBE) |
| Data in transit | None — no network calls |
| SMS data | Read-only, processed locally, stored in Room |
| AI inference | On-device only, no cloud API |
| Backup | Optional local backup, user-controlled |
| Data deletion | Settings → Clear All Data (drops all tables) |
| 3rd party SDKs | Zero analytics/tracking/ad SDKs |

---

## 12. Build Configuration

### 12.1 `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.everypaisa.tracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.everypaisa.tracker"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // java.time API on older devices
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Modules
    implementation(project(":parser-core"))

    // Core
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // DataStore
    implementation(libs.datastore)

    // Coroutines
    implementation(libs.coroutines.android)

    // Serialization
    implementation(libs.serialization.json)

    // MediaPipe
    implementation(libs.mediapipe.llm)

    // WorkManager
    implementation(libs.work.runtime)

    // Biometric
    implementation(libs.biometric)

    // Glance (Widgets)
    implementation(libs.glance)
    implementation(libs.glance.material3)

    // Charts
    implementation(libs.vico.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.test)
}
```

### 12.2 `parser-core/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Pure Kotlin — no Android dependencies
    testImplementation(libs.junit)
}
```

### 12.3 AndroidManifest.xml (Key Permissions & Components)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <application
        android:name=".EveryPaisaApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Everypaisa"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Everypaisa">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Everypaisa.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver
            android:name=".receiver.SmsBroadcastReceiver"
            android:exported="true"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>

        <receiver
            android:name=".widget.EveryPaisaDailyWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/daily_widget_info" />
        </receiver>

        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>

    </application>
</manifest>
```

---

## 13. Testing Strategy

### 13.1 Testing Pyramid

```
          ╱╲
         ╱  ╲         E2E Tests (10%)
        ╱    ╲        Compose UI tests, full flows
       ╱──────╲
      ╱        ╲      Integration Tests (20%)
     ╱          ╲     DAO + Repository, ViewModel + Repository
    ╱────────────╲
   ╱              ╲   Unit Tests (70%)
  ╱                ╲  Parsers, ViewModels, Repositories, Utils
 ╱──────────────────╲
```

### 13.2 Test Plan

| Layer | What | Tool | Coverage Target |
|---|---|---|---|
| parser-core | Each bank parser with 10+ SMS samples | JUnit | 95% |
| parser-core | Edge cases: amounts, dates, special chars | JUnit | 95% |
| parser-core | BankParserFactory sender mapping | JUnit | 100% |
| Data | Room DAOs (queries, inserts, updates) | Instrumentation | 80% |
| Data | Repository implementations | JUnit + MockK | 85% |
| Data | TypeConverters | JUnit | 100% |
| Presentation | ViewModels (state transitions, events) | JUnit + Turbine | 80% |
| Presentation | State reduction (UiState from events) | JUnit | 90% |
| UI | Screen rendering (Compose) | Compose Testing | 60% |
| UI | Navigation (correct routes) | Compose Navigation Testing | 70% |
| E2E | SMS scan → transaction list flow | Espresso + Compose | Key flows |
| E2E | Budget setup → overspend alert flow | Espresso + Compose | Key flows |

### 13.3 Parser Test Example

```kotlin
class HDFCBankParserTest {

    private val parser = HDFCBankParser()

    @Test
    fun `parse debit SMS with UPI`() {
        val sms = "Rs 500.00 debited from A/c XX1234 on 16-02-26 at MCD STORE (UPI Ref No 123456789)"
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(BigDecimal("500.00"), result!!.amount)
        assertEquals("MCD STORE", result.merchantName)
        assertEquals(TransactionType.EXPENSE, result.transactionType)
        assertEquals("HDFC", result.bankName)
        assertEquals("1234", result.accountLast4)
    }

    @Test
    fun `parse credit SMS`() {
        val sms = "Rs 50,000.00 credited to A/c XX1234 on 16-02-26. Available Balance: Rs 1,25,678.50"
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(BigDecimal("50000.00"), result!!.amount)
        assertEquals(TransactionType.INCOME, result.transactionType)
    }

    @Test
    fun `return null for OTP SMS`() {
        val sms = "Your OTP for transaction is 123456. Do not share."
        val result = parser.parse(sms)
        assertNull(result)
    }

    @Test
    fun `return null for promotional SMS`() {
        val sms = "Dear Customer, enjoy 10% cashback on shopping with HDFC card."
        val result = parser.parse(sms)
        assertNull(result)
    }
}
```

---

## 14. File-by-File Manifest

### 14.1 App Module — Complete File Tree

```
app/src/main/java/com/everypaisa/tracker/
│
├── EveryPaisaApp.kt                          # @HiltAndroidApp Application
├── MainActivity.kt                            # Single Activity, setContent { Theme { NavHost } }
│
├── navigation/
│   ├── EveryPaisaDestinations.kt             # All route sealed classes
│   ├── EveryPaisaNavHost.kt                  # Root NavHost composable
│   └── BottomNavItem.kt                      # Bottom nav tab definitions
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                          # Brand color palette
│   │   ├── Theme.kt                          # EveryPaisaTheme composable
│   │   ├── Type.kt                           # Typography definitions
│   │   └── Shape.kt                          # Shape definitions
│   │
│   └── components/
│       ├── EveryPaisaScaffold.kt             # Reusable scaffold wrapper
│       ├── SummaryCard.kt                    # Hero card (monthly total)
│       ├── TransactionListItem.kt            # Single transaction row
│       ├── DateHeader.kt                     # Date group header
│       ├── CategoryChip.kt                   # Filterable category chip
│       ├── BudgetProgressCard.kt             # Budget progress bar card
│       ├── AccountSummaryCard.kt             # Bank account card
│       ├── SubscriptionCard.kt               # Subscription display card
│       ├── ChatBubble.kt                     # AI/User chat message
│       ├── SuggestionChipRow.kt              # Chat suggestion chips
│       ├── SpotlightOverlay.kt               # Tutorial spotlight
│       ├── ExpandableSection.kt              # Collapsible section
│       ├── EmptyState.kt                     # Empty state illustration
│       ├── ScanFab.kt                        # Scan SMS FAB with animation
│       ├── PeriodSelector.kt                 # Period filter tabs/chips
│       ├── SearchBar.kt                      # Transaction search
│       ├── FilterRow.kt                      # Multi-filter row
│       ├── AmountText.kt                     # Formatted, colored amount
│       ├── CategoryIcon.kt                   # Icon with category color
│       └── LoadingIndicator.kt               # Shimmer/skeleton loading
│
├── presentation/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   ├── HomeUiState.kt
│   │   └── HomeEvent.kt
│   │
│   ├── transactions/
│   │   ├── TransactionsScreen.kt
│   │   ├── TransactionsViewModel.kt
│   │   ├── TransactionsUiState.kt
│   │   └── TransactionsEvent.kt
│   │
│   ├── transaction_detail/
│   │   ├── TransactionDetailScreen.kt
│   │   ├── TransactionDetailViewModel.kt
│   │   ├── TransactionDetailUiState.kt
│   │   └── TransactionDetailEvent.kt
│   │
│   ├── add_transaction/
│   │   ├── AddTransactionScreen.kt
│   │   └── AddTransactionViewModel.kt
│   │
│   ├── analytics/
│   │   ├── AnalyticsScreen.kt
│   │   ├── AnalyticsViewModel.kt
│   │   ├── AnalyticsUiState.kt
│   │   └── charts/
│   │       ├── SpendingTrendChart.kt
│   │       ├── CategoryBreakdownChart.kt
│   │       └── MerchantRankingList.kt
│   │
│   ├── chat/
│   │   ├── ChatScreen.kt
│   │   ├── ChatViewModel.kt
│   │   └── ChatUiState.kt
│   │
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsViewModel.kt
│   │   └── components/
│   │       ├── SwitchPreference.kt
│   │       ├── NavigationPreference.kt
│   │       ├── InfoPreference.kt
│   │       └── DangerButton.kt
│   │
│   ├── categories/
│   │   ├── CategoriesScreen.kt
│   │   └── CategoriesViewModel.kt
│   │
│   ├── subscriptions/
│   │   ├── SubscriptionsScreen.kt
│   │   └── SubscriptionsViewModel.kt
│   │
│   ├── budget/
│   │   ├── BudgetGroupsScreen.kt
│   │   ├── BudgetGroupsViewModel.kt
│   │   ├── MonthlyBudgetSettingsScreen.kt
│   │   └── MonthlyBudgetSettingsViewModel.kt
│   │
│   ├── rules/
│   │   ├── RulesScreen.kt
│   │   ├── RulesViewModel.kt
│   │   ├── CreateRuleScreen.kt
│   │   └── CreateRuleViewModel.kt
│   │
│   ├── accounts/
│   │   ├── ManageAccountsScreen.kt
│   │   ├── AccountsViewModel.kt
│   │   ├── AccountDetailScreen.kt
│   │   ├── AccountDetailViewModel.kt
│   │   ├── AddAccountScreen.kt
│   │   └── AddAccountViewModel.kt
│   │
│   ├── exchange_rates/
│   │   ├── ExchangeRatesScreen.kt
│   │   └── ExchangeRatesViewModel.kt
│   │
│   ├── unrecognized/
│   │   ├── UnrecognizedSmsScreen.kt
│   │   └── UnrecognizedSmsViewModel.kt
│   │
│   ├── faq/
│   │   └── FaqScreen.kt
│   │
│   ├── permission/
│   │   └── PermissionScreen.kt
│   │
│   └── applock/
│       └── AppLockScreen.kt
│
├── domain/
│   ├── model/
│   │   ├── Period.kt                         # TODAY, THIS_WEEK, THIS_MONTH, etc.
│   │   ├── CategorySpending.kt              # category, total, count
│   │   ├── MerchantSpending.kt              # merchant, total, count
│   │   ├── MonthSummary.kt                  # totalExpenses, totalIncome, count
│   │   ├── BudgetOverallSummary.kt          # budget, spent, remaining, dailyAllowance
│   │   ├── QuickStats.kt                    # avgDaily, highestSingle, etc.
│   │   └── ChatContext.kt                   # Context injected into AI
│   │
│   └── repository/
│       ├── TransactionRepository.kt          # Interface
│       ├── SubscriptionRepository.kt         # Interface
│       ├── CategoryRepository.kt             # Interface
│       ├── BudgetGroupRepository.kt          # Interface
│       ├── AccountRepository.kt              # Interface
│       ├── ChatRepository.kt                 # Interface
│       ├── RuleRepository.kt                 # Interface
│       ├── ExchangeRateRepository.kt         # Interface
│       └── PreferencesRepository.kt          # Interface
│
├── data/
│   ├── db/
│   │   ├── EveryPaisaDatabase.kt             # @Database class
│   │   ├── Converters.kt                    # TypeConverters
│   │   ├── DatabaseSeedCallback.kt          # Seed default categories
│   │   └── Migrations.kt                    # Manual migration helpers
│   │
│   ├── entity/
│   │   ├── TransactionEntity.kt
│   │   ├── SubscriptionEntity.kt
│   │   ├── CategoryEntity.kt
│   │   ├── MerchantMappingEntity.kt
│   │   ├── AccountBalanceEntity.kt
│   │   ├── CardEntity.kt
│   │   ├── ChatMessage.kt
│   │   ├── UnrecognizedSmsEntity.kt
│   │   ├── RuleEntity.kt
│   │   ├── RuleApplicationEntity.kt
│   │   ├── ExchangeRateEntity.kt
│   │   ├── BudgetEntity.kt
│   │   ├── BudgetCategoryEntity.kt
│   │   ├── TransactionSplitEntity.kt
│   │   └── CategoryBudgetLimitEntity.kt
│   │
│   ├── dao/
│   │   ├── TransactionDao.kt
│   │   ├── SubscriptionDao.kt
│   │   ├── CategoryDao.kt
│   │   ├── MerchantMappingDao.kt
│   │   ├── AccountBalanceDao.kt
│   │   ├── CardDao.kt
│   │   ├── ChatMessageDao.kt
│   │   ├── UnrecognizedSmsDao.kt
│   │   ├── RuleDao.kt
│   │   ├── RuleApplicationDao.kt
│   │   ├── ExchangeRateDao.kt
│   │   ├── BudgetDao.kt
│   │   ├── BudgetCategoryDao.kt
│   │   ├── TransactionSplitDao.kt
│   │   └── CategoryBudgetLimitDao.kt
│   │
│   └── repository/
│       ├── TransactionRepositoryImpl.kt
│       ├── SubscriptionRepositoryImpl.kt
│       ├── CategoryRepositoryImpl.kt
│       ├── BudgetGroupRepositoryImpl.kt
│       ├── AccountRepositoryImpl.kt
│       ├── ChatRepositoryImpl.kt
│       ├── RuleRepositoryImpl.kt
│       ├── ExchangeRateRepositoryImpl.kt
│       └── PreferencesRepositoryImpl.kt
│
├── di/
│   ├── DatabaseModule.kt                     # Room DB + DAOs
│   ├── RepositoryModule.kt                   # Binds interfaces → impls
│   ├── ParserModule.kt                       # BankParserFactory
│   ├── AiModule.kt                           # MediaPipe LLM
│   └── WorkerModule.kt                       # WorkManager config
│
├── worker/
│   ├── OptimizedSmsReaderWorker.kt           # Background SMS scan
│   ├── WidgetUpdateWorker.kt                 # Widget data refresh
│   └── SmsTransactionProcessor.kt            # Parse + deduplicate + categorize + store
│
├── receiver/
│   └── SmsBroadcastReceiver.kt               # Real-time SMS listener
│
├── widget/
│   ├── EveryPaisaDailyWidget.kt              # GlanceAppWidget - daily total
│   ├── EveryPaisaDailyWidgetReceiver.kt
│   ├── EveryPaisaRecentWidget.kt             # GlanceAppWidget - recent txns
│   └── EveryPaisaRecentWidgetReceiver.kt
│
├── ai/
│   └── AiChatService.kt                     # MediaPipe LLM wrapper
│
├── utils/
│   ├── CurrencyFormatter.kt                 # ₹1,25,000 formatting
│   ├── DateUtils.kt                         # Period → date range, relative dates
│   ├── TransactionHashUtils.kt              # SHA-256 deduplication hash
│   └── Extensions.kt                        # BigDecimal, LocalDateTime extensions
│
└── core/
    └── Constants.kt                          # Default categories, colors, etc.
```

### 14.2 File Count Summary

| Section | Files |
|---|---|
| App entry (Application, Activity) | 2 |
| Navigation | 3 |
| Theme | 4 |
| UI Components | 21 |
| Screens + ViewModels | ~42 |
| Domain Models | 7 |
| Domain Repository Interfaces | 9 |
| Entities | 15 |
| DAOs | 15 |
| Repository Implementations | 9 |
| DI Modules | 5 |
| Workers | 3 |
| Receivers | 1 |
| Widgets | 4 |
| AI | 1 |
| Utils | 4 |
| Core | 1 |
| **App Module Total** | **~146 files** |
| parser-core Module | ~30 files |
| **Grand Total** | **~176 Kotlin files** |

---

*This is a living document. Updated as architecture evolves.*
