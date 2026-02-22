# EveryPaisa Documentation Index

**Last Updated:** February 22, 2026  
**Version:** 2.0.0  
**Status:** ✅ Production-Ready

---

## 📚 Complete Documentation Map

### 🎯 For Users & Product Managers
Start here to understand what EveryPaisa is and what it does:

1. **[README.md](README.md)** — Project overview, features, tech stack
   - ✅ Multi-currency support (30+ currencies)
   - ✅ 40+ bank support
   - ✅ Installation & setup instructions
   - ✅ Contact: every.paisa.app@gmail.com

2. **[PRD.md](PRD.md)** — Complete product requirements
   - ✅ Executive summary
   - ✅ Problem statement & goals
   - ✅ Feature list by priority
   - ✅ Target audience (global)
   - ✅ Supported regions & currencies

3. **[QUICK_START.md](QUICK_START.md)** — Get started in 5 minutes
   - Installation steps
   - First launch setup
   - Basic usage guide

---

### 🏗️ For Developers & Architects
Detailed technical documentation:

1. **[ARCHITECTURE.md](ARCHITECTURE.md)** — System design & engineering guide (2,000 lines)
   - 🔍 Architecture overview & patterns
   - 📦 Module structure (app + parser-core)
   - 🗂️ Layer architecture (MVVM + Clean)
   - 💉 Dependency injection (Hilt)
   - 🗺️ Navigation architecture
   - 💾 Data layer (Room, TypeConverters)
   - 📨 SMS pipeline
   - 🎨 UI components (Compose)
   - ⚙️ State management (UDF)
   - 🔄 Background processing (WorkManager)
   - 🔐 Security architecture
   - 📝 Build configuration
   - 🧪 Testing strategy
   - 📂 File-by-file manifest

2. **[DESIGN_SPECIFICATIONS.md](DESIGN_SPECIFICATIONS.md)** — UI/UX design guide (1,000 lines)
   - 🎨 Material You color system
   - 🔤 Typography & spacing
   - 📱 Screen layouts
   - 🎯 Component designs
   - ♿ Accessibility guidelines

3. **[MULTI_CURRENCY_SUPPORT.md](MULTI_CURRENCY_SUPPORT.md)** — Currency implementation (600 lines)
   - 📋 All 30+ supported currencies with symbols
   - 🏦 Banks by region (40+ banks documented)
   - 🔍 Currency detection algorithm (4-level priority)
   - 💰 Amount extraction patterns
   - 🗄️ Database schema with currency support
   - 🎨 UI formatting & display
   - 💼 Bank-specific parser examples
   - 🔮 Future enhancements (Phase 5-6)

4. **[ARCHITECTURE.md](ARCHITECTURE.md) — Deep Dive Sections**
   - Data models and entity relationships
   - DAO queries and database design
   - Repository patterns
   - ViewModel state management
   - Compose screen structure

---

### 🧪 For QA & Testing
Test data and testing guides:

1. **[TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md)** — 50+ real SMS examples (400 lines)
   - 🇦🇪 **UAE Banks** (E&, Mashreq, Emirates NBD, FAB, ADIB)
   - 🇮🇳 **Indian Banks** (HDFC, ICICI, SBI, Axis, Kotak, etc.)
   - 🌐 **Multi-Currency Examples** (USD, EUR, GBP, SAR, JPY, etc.)
   - 💳 **Digital Wallets** (Google Pay, PhonePe, PayTm, Amazon Pay)
   - ❌ **Failed Transactions** (Refunds, declined cards, cancellations)
   - Expected parsing results for each SMS
   - Parser coverage table
   - Testing instructions
   - Expected outcomes

2. **[SMS_FILTERING_GUIDE.md](SMS_FILTERING_GUIDE.md)** — SMS filtering logic
   - Failed transaction handling
   - Non-transactional SMS filtering
   - OTP & security code detection
   - Promotional message filtering
   - Balance inquiry filtering

---

### 📊 Project Status & Updates

1. **[PROJECT_STATUS.md](PROJECT_STATUS.md)** — Development progress (310 lines)
   - ✅ Phases 0-4 completed
   - 📈 Project statistics (65+ files, 7,000+ LOC)
   - 🚀 Current capabilities
   - ⬜ Pending work (Phases 5-7)
   - 🧪 Test transactions section
   - 🔐 Privacy verification
   - 📂 Project file structure
   - 📝 Important notes

2. **[UPDATES_SUMMARY.md](UPDATES_SUMMARY.md)** — February 2026 updates (NEW)
   - Complete list of all documentation updates
   - 30+ supported currencies overview
   - 40+ supported banks
   - Contact information
   - Social media links
   - Next steps for testing & development

---

## 🌍 Regional Support Matrix

