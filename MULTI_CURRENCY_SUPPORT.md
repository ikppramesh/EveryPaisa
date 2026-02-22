# Multi-Currency Support in EveryPaisa

This document details how EveryPaisa supports 30+ currencies across 40+ banks in multiple regions.

---

## 📋 Table of Contents

1. [Supported Currencies](#supported-currencies)
2. [Supported Banks by Region](#supported-banks-by-region)
3. [GenericBankParser Improvements (v2.2.2)](#genericbankparser-improvements-v222)
4. [Other Countries Tab (v2.2.2)](#other-countries-tab-v222)
5. [Currency Detection Implementation](#currency-detection-implementation)
6. [Amount Extraction](#amount-extraction)
7. [Database Schema](#database-schema)
8. [UI & Display](#ui--display)
9. [Future Enhancements](#future-enhancements)

---

## 💱 Supported Currencies

### Middle East (GCC)
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| AED | United Arab Emirates Dirham | د.إ | 🇦🇪 UAE |
| SAR | Saudi Riyal | ﷼ | 🇸🇦 Saudi Arabia |
| OMR | Omani Rial | ر.ع. | 🇴🇲 Oman |
| QAR | Qatari Riyal | ر.ق | 🇶🇦 Qatar |
| KWD | Kuwaiti Dinar | د.ك | 🇰🇼 Kuwait |
| BHD | Bahraini Dinar | د.ب | 🇧🇭 Bahrain |

### South Asia
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| INR | Indian Rupee | ₹ | 🇮🇳 India |
| NPR | Nepalese Rupee | ₨ | 🇳🇵 Nepal |
| PKR | Pakistani Rupee | ₨ | 🇵🇰 Pakistan |
| LKR | Sri Lankan Rupee | Rs | 🇱🇰 Sri Lanka |
| BDT | Bangladeshi Taka | ৳ | 🇧🇩 Bangladesh |

### Southeast Asia
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| THB | Thai Baht | ฿ | 🇹🇭 Thailand |
| MYR | Malaysian Ringgit | RM | 🇲🇾 Malaysia |
| IDR | Indonesian Rupiah | Rp | 🇮🇩 Indonesia |
| PHP | Philippine Peso | ₱ | 🇵🇭 Philippines |
| VND | Vietnamese Dong | ₫ | 🇻🇳 Vietnam |
| SGD | Singapore Dollar | S$ | 🇸🇬 Singapore |
| HKD | Hong Kong Dollar | HK$ | 🇭🇰 Hong Kong |
| TWD | Taiwan Dollar | NT$ | 🇹🇼 Taiwan (v2.2.2) |
| KHR | Cambodian Riel | ៛ | 🇰🇭 Cambodia (v2.2.2) |
| LAK | Lao Kip | ₭ | 🇱🇦 Laos (v2.2.2) |
| MMK | Myanmar Kyat | K | 🇲🇲 Myanmar (v2.2.2) |

### Major Global Currencies
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| USD | US Dollar | $ | 🇺🇸 USA & Global |
| EUR | Euro | € | 🇪🇺 EU Countries |
| GBP | British Pound | £ | 🇬🇧 UK |
| JPY | Japanese Yen | ¥ | 🇯🇵 Japan |
| CNY | Chinese Yuan | ¥ | 🇨🇳 China |
| AUD | Australian Dollar | A$ | 🇦🇺 Australia |
| CAD | Canadian Dollar | C$ | 🇨🇦 Canada |
| NZD | New Zealand Dollar | NZ$ | 🇳🇿 New Zealand |

### Africa
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| KES | Kenyan Shilling | KSh | 🇰🇪 Kenya (v2.2.2) |
| EGP | Egyptian Pound | E£ | 🇪🇬 Egypt (v2.2.2) |
| NGN | Nigerian Naira | ₦ | 🇳🇬 Nigeria |
| ZAR | South African Rand | R | 🇿🇦 South Africa |
| ETB | Ethiopian Birr | ብር | 🇪🇹 Ethiopia |

### Latin America
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| MXN | Mexican Peso | $ | 🇲🇽 Mexico (v2.2.2) |
| ARS | Argentine Peso | $ | 🇦🇷 Argentina (v2.2.2) |
| CLP | Chilean Peso | $ | 🇨🇱 Chile (v2.2.2) |
| COP | Colombian Peso | $ | 🇨🇴 Colombia (v2.2.2) |
| BRL | Brazilian Real | R$ | 🇧🇷 Brazil |

### Others
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| CHF | Swiss Franc | CHF | 🇨🇭 Switzerland |
| KRW | South Korean Won | ₩ | 🇰🇷 South Korea |
| TRY | Turkish Lira | ₺ | 🇹🇷 Turkey |
| RUB | Russian Ruble | ₽ | 🇷🇺 Russia |

---

## 🏦 Supported Banks by Region

### 🇮🇳 India (25+ Banks)

**Large Banks:**
- HDFC Bank
- ICICI Bank
- SBI (State Bank of India)
- Axis Bank
- Kotak Mahindra Bank

**Other Banks:**
- IDFC First Bank
- Federal Bank
- Punjab National Bank (PNB)
- Bank of Baroda (BOB)
- Canara Bank
- Union Bank of India
- Yes Bank
- IndusInd Bank

**Non-Banking Finance:**
- Airtel Payments
- Jio Finance

---

### 🇦🇪 UAE (5+ Banks)

**Banks:**
- Emirates NBD (ENBD)
- First Abu Dhabi Bank (FAB)
- Mashreq Bank (NEO VISA)
- Abu Dhabi Islamic Bank (ADIB)
- Al Hilal Bank
- Ajman Bank

**Digital Wallets:**
- E& Money (Etisalat)
- Noon Money

---

### 🌐 International Banks

**Global Operations:**
- Citi Bank (40+ countries)
- HSBC (60+ countries)
- Standard Chartered (70+ countries)

**Supported Currencies:** USD, EUR, GBP, JPY, CNY, AUD, CAD, and more

---

## 🔧 GenericBankParser Improvements (v2.2.2)

The `GenericBankParser` received a major overhaul in v2.2.2 to correctly handle international SMS messages from any country.

### Before v2.2.2 (Problem)
- `GenericBankParser` had its own limited `extractCurrency()` method that defaulted everything to INR
- `canParse()` required an account/card reference — this blocked many international SMS from being parsed
- Only a handful of currencies were handled; anything exotic fell through as INR

### After v2.2.2 (Fix)

**Currency Detection — Delegates to ParserUtils:**
```kotlin
// OLD (broken): custom extractCurrency() defaulted to INR
// NEW: delegates to ParserUtils which handles 30+ currencies
val currency = ParserUtils.extractCurrency(message)
val amount = ParserUtils.extractAmount(message)
```

**canParse() — Accepts Explicit Foreign Currencies:**
```kotlin
fun canParse(sender: String, message: String): Boolean {
    val hasExplicitForeignCurrency = ParserUtils.extractCurrency(message)
        .let { it != "INR" && it.isNotEmpty() }
    // International SMS with explicit currency (LKR, MXN, CAD, JPY, CNY, AUD etc.)
    // no longer require account reference
    if (hasExplicitForeignCurrency) return hasTransactionKeyword(message)
    // ... existing logic for INR/account-based SMS
}
```

**New Keywords Added:**

| Category | Keywords Added |
|----------|---------------|
| Debit | `"pos txn"`, `"pos "`, `"card txn"`, `"card payment"`, `"online txn"`, `" txn "`, `"txn at"` |
| Strong Credit | `"direct deposit"`, `"payid transfer"`, `"inward transfer"` |
| Weak Credit | `"transfer received"`, `"deposit received"`, `"incoming transfer"` |

**New Currencies in `codeWithAmountPatterns`:**

```kotlin
// v2.2.2 additions:
"MXN\\s+[\\d,]+\\.?\\d*" to "MXN",   // Mexico
"ARS\\s+[\\d,]+\\.?\\d*" to "ARS",   // Argentina
"CLP\\s+[\\d,]+\\.?\\d*" to "CLP",   // Chile
"COP\\s+[\\d,]+\\.?\\d*" to "COP",   // Colombia
"TWD\\s+[\\d,]+\\.?\\d*" to "TWD",   // Taiwan
"KES\\s+[\\d,]+\\.?\\d*" to "KES",   // Kenya
"EGP\\s+[\\d,]+\\.?\\d*" to "EGP",   // Egypt
"MMK\\s+[\\d,]+\\.?\\d*" to "MMK",   // Myanmar
"KHR\\s+[\\d,]+\\.?\\d*" to "KHR",   // Cambodia
"LAK\\s+[\\d,]+\\.?\\d*" to "LAK",   // Laos
```

**New Keyword Patterns:**

```kotlin
// v2.2.2 additions:
"shilling" to "KES",    // Kenyan Shilling
"dirham"   to "AED",    // UAE Dirham
```

**ParserUtils Visibility Change:**
```kotlin
// BankParsers.kt
// Before: private object ParserUtils { ... }
// After:
internal object ParserUtils { ... }
// Now accessible from GenericBankParser.kt in the same module
```

---

## 🌐 Other Countries Tab (v2.2.2)

A new dynamic "🌐 Other" tab is automatically added to the home screen navigation when the user has SMS transactions in currencies not covered by any named country tab.

### TabsConfig.kt

```kotlin
data class CountryTab(
    val label: String,       // e.g. "🇮🇳 India"
    val currencies: Set<String>
)

val countryTabs = listOf(
    CountryTab("🇮🇳 India",      setOf("INR")),
    CountryTab("🇦🇪 UAE",        setOf("AED", "SAR", "QAR", "OMR", "KWD", "BHD")),
    CountryTab("🇺🇸 USA",        setOf("USD")),
    CountryTab("🇪🇺 Europe",     setOf("EUR", "CHF")),
    CountryTab("🇬🇧 UK",         setOf("GBP")),
    CountryTab("🇸🇬 Singapore",  setOf("SGD", "MYR", "HKD", "THB", "IDR", "PHP", "VND")),
    CountryTab("🇦🇺 Australia",  setOf("AUD", "NZD")),
    CountryTab("🇨🇦 Canada",     setOf("CAD")),
)

fun tabCurrenciesSet() = countryTabs.flatMap { it.currencies }.toSet()
```

### MainTabsViewModel.kt

```kotlin
@HiltViewModel
class MainTabsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val visibleTabs: StateFlow<List<CountryTab>> = 
        transactionRepository.getDistinctCurrencies()
            .map { allCurrencies ->
                val knownCurrencies = tabCurrenciesSet()
                val unmatchedCurrencies = allCurrencies.filter { it !in knownCurrencies }.toSet()
                
                val tabs = countryTabs.toMutableList()
                if (unmatchedCurrencies.isNotEmpty()) {
                    tabs.add(CountryTab("🌐 Other", unmatchedCurrencies))
                }
                tabs
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), countryTabs)
}
```

### How the Other Tab Works

1. `TransactionRepository.getDistinctCurrencies()` returns a `Flow<List<String>>` of all unique currency codes stored in the database (all-time, not just current month)
2. `MainTabsViewModel` computes `unmatchedCurrencies` = currencies not in any named tab's set
3. If `unmatchedCurrencies` is non-empty, a "🌐 Other" tab is appended after Canada
4. `MainScreenWithTabs` already filters tabs to only those with current-month transactions — Other tab follows the same visibility rule
5. `RegionalHomeScreen` receives `current.currencies` (the unmatched set) and filters transactions accordingly

### Crash Fix — Safe Tab Index

```kotlin
// OLD (buggy): LaunchedEffect that reset selectedTab asynchronously
// caused IndexOutOfBoundsException race condition

// NEW (v2.2.2): synchronous clamping before any use of the index
val safeIndex = selectedTab.coerceIn(0, tabsList.size - 1)
HorizontalPager(
    pageCount = tabsList.size,
    state = rememberPagerState(initialPage = safeIndex)
) { page ->
    val current = tabsList[safeIndex]
    // ...
}
```

---

## 🔍 Currency Detection Implementation

### Detection Strategy (Priority Order)

**File:** `parser-core/src/main/java/com/everypaisa/parser/BankParsers.kt`

#### 1. **Currency Symbols (Highest Priority)**

```kotlin
val symbolPatterns = listOf(
    "₹\\s*[\\d,]+\\.?\\d*" to "INR",
    "\\$\\s*[\\d,]+\\.?\\d*" to "USD",
    "€\\s*[\\d,]+\\.?\\d*" to "EUR",
    "£\\s*[\\d,]+\\.?\\d*" to "GBP",
    "د\\.إ\\s*[\\d,]+\\.?\\d*" to "AED",
    "﷼\\s*[\\d,]+\\.?\\d*" to "SAR",
    "¥\\s*[\\d,]+\\.?\\d*" to "JPY",
    "₨\\s*[\\d,]+\\.?\\d*" to "NPR"
)
```

#### 2. **Currency Codes with Amounts (30+ codes, v2.2.2 extended)**

```kotlin
val codeWithAmountPatterns = listOf(
    "USD\\s+[\\d,]+\\.?\\d*" to "USD",
    "AED\\s+[\\d,]+\\.?\\d*" to "AED",
    "EUR\\s+[\\d,]+\\.?\\d*" to "EUR",
    "GBP\\s+[\\d,]+\\.?\\d*" to "GBP",
    "SAR\\s+[\\d,]+\\.?\\d*" to "SAR",
    // ... v2.2.2 new additions:
    "MXN\\s+[\\d,]+\\.?\\d*" to "MXN",
    "ARS\\s+[\\d,]+\\.?\\d*" to "ARS",
    "CLP\\s+[\\d,]+\\.?\\d*" to "CLP",
    "COP\\s+[\\d,]+\\.?\\d*" to "COP",
    "TWD\\s+[\\d,]+\\.?\\d*" to "TWD",
    "KES\\s+[\\d,]+\\.?\\d*" to "KES",
    "EGP\\s+[\\d,]+\\.?\\d*" to "EGP",
    "MMK\\s+[\\d,]+\\.?\\d*" to "MMK",
    "KHR\\s+[\\d,]+\\.?\\d*" to "KHR",
    "LAK\\s+[\\d,]+\\.?\\d*" to "LAK",
)
```

#### 3. **Currency Keywords & Context (v2.2.2 extended)**

```kotlin
val currencyPatterns = mapOf(
    "aed" to "AED",
    "dirham" to "AED",     // v2.2.2
    "shilling" to "KES",   // v2.2.2
    "inr" to "INR",
    "rupee" to "INR",
    "rs." to "INR",
    "dollar" to "USD",
    "euro" to "EUR",
    // ... 25+ more
)
```

#### 4. **Default (Fallback)**

```kotlin
return "INR"  // Default for India region
```

---

## 💰 Amount Extraction

### Currency-Specific Amount Patterns

#### AED/GCC Currencies
```kotlin
"AED", "SAR", "QAR", "OMR", "KWD", "BHD" -> listOf(
    Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("(?:credited|debited)(?:\\s+to)?(?:\\s+A/C)?\\s+[^0-9]*${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
)
```

#### INR Patterns
```kotlin
listOf(
    Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("amt\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("amount\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("txn\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
)
```

---

## 📊 Database Schema

### TransactionEntity with Multi-Currency Support

```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: BigDecimal,
    val currency: String = "INR",
    val merchant: String,
    val date: LocalDateTime,
    val category: String,
    val type: TransactionType,
    val bankName: String,
    val cardLastFour: String? = null,
    val accountLastFour: String? = null,
    val description: String? = null,
    val transactionHash: String,
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### `getDistinctCurrencies()` (v2.2.2)

```kotlin
// TransactionRepository.kt
interface TransactionRepository {
    // ... existing methods
    fun getDistinctCurrencies(): Flow<List<String>>
}

// TransactionRepositoryImpl.kt
override fun getDistinctCurrencies(): Flow<List<String>> =
    transactionDao.getDistinctCurrencies()

// TransactionDao.kt
@Query("SELECT DISTINCT currency FROM transactions WHERE isDeleted = 0")
fun getDistinctCurrencies(): Flow<List<String>>
```

### Sample Queries with Currency Filter

```sql
-- Get all transactions in AED
SELECT * FROM transactions WHERE currency = 'AED' ORDER BY date DESC;

-- Get spending by currency
SELECT currency, SUM(amount) as total FROM transactions 
WHERE type = 'EXPENSE' GROUP BY currency;

-- Get transactions for Other tab (unmatched currencies)
SELECT * FROM transactions 
WHERE currency NOT IN ('INR','AED','SAR','QAR','OMR','KWD','BHD','USD','EUR','CHF','GBP','SGD','MYR','HKD','THB','IDR','PHP','VND','AUD','NZD','CAD')
ORDER BY date DESC;
```

---

## 🎨 UI & Display

### Currency Symbol in Total Spend Tile (v2.2.2)

```kotlin
// HomeScreenNew.kt
@Composable
fun QuickStatsRow(
    totalSpend: Double,
    currencySymbol: String = "₹",   // v2.2.2: parameterized
    // ...
) {
    Text("$currencySymbol${format(totalSpend)}")
}

// RegionalHomeScreen.kt
val primaryCurrency = filteredSummary.inrSummary?.currency ?: current.currencies.first()
val currencySymbol = CurrencySummary.getCurrencySymbol(primaryCurrency)
QuickStatsRow(currencySymbol = currencySymbol, ...)
```

### Net Balance Tile Flag/Label (v2.2.2)

```kotlin
// HomeScreenNew.kt
@Composable
fun MultiCurrencySummaryCard(
    primaryLabel: String = "🇮🇳 Indian",   // v2.2.2: parameterized
    // ...
) {
    Text(primaryLabel)
}

// RegionalHomeScreen.kt
val flag = current.label.split(" ").first()   // e.g. "🇦🇪"
val primaryLabel = "$flag $regionName ($primaryCurrencyCode)"
MultiCurrencySummaryCard(primaryLabel = primaryLabel, ...)
// UAE shows: "🇦🇪 UAE (AED)"
// USA shows: "🇺🇸 USA (USD)"
// India shows: "🇮🇳 India (INR)"
```

### Currency Formatter

```kotlin
object CurrencyFormatter {
    fun formatAmount(amount: BigDecimal, currency: String): String {
        return when (currency) {
            "INR" -> "₹${amount.setScale(2, RoundingMode.HALF_UP)}"
            "AED" -> "د.إ${amount.setScale(2, RoundingMode.HALF_UP)}"
            "USD" -> "$${amount.setScale(2, RoundingMode.HALF_UP)}"
            "EUR" -> "€${amount.setScale(2, RoundingMode.HALF_UP)}"
            "GBP" -> "£${amount.setScale(2, RoundingMode.HALF_UP)}"
            "SAR" -> "﷼${amount.setScale(2, RoundingMode.HALF_UP)}"
            "JPY" -> "¥${amount.setScale(0, RoundingMode.HALF_UP)}"
            else -> "$currency ${amount.setScale(2, RoundingMode.HALF_UP)}"
        }
    }
}
```

---

## 📋 Bank-Specific Parser Examples

### AED Parser (E& Money)

```kotlin
class EandMoneyParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean {
        return (sender.contains("ETISALAT", ignoreCase = true) || 
                sender.contains("E&", ignoreCase = true)) &&
               message.contains("e&", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = "AED"
        val merchant = extractMerchant(message, "E& Money")
        val cardLast4 = extractCardNumber(message)
        val type = ParserUtils.determineType(message)
        return ParsedTransaction(
            amount = amount, merchantName = merchant, bankName = "E& Money",
            transactionType = type, dateTime = LocalDateTime.now(),
            cardLast4 = cardLast4, rawMessage = message, currency = currency
        )
    }
}
```

### GenericBankParser (v2.2.2 — International SMS)

```kotlin
class GenericBankParser : BankParser {
    override fun canParse(sender: String, message: String): Boolean {
        val hasExplicitForeignCurrency = ParserUtils.extractCurrency(message)
            .let { it != "INR" && it.isNotEmpty() }
        // Accept international SMS with any explicit non-INR currency
        if (hasExplicitForeignCurrency) return hasTransactionKeyword(message)
        // Existing logic: require account/card reference for INR SMS
        return hasAccountReference(message) && hasTransactionKeyword(message)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val currency = ParserUtils.extractCurrency(message)  // 30+ currencies
        val amount = ParserUtils.extractAmount(message) ?: return null
        // ...
    }
}
```

---

## 🔮 Future Enhancements

### Phase 5 (Planned)

1. **Exchange Rate Support** — Manual entry, local cached rates
2. **Currency-Specific Formatting** — JPY no decimals, BHD 3 decimals
3. **Regional Category Mapping** — AED merchants → UAE categories
4. **Analytics by Currency** — Spending breakdown, currency-wise totals

### Phase 6 (Planned)

1. **Real Exchange Rates** — Optional monthly download
2. **Currency Conversion Tool** — Quick converter in app
3. **International Tax Support** — VAT/GST by region

---

## ✅ Testing

### Multi-Currency Test Cases

See [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md) for comprehensive SMS examples:

- ✅ AED (E&, Mashreq, Emirates NBD, FAB, ADIB)
- ✅ INR (HDFC, ICICI, SBI, Axis, Kotak)
- ✅ USD, EUR, GBP, SAR, JPY (International banks)
- ✅ MXN, ARS, KES, EGP, TWD, MMK, KHR, LAK (v2.2.2 new currencies)
- ✅ Digital Wallets (Google Pay, PhonePe, PayTm)

---

**Last Updated:** February 22, 2026  
**Version:** 2.2.2  
**Contact:** every.paisa.app@gmail.com
