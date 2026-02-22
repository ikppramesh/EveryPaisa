# Multi-Currency Support in EveryPaisa

This document details how EveryPaisa supports 30+ currencies across 40+ banks in multiple regions.

---

## 📋 Table of Contents

1. [Supported Currencies](#supported-currencies)
2. [Supported Banks by Region](#supported-banks-by-region)
3. [Currency Detection Implementation](#currency-detection-implementation)
4. [Amount Extraction](#amount-extraction)
5. [Database Schema](#database-schema)
6. [UI & Display](#ui--display)
7. [Future Enhancements](#future-enhancements)

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

### Others
| Code | Name | Symbol | Regions |
|------|------|--------|---------|
| CHF | Swiss Franc | CHF | 🇨🇭 Switzerland |
| KRW | South Korean Won | ₩ | 🇰🇷 South Korea |
| TRY | Turkish Lira | ₺ | 🇹🇷 Turkey |
| RUB | Russian Ruble | ₽ | 🇷🇺 Russia |
| ZAR | South African Rand | R | 🇿🇦 South Africa |
| BRL | Brazilian Real | R$ | 🇧🇷 Brazil |
| MXN | Mexican Peso | $ | 🇲🇽 Mexico |
| ETB | Ethiopian Birr | ብር | 🇪🇹 Ethiopia |
| NGN | Nigerian Naira | ₦ | 🇳🇬 Nigeria |

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

## 🔍 Currency Detection Implementation

### Detection Strategy (Priority Order)

**File:** `parser-core/src/main/java/com/everypaisa/parser/BankParsers.kt`

#### 1. **Currency Symbols (Highest Priority)**

```kotlin
// PRIORITY 1: Check for currency symbols directly attached to amounts
val symbolPatterns = listOf(
    "₹\\s*[\\d,]+\\.?\\d*" to "INR",      // ₹2000.50
    "\\$\\s*[\\d,]+\\.?\\d*" to "USD",    // $100.00
    "€\\s*[\\d,]+\\.?\\d*" to "EUR",      // €50.00
    "£\\s*[\\d,]+\\.?\\d*" to "GBP",      // £32.50
    "د\\.إ\\s*[\\d,]+\\.?\\d*" to "AED",  // د.إ 150.00
    "﷼\\s*[\\d,]+\\.?\\d*" to "SAR",      // ﷼ 149.99
    "¥\\s*[\\d,]+\\.?\\d*" to "JPY",      // ¥10000
    "₨\\s*[\\d,]+\\.?\\d*" to "NPR"       // ₨500
)
```

#### 2. **Currency Codes with Amounts**

```kotlin
// PRIORITY 2: Check for currency codes directly before amounts
val codeWithAmountPatterns = listOf(
    "USD\\s+[\\d,]+\\.?\\d*" to "USD",    // USD 100.00
    "AED\\s+[\\d,]+\\.?\\d*" to "AED",    // AED 31.89
    "EUR\\s+[\\d,]+\\.?\\d*" to "EUR",    // EUR 50.00
    "GBP\\s+[\\d,]+\\.?\\d*" to "GBP",    // GBP 32.50
    "SAR\\s+[\\d,]+\\.?\\d*" to "SAR",    // SAR 149.99
    // ... 25+ more currencies
)
```

#### 3. **Currency Keywords & Context**

```kotlin
// PRIORITY 3: Check for explicit currency keywords
val currencyPatterns = mapOf(
    "aed" to "AED",         // "aed 150"
    "dirham" to "AED",      // "150 dirham"
    "dubai" to "AED",       // Location hint
    "uae" to "AED",         // Location hint
    "inr" to "INR",         // "150 INR"
    "rupee" to "INR",       // "150 rupees"
    "rs." to "INR",         // "Rs. 150"
    "rs " to "INR",         // "Rs 150"
    "dollar" to "USD",      // "100 dollars"
    "euro" to "EUR",        // "50 euros"
    // ... 25+ more keywords
)
```

#### 4. **Default (Fallback)**

```kotlin
// PRIORITY 4: Rs/INR for Indian SMS (most common)
if (lower.contains("rs.") || lower.contains("rs ") || lower.contains("inr")) {
    return "INR"
}

// Final fallback
return "INR"  // Default for India region
```

---

## 💰 Amount Extraction

### Currency-Specific Amount Patterns

The parser tries currency-specific regex patterns to extract amounts correctly:

#### AED/GCC Currencies
```kotlin
"AED", "SAR", "QAR", "OMR", "KWD", "BHD" -> listOf(
    Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("(?:credited|debited)(?:\\s+to)?(?:\\s+A/C)?\\s+[^0-9]*${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
)
```

**Example:**
- Input: `"AED 31.89 was successfully completed"`
- Pattern 1: `AED\\s*([\\d,]+\\.?\\d*)` matches
- Extracted: `31.89`

#### INR Patterns
```kotlin
listOf(
    Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("amt\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("amount\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("txn\\s*(?:of\\s*)?(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
)
```

**Examples:**
- `"Rs. 2250.00 debited"` → Extracts `2250.00`
- `"₹500 spent"` → Extracts `500`
- `"amount INR 1500"` → Extracts `1500`

#### International Currencies (USD, EUR, GBP)
```kotlin
"USD", "CAD", "AUD", "NZD", "SGD", "HKD" -> listOf(
    Pattern.compile("${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
    Pattern.compile("\\$\\s*([\\d,]+\\.?\\d*)"),
    Pattern.compile("(?:amt|amount)\\s*(?:of\\s*)?${currency}\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
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
    val amount: BigDecimal,           // Amount in original currency
    val currency: String = "INR",      // Currency code (AED, USD, etc.)
    val merchant: String,
    val date: LocalDateTime,
    val category: String,
    val type: TransactionType,
    val bankName: String,
    val cardLastFour: String? = null,
    val accountLastFour: String? = null,
    val description: String? = null,
    val transactionHash: String,       // SHA-256 for deduplication
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### Sample Queries with Currency Filter

```sql
-- Get all transactions in AED
SELECT * FROM transactions WHERE currency = 'AED' ORDER BY date DESC;

-- Get spending by currency
SELECT currency, SUM(amount) as total FROM transactions 
WHERE type = 'EXPENSE' GROUP BY currency;

-- Get transactions for a specific region
SELECT * FROM transactions 
WHERE currency IN ('AED', 'SAR', 'OMR') AND type = 'EXPENSE'
ORDER BY date DESC LIMIT 100;
```

---

## 🎨 UI & Display

### Currency Formatting

**File:** `app/src/main/java/com/everypaisa/tracker/utils/Formatters.kt`

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
            "JPY" -> "¥${amount.setScale(0, RoundingMode.HALF_UP)}" // No decimal for JPY
            else -> "$currency ${amount.setScale(2, RoundingMode.HALF_UP)}"
        }
    }
    
    fun getCurrencyFlag(currency: String): String {
        return when (currency) {
            "AED" -> "🇦🇪"
            "INR" -> "🇮🇳"
            "USD" -> "🇺🇸"
            "EUR" -> "🇪🇺"
            "GBP" -> "🇬🇧"
            "SAR" -> "🇸🇦"
            "JPY" -> "🇯🇵"
            else -> ""
        }
    }
}
```

### Display Examples

**Home Dashboard:**
```
💰 Monthly Summary
────────────────────
Income:  ₹ 75,000.00 (INR)
         د.إ 2,000.00 (AED)
         $ 500.00 (USD)

Expense: ₹ 35,000.00 (INR)
         د.إ 1,500.00 (AED)
         $ 200.00 (USD)
```

**Transaction List:**
```
🇦🇪 د.إ 31.89  Amazon.ae          21 Feb, 4:08 PM
🇮🇳 ₹ 2,250    Swiggy             21 Feb, 3:45 PM
🇺🇸 $ 45.99    Netflix            21 Feb, 10:00 AM
🇮🇳 ₹ 15,000   Loan EMI          20 Feb, 6:00 PM
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
        
        // E& Money SMS always has AED
        val currency = "AED"
        
        val merchant = extractMerchant(message, "E& Money")
        val cardLast4 = extractCardNumber(message)
        val type = ParserUtils.determineType(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "E& Money",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency  // Always AED for E& Money
        )
    }
}
```

### Mashreq Parser

```kotlin
class MashreqParser : BankParser {
    
    override fun canParse(sender: String, message: String): Boolean {
        return sender.contains("MASHREQ", ignoreCase = true) ||
               message.contains("NEO VISA", ignoreCase = true)
    }
    
    override fun parse(sender: String, message: String): ParsedTransaction? {
        val amount = ParserUtils.extractAmount(message) ?: return null
        val currency = ParserUtils.extractCurrency(message) // Auto-detect (usually AED)
        
        val merchant = extractMerchant(message, "Mashreq")
        val cardLast4 = extractCardNumber(message)
        val type = determineTransactionType(message)
        
        return ParsedTransaction(
            amount = amount,
            merchantName = merchant,
            bankName = "Mashreq",
            transactionType = type,
            dateTime = LocalDateTime.now(),
            cardLast4 = cardLast4,
            rawMessage = message,
            currency = currency  // Usually AED, but supports multi-currency
        )
    }
}
```

---

## 🔮 Future Enhancements

### Phase 5 (Planned)

1. **Exchange Rate Support**
   - Manual exchange rate entry
   - Local cached rates (no internet)
   - Multi-currency wallet conversion

2. **Currency-Specific Formatting**
   - JPY: No decimal places (¥10000)
   - BHD: 3 decimal places
   - Others: 2 decimal places

3. **Regional Category Mapping**
   - AED merchants → UAE categories
   - INR merchants → India categories
   - USD merchants → International categories

4. **Analytics by Currency**
   - Spending breakdown by currency
   - Currency-wise totals
   - Multi-currency period comparison

### Phase 6 (Planned)

1. **Real Exchange Rates**
   - Optional: Download rates once monthly
   - Show "converted" amounts
   - Budget tracking across currencies

2. **Currency Conversion Tool**
   - Quick converter in app
   - Historical rates
   - Multi-currency balance view

3. **International Tax Support**
   - VAT/GST calculation by region
   - Expense categorization by tax rules
   - Multi-region tax reports

---

## ✅ Testing

### Multi-Currency Test Cases

See [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md) for comprehensive SMS examples:

- ✅ AED (E&, Mashreq, Emirates NBD, FAB, ADIB)
- ✅ INR (HDFC, ICICI, SBI, Axis, Kotak)
- ✅ USD (International banks)
- ✅ EUR, GBP, SAR, JPY (Examples provided)
- ✅ Digital Wallets (Google Pay, PhonePe, PayTm)

---

**Last Updated:** February 22, 2026  
**Version:** 2.0  
**Contact:** every.paisa.app@gmail.com