| Region | Status | Currencies | Banks | Examples |
|--------|--------|-----------|-------|----------|
| 🇮🇳 **India** | ✅ Active | INR | 25+ | [Test SMS](TEST_TRANSACTIONS.md#-indian-banks-inr) |
| 🇦🇪 **UAE** | ✅ Active | AED | 6 | [Test SMS](TEST_TRANSACTIONS.md#-uae-banks-aed) |
| 🇺🇸 **USA** | ✅ Active | USD | International | [Test SMS](TEST_TRANSACTIONS.md#usd-transactions) |
| 🇬🇧 **UK** | ✅ Active | GBP | International | [Test SMS](TEST_TRANSACTIONS.md#gbp-transactions) |
| 🇸🇦 **Saudi Arabia** | ✅ Active | SAR | SAB, others | [Test SMS](TEST_TRANSACTIONS.md#sar-transactions-saudi-arabia) |
| 🇯🇵 **Japan** | ✅ Active | JPY | International | [Test SMS](TEST_TRANSACTIONS.md#jpy-transactions-japan) |
| 🌐 **Other Regions** | ✅ Supported | 20+ | International | [See MULTI_CURRENCY_SUPPORT.md](MULTI_CURRENCY_SUPPORT.md) |

---

## 💡 Quick References

### For Adding New Banks
1. Study existing parsers in `BankParsers.kt`
2. Implement `BankParser` interface
3. Add to `BankParserFactory.parsers` list
4. Add SMS examples to [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md)
5. Update bank table in [MULTI_CURRENCY_SUPPORT.md](MULTI_CURRENCY_SUPPORT.md)

### For Adding New Currencies
1. Review [MULTI_CURRENCY_SUPPORT.md](MULTI_CURRENCY_SUPPORT.md#supported-currencies)
2. Add currency symbol to `symbolPatterns` in `BankParsers.kt`
3. Add currency code pattern to `codeWithAmountPatterns`
4. Add keyword to `currencyPatterns` map
5. Add test SMS to [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md)

### For Testing Multi-Currency
1. Open [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md)
2. Copy SMS text for desired currency
3. Send to test device (or paste into emulator)
4. Open EveryPaisa → tap "Scan SMS"
5. Verify transaction appears with correct:
   - ✅ Amount
   - ✅ Currency code
   - ✅ Currency symbol
   - ✅ Merchant name
   - ✅ Category

### For Device Testing
- **Phones:** All Android phones API 26+
- **Tablets:** Any Android tablet with SMS capability
- **Foldables:** Samsung Fold 7 (primary target)
- See [README.md](README.md#-supported-banks--regions) for supported devices

---

## 📞 Contact & Support

**Email:** [every.paisa.app@gmail.com](mailto:every.paisa.app@gmail.com)

**Social Media:**
- 🐦 **Twitter:** [@everypaisa_app](https://x.com/everypaisa_app)
- 📸 **Instagram:** [@every.paisa](https://www.instagram.com/every.paisa)

**GitHub Issues:** [Create an issue](https://github.com/everypaisa/everypaisa-android/issues)

---

## 🔄 Documentation Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.0 | Feb 22, 2026 | Multi-currency support, 40+ banks, contact info, test transactions |
| 1.0 | Feb 16, 2026 | Initial documentation (India-focused) |

---

## ✨ Key Statistics (v2.0)

- **Total Documentation:** 5,000+ lines across 10 markdown files
- **Code Files:** 65+
- **Lines of Code:** 7,000+
- **Supported Currencies:** 30+
- **Supported Banks:** 40+
- **Test SMS Examples:** 50+
- **Minimum Android API:** 26 (Android 8.0)
- **Target Android API:** 34+ (Android 14+)
- **Privacy Status:** 100% on-device, NO internet permission

---

## 🚀 What's Next?

### For Users
1. Download & install EveryPaisa
2. Grant SMS read permission
3. Tap "Scan SMS" to import transactions
4. View dashboard with multi-currency summary

### For Developers
1. **Phase 5:** Analytics screen with charts (Vico library)
2. **Phase 6:** Samsung Fold adaptive layouts
3. **Phase 7:** Release signing, Play Store optimization

### For Testers
1. Use [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md) for test cases
2. Test all 40+ banks and 30+ currencies
3. Verify device compatibility (phones, tablets, folds)
4. Check multi-currency display formatting
5. Test failed transaction handling

---

## 📖 Documentation Best Practices

- Always check [MULTI_CURRENCY_SUPPORT.md](MULTI_CURRENCY_SUPPORT.md) for technical implementation
- Use [TEST_TRANSACTIONS.md](TEST_TRANSACTIONS.md) for SMS format examples
- Refer to [ARCHITECTURE.md](ARCHITECTURE.md) for system design
- Check [PROJECT_STATUS.md](PROJECT_STATUS.md) for current progress
- See [README.md](README.md) for quick overview

---

**EveryPaisa** — Every paisa accounted for, automatically, in any currency.

**Last Updated:** February 22, 2026  
**Status:** ✅ Production Ready for Global Multi-Currency Expense Tracking
