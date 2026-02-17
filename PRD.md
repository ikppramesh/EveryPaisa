# Product Requirements Document (PRD)
# Everypaisa — SMS-Powered Expense Tracker for Android

**Version:** 1.0  
**Date:** February 16, 2026  
**Status:** Draft  
**License:** Proprietary

---

## Table of Contents
1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Goals & Objectives](#3-goals--objectives)
4. [Target Audience & Personas](#4-target-audience--personas)
5. [Feature Requirements (Prioritized)](#5-feature-requirements)
6. [Screens & Navigation Map](#6-screens--navigation-map)
7. [Technical Architecture](#7-technical-architecture)
8. [Data Model & Database Schema](#8-data-model--database-schema)
9. [SMS Parsing Engine](#9-sms-parsing-engine)
10. [AI Assistant (On-Device)](#10-ai-assistant-on-device)
11. [Privacy & Security](#11-privacy--security)
12. [Performance Requirements](#12-performance-requirements)
13. [Success Metrics](#13-success-metrics)
14. [Phased Delivery Plan](#14-phased-delivery-plan)
15. [Risks & Mitigations](#15-risks--mitigations)
16. [Appendix](#appendix)

---

## 1. Executive Summary

**Product Name:** Everypaisa  
**Tagline:** *Every paisa accounted for — automatically.*

**What It Does:**  
Everypaisa is a privacy-first Android app that automatically reads bank SMS messages and transforms them into a clean, searchable money timeline with on-device AI assistance. It provides comprehensive expense dashboards (daily, weekly, monthly), intelligent categorization, subscription tracking, budget management, and an AI chat assistant — all without any data ever leaving the user's device.

**Key Differentiators:**
- 100% on-device processing (no cloud, no servers, no tracking)
- Zero manual entry — fully automatic SMS parsing
- On-device AI assistant (MediaPipe/Qwen 2.5) for natural-language financial queries
- Support for 35+ Indian banks, UPI platforms, credit cards
- Multi-currency support with exchange rates
- Subscription/recurring payment detection
- Budget groups with category-level spending tracking
- Modern Material You design with dynamic theming

---

## 2. Problem Statement

### The User's Pain
| Pain Point | Impact | Current Workaround |
|---|---|---|
| Manual expense entry is tedious | Users abandon tracking within a week | Spreadsheets, notes apps |
| No unified view across banks | Fragmented picture of finances | Switching between 3-4 banking apps |
| Privacy concerns with cloud-based trackers | Sensitive data exposed to servers | Avoid using expense apps entirely |
| Subscription creep goes unnoticed | Users overpay ₹1000-3000/month on unused services | Manually check bank statements |
| No contextual insights | Users can't ask "How much did I spend on food this month?" | Manual calculations |
| Budget limits are hard to enforce | Users overspend without awareness | Mental tracking |

### Our Solution
An intelligent, privacy-first Android app that:
1. **Automatically** reads bank transaction SMS
2. **Instantly** categorizes and organizes expenses
3. **Visualizes** spending with charts and analytics
4. **Detects** subscriptions and recurring charges
5. **Answers** natural-language finance questions via on-device AI
6. **Never** sends any data to any server

---

## 3. Goals & Objectives

### Business Goals
| Goal | Metric | Target |
|---|---|---|
| User acquisition | Play Store downloads (first 6 months) | 50,000+ |
| User retention | Day-30 retention rate | >50% |
| User satisfaction | Play Store rating | 4.5+ stars |
| Privacy trust | "No data collected" safety label | Maintained always |

### Product Goals
| Goal | Measurement |
|---|---|
| Zero manual entry | >95% of bank transactions auto-captured |
| Instant insights | Dashboard loads in <2 seconds |
| Bank coverage | Support 35+ Indian banks at launch |
| Privacy | 100% on-device processing verified |
| Smart categorization | >90% auto-categorization accuracy |
| Subscription detection | Detect recurring payments within 2 billing cycles |

---

## 4. Target Audience & Personas

### Geographic Focus
- **Primary:** India (SMS-based transaction alerts are standard across all banks)
- **Future:** UAE, Nepal, USA, Thailand, Ethiopia, Kenya

### Persona 1: "Priya" — Busy Tech Professional
| Attribute | Detail |
|---|---|
| Age | 26-32 |
| Income | ₹10-25 LPA |
| Payment Methods | 2-3 UPI apps, 1-2 credit cards, debit card |
| Pain | Uses GPay, PhonePe, Amazon Pay — loses track of total spend |
| Goal | "Show me where my money goes without me doing anything" |
| Key Features | Auto-tracking, analytics, AI chat, budget alerts |

### Persona 2: "Rahul" — Budget-Conscious Family Man
| Attribute | Detail |
|---|---|
| Age | 33-42 |
| Income | ₹6-12 LPA |
| Payment Methods | UPI, 1 debit card, occasional net banking |
| Pain | Needs to stick to monthly household budget |
| Goal | "Alert me when I'm overspending before it's too late" |
| Key Features | Monthly budget, category limits, daily allowance, subscription tracking |

### Persona 3: "Sneha" — Privacy-Conscious Student
| Attribute | Detail |
|---|---|
| Age | 18-24 |
| Income | ₹1-3 LPA (stipend/part-time) |
| Payment Methods | Primarily UPI |
| Pain | Worried about finance apps selling data |
| Goal | "Track my spending without ANY data leaving my phone" |
| Key Features | 100% offline, no cloud, privacy guarantee |

---

## 5. Feature Requirements

### 5.1 Core Features (MVP — Phase 1)

#### F1: SMS Permission & Reading
**Priority:** P0 (Critical)  
**Screen:** Permission Screen

| Requirement | Details |
|---|---|
| Permissions | `READ_SMS`, `RECEIVE_SMS`, `POST_NOTIFICATIONS` (Android 13+) |
| Education | Full-screen explanation of WHY permission is needed with privacy guarantees |
| Denial handling | Graceful fallback with ability to re-request |
| SMS monitoring | `BroadcastReceiver` for real-time new SMS + `ContentResolver` for history scan |
| Auto-scan on launch | Scan for new messages every time app opens |
| Scan range | Configurable: Last 30 days, 90 days, 6 months, 1 year, All time |

#### F2: SMS Transaction Parser Engine
**Priority:** P0 (Critical)  
**Module:** `parser-core` (separate Kotlin module)

| Requirement | Details |
|---|---|
| Architecture | Abstract `BankParser` interface with per-bank implementations |
| Extraction | Amount, merchant, date/time, transaction type, account/card last 4 digits, bank name, UPI ref |
| Deduplication | SHA-256 hash of (amount + merchant + timestamp + bank) for `transaction_hash` unique index |
| Transaction types | EXPENSE, INCOME, CREDIT (card), TRANSFER, INVESTMENT |
| Currency detection | Auto-detect INR (₹, Rs, INR), USD ($), AED (د.إ), NPR (₨), ETB (ብር) |
| Unrecognized SMS | Store unparseable bank SMS separately for future parser improvements |
| Error handling | Graceful — never crash on malformed SMS |

**Supported Banks (Launch):**

| Category | Banks |
|---|---|
| Major Private | HDFC, ICICI, Axis, Kotak, IDFC First, Yes, IndusInd, Federal |
| Public Sector | SBI, PNB, Canara, Bank of Baroda, Bank of India, Indian Bank, Union Bank, Central Bank, IOB |
| Digital/Neo | Jupiter (CSB), AMEX, OneCard, AU Bank |
| UPI Platforms | Google Pay, PhonePe, Paytm, Amazon Pay, BHIM |
| Others | HSBC, IDBI, Karnataka Bank, South Indian Bank, JK Bank, UCO Bank, Airtel Payments Bank |

#### F3: Home Dashboard Screen
**Priority:** P0 (Critical)  
**Route:** `home`

**Layout (top to bottom):**
1. **App Bar** — "Everypaisa" title, Settings gear icon
2. **Month Summary Card** — Total expenses for current month with trend indicator (↑/↓ vs previous month)
3. **Period Tabs** — Swipeable pages: Today | This Week | This Month via `HorizontalPager`
4. **Category Filter Chips** — Horizontal scrollable chips to filter by category
5. **Account Summary Cards** — Bank-wise account totals (tappable for drill-down)
6. **Budget Progress** — If budget set, show progress bar with % used, daily allowance
7. **Recent Transactions** — Grouped by date, showing merchant, amount, category icon, payment method
8. **FAB** — Scan SMS button (with spotlight tutorial on first launch)

**Interactions:**
- Pull-to-refresh triggers SMS re-scan
- Long-press transaction → delete with undo snackbar
- Tap transaction → navigate to TransactionDetail
- Tap category chip → filter transactions
- Tap "View All" → navigate to Transactions screen

#### F4: Transaction List Screen
**Priority:** P0 (Critical)  
**Route:** `transactions`

| Feature | Details |
|---|---|
| Grouping | Chronological, grouped by date with daily totals |
| Period filters | Chips: Today, This Week, This Month, Last Month, 3 Months, 6 Months, Year, All Time |
| Category filter | Dropdown with all categories |
| Transaction type filter | All, Expense, Income, Credit, Transfer, Investment |
| Payment method filter | UPI, Debit Card, Credit Card, Net Banking, Wallet |
| Currency filter | Auto-populated from user's transactions |
| Sort | By date (default), by amount |
| Search | Real-time search by merchant name, description, SMS body |
| Multi-currency totals | Show grouped totals per currency for selected period |
| Per-item display | Category icon, merchant name, amount (color-coded), time, payment method |

#### F5: Transaction Detail Screen
**Priority:** P0 (Critical)  
**Route:** `transactionDetail/{transactionId}`

| Feature | Details |
|---|---|
| Display | Amount (large), merchant, category with color, date/time, payment method, bank, account |
| Edit category | Dropdown to reassign category |
| Edit merchant name | Inline text edit |
| Add notes | Free text field |
| Transaction splits | Split one transaction across multiple categories (e.g., ₹5000 grocery → ₹3000 Food + ₹2000 Household) |
| Original SMS | Show raw SMS body (expandable) |
| Merchant mapping | Option to "Apply to all transactions from this merchant" |
| Delete | Soft-delete with confirmation |

#### F6: Data Persistence (Room Database)
**Priority:** P0 (Critical)

| Requirement | Details |
|---|---|
| Database | Room (SQLite) with TypeConverters for BigDecimal, LocalDateTime, enums |
| Tables | transactions, subscriptions, categories, merchant_mappings, cards, account_balances, chat_messages, transaction_rules, rule_applications, exchange_rates, budgets, budget_categories, transaction_splits, category_budget_limits, unrecognized_sms |
| Migrations | Auto-migrations with manual fallback for complex schema changes |
| Indexing | `transaction_hash` (unique), `date_time`, `category`, `merchant_name`, `currency` |
| Soft delete | `is_deleted` flag instead of hard delete for transactions |
| Preferences | DataStore (Protobuf) for user settings: theme, default currency, budget limit, scan range |

### 5.2 Enhanced Features (Phase 2)

#### F7: Analytics Screen
**Priority:** P1 (High)  
**Route:** `analytics`

| Feature | Details |
|---|---|
| Period selector | Week, Month, Year, Custom date range |
| Transaction type toggle | Expense, Income, Credit |
| Currency filter | Filter analytics by currency |
| Spending trend chart | Line/bar chart showing daily spending over selected period |
| Category breakdown | Horizontal bar chart with category name, amount, percentage, transaction count |
| Top merchants | Ranked list of highest-spend merchants with amounts and % of total |
| Comparison | Current period vs previous period delta |
| Tap-through | Tap any category/merchant → navigate to filtered transaction list |
| Navigate to AI Chat | Button to ask follow-up questions via AI |

#### F8: Auto-Categorization
**Priority:** P1 (High)

| Feature | Details |
|---|---|
| Rule-based | Merchant name → category mapping (e.g., "Swiggy" → "Food & Dining") |
| Merchant mappings table | User overrides persist — "always categorize X as Y" |
| Default categories | Food & Dining, Groceries, Shopping, Transportation, Bills & Utilities, Entertainment, Healthcare, Education, Personal Care, Travel, Investments, Subscriptions, Transfers, Salary, Refunds, Cashback, Interest, Dividends, Income, Others |
| Custom categories | Users can create new categories with custom name + color |
| Category colors | Each category has an assigned hex color for charts and chips |
| Income detection | Salary, refund, cashback, interest, dividend auto-detection from merchant name |

#### F9: Subscription Detection & Tracking
**Priority:** P1 (High)  
**Route:** `subscriptions`

| Feature | Details |
|---|---|
| Auto-detection | Identify recurring payments (same merchant + similar amount + monthly interval) |
| E-Mandate parsing | Extract UMN (Unique Mandate Number) from bank SMS |
| States | Active, Paused, Cancelled, Hidden |
| Display | Merchant, amount, next payment date, billing cycle |
| Upcoming alerts | Highlight subscriptions due within 7 days |
| Add manual | User can manually add subscriptions |
| Monthly total | Sum of all active subscription costs |
| Category | Auto-categorized, user-editable |

#### F10: Budget Management
**Priority:** P1 (High)  
**Routes:** `budgetGroups`, `monthlyBudgetSettings`

| Feature | Details |
|---|---|
| Monthly budget | Set overall monthly spending limit |
| Category budgets | Set per-category limits (e.g., Food: ₹8000, Transport: ₹3000) |
| Budget groups | Create named budget groups with selected categories and limits |
| Progress tracking | Real-time progress bar: spent vs limit with percentage |
| Daily allowance | Calculate remaining budget / remaining days = daily allowance |
| Overspend alerts | Notifications at 80%, 100% of budget |
| Budget vs actual | Visual comparison chart |
| Period support | Monthly budgets with auto-renewal |
| Budget drag-to-reorder | Drag to reorder budget groups |
| Navigation | Tap budget category → filtered transaction list |

#### F11: Smart Rules Engine
**Priority:** P1 (High)  
**Routes:** `rules`, `createRule`

| Feature | Details |
|---|---|
| Rule definition | IF conditions (merchant contains, amount >, category is) THEN actions (set category, add tag) |
| Priority ordering | Rules apply in priority order |
| Batch apply | Apply a rule retroactively to all matching historical transactions |
| Rule templates | Pre-built system rules for common patterns |
| Active/Inactive toggle | Enable/disable rules without deleting |
| Rule applications log | Track which rules were applied to which transactions |

### 5.3 Advanced Features (Phase 3)

#### F12: On-Device AI Chat Assistant
**Priority:** P2 (Medium)  
**Route:** `chat`

| Feature | Details |
|---|---|
| AI model | MediaPipe LLM with Qwen 2.5 (on-device, ~1GB model) |
| Natural language queries | "What did I spend on food last month?", "Compare my spending this month vs last month" |
| Context injection | Feed AI with: month summary, recent transactions, top categories, active subscriptions, quick stats |
| Chat history | Persist conversations in Room database |
| Developer mode | Toggle to see raw AI prompts and token usage |
| Token tracking | Show tokens used per query |
| Chat stats | Display model status (downloading, ready, error) |
| Suggested prompts | Quick-tap suggestion chips for common queries |
| Model management | Download/delete model from settings |

#### F13: Account Management
**Priority:** P2 (Medium)  
**Routes:** `manage_accounts`, `add_account`, `accountDetail/{bankName}/{accountLast4}`

| Feature | Details |
|---|---|
| Auto-detection | Detect bank accounts from parsed SMS (bank + last 4 digits) |
| Balance tracking | Store and update account balances from SMS |
| Manual accounts | Add accounts manually with opening balance |
| Account detail | Drill-down view: transactions for a specific account with date range filter |
| Account types | Savings, Current, Credit Card |
| Balance history | Track balance changes over time |

#### F14: Data Export
**Priority:** P2 (Medium)

| Feature | Details |
|---|---|
| CSV export | Export transactions with all fields, date range selectable |
| PDF reports | Monthly summary reports with charts |
| Share | Share via email, WhatsApp, etc. |
| Backup/Restore | Full database backup to local storage |
| Backup metadata | App version, device info, transaction count, date range |

#### F15: Multi-Currency Support
**Priority:** P2 (Medium)  
**Route:** `exchangeRates`

| Feature | Details |
|---|---|
| Auto-detection | Detect currency from SMS (₹, $, د.إ, etc.) |
| Exchange rates | View and manually set exchange rates |
| Unified currency mode | Convert all amounts to a single display currency |
| Custom exchange rates | Override with user-defined rates |
| Per-currency analytics | Filter all analytics by currency |

#### F16: App Lock (Biometric)
**Priority:** P2 (Medium)  
**Route:** `appLock`

| Feature | Details |
|---|---|
| Biometric | Fingerprint/Face unlock via BiometricPrompt |
| Fallback | Device PIN/Pattern fallback |
| Auto-lock | Lock when app goes to background |
| Settings toggle | Enable/disable from settings |

### 5.4 Polish Features (Phase 4)

#### F17: Dark Mode & Dynamic Theming
**Priority:** P2

| Feature | Details |
|---|---|
| System-follow | Auto dark/light based on system setting |
| Manual toggle | Override to always dark or always light |
| Material You | Dynamic color from wallpaper (Android 12+) |
| AMOLED dark | True black dark mode option |

#### F18: Home Screen Widgets
**Priority:** P3 (Low)

| Feature | Details |
|---|---|
| Daily spending widget | Glanceable card with today's total |
| Recent transactions widget | Last 3-5 transactions |
| Budget widget | Budget progress bar |
| Quick-add widget | Shortcut to add manual transaction |

#### F19: Spotlight Tutorial / Onboarding
**Priority:** P2

| Feature | Details |
|---|---|
| First-launch flow | Permission → Scan → Dashboard walkthrough |
| Spotlight overlay | Highlight FAB scan button with animated tooltip |
| Progressive | Dismiss after first interaction |
| What's New dialog | Show changelog after app update |

#### F20: Unrecognized SMS Management
**Priority:** P3 (Low)  
**Route:** `unrecognized_sms`

| Feature | Details |
|---|---|
| View | List of bank SMS that couldn't be parsed |
| Report | One-tap to report/request bank support |
| Count | Badge showing number of unrecognized SMS |
| Retry | Re-process when parser is updated |

#### F21: FAQ Screen
**Priority:** P3 (Low)  
**Route:** `faq`

| Feature | Details |
|---|---|
| Content | Common questions about privacy, SMS access, data storage |
| Expandable | Accordion-style Q&A |
| Links | Links to privacy policy, support channels |

---

## 6. Screens & Navigation Map

### 6.1 Navigation Architecture

**Pattern:** Single Activity + Compose Navigation  
**Two-level navigation:**

```
Root NavHost (EveryPaisaNavHost)
├── AppLock Screen (if biometric enabled)
├── Permission Screen (first launch only)
├── Home (MainScreen with nested NavHost)
│   ├── Bottom Nav: Home | Analytics | Chat
│   ├── home (HomeScreen)
│   ├── analytics (AnalyticsScreen)
│   ├── chat (ChatScreen)
│   ├── transactions (TransactionsScreen)
│   ├── subscriptions (SubscriptionsScreen)
│   ├── settings (SettingsScreen)
│   │   ├── categories (CategoriesScreen)
│   │   ├── unrecognized_sms (UnrecognizedSmsScreen)
│   │   ├── manage_accounts (ManageAccountsScreen)
│   │   │   └── add_account (AddAccountScreen)
│   │   └── faq (FAQScreen)
│   └── (future screens nest here)
├── TransactionDetail/{id}
├── AddTransaction
├── AccountDetail/{bankName}/{accountLast4}
├── Rules
│   └── CreateRule/{ruleId?}
├── BudgetGroups
├── MonthlyBudgetSettings
└── ExchangeRates
```

### 6.2 Bottom Navigation Tabs

| Tab | Icon | Route | Screen |
|---|---|---|---|
| Home | `Icons.Default.Home` | `home` | HomeScreen |
| Analytics | `Icons.Default.Analytics` | `analytics` | AnalyticsScreen |
| Chat | `Icons.AutoMirrored.Filled.Chat` | `chat` | ChatScreen |

### 6.3 Screen Inventory (21 Screens)

| # | Screen | Route | Priority |
|---|---|---|---|
| 1 | Splash / App Lock | `appLock` | P0 |
| 2 | Permission | `permission` | P0 |
| 3 | Home Dashboard | `home` | P0 |
| 4 | Transactions List | `transactions` | P0 |
| 5 | Transaction Detail | `transactionDetail/{id}` | P0 |
| 6 | Add Transaction | `addTransaction` | P1 |
| 7 | Analytics | `analytics` | P1 |
| 8 | AI Chat | `chat` | P2 |
| 9 | Subscriptions | `subscriptions` | P1 |
| 10 | Settings | `settings` | P0 |
| 11 | Categories Management | `categories` | P1 |
| 12 | Budget Groups | `budgetGroups` | P1 |
| 13 | Monthly Budget Settings | `monthlyBudgetSettings` | P1 |
| 14 | Account Detail | `accountDetail/{bank}/{last4}` | P2 |
| 15 | Manage Accounts | `manage_accounts` | P2 |
| 16 | Add Account | `add_account` | P2 |
| 17 | Smart Rules | `rules` | P1 |
| 18 | Create/Edit Rule | `createRule/{ruleId?}` | P1 |
| 19 | Exchange Rates | `exchangeRates` | P2 |
| 20 | Unrecognized SMS | `unrecognized_sms` | P3 |
| 21 | FAQ | `faq` | P3 |

### 6.4 Screen Wireframes

#### Home Dashboard (Primary Screen)
```
┌──────────────────────────────────────────┐
│  Everypaisa                     ⚙️       │ TopAppBar
├──────────────────────────────────────────┤
│                                          │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  │
│  ┃  February 2026                     ┃  │
│  ┃  ₹ 28,560                          ┃  │ Month Summary
│  ┃  ▲ 5% vs last month    45 txns    ┃  │ (Hero Card)
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  │
│                                          │
│  [Today] [This Week] [This Month]        │ HorizontalPager
│                                          │
│  [All] [Food] [Shopping] [Transport]→    │ Category Chips
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 💰 Budget: ₹40,000                │  │
│  │ ████████████░░░░░░ 71% used        │  │ Budget Card
│  │ Daily allowance: ₹763              │  │
│  └────────────────────────────────────┘  │
│                                          │
│  TODAY                          ₹1,450  │
│  ─────────────────────────────────────   │
│  🍔 Swiggy          Food     -₹320     │
│      1:30 PM • GPay                     │ Transactions
│  🛒 Amazon          Shop    -₹1,250    │ (LazyColumn)
│      11:20 AM • ICICI XX5678            │
│                                          │
│  YESTERDAY                      ₹2,150  │
│  ─────────────────────────────────────   │
│  🎬 Netflix        Subs      -₹499     │
│      Auto-debit • HDFC XX1234           │
│                                          │
│            [View All →]            [🔄]  │ FAB = Scan
│                                          │
├──────────────────────────────────────────┤
│  🏠 Home    📊 Analytics    💬 Chat      │ BottomNav
└──────────────────────────────────────────┘
```

#### Analytics Screen
```
┌──────────────────────────────────────────┐
│  Everypaisa                     ⚙️       │
├──────────────────────────────────────────┤
│  [Week] [Month] [Year] [Custom]          │
│  [Expense ▾] [All Currencies ▾]         │
│                                          │
│  SPENDING TREND                          │
│  ┌────────────────────────────────────┐  │
│  │  5k│     ╱╲                        │  │
│  │  3k│    ╱  ╲   ╱╲                  │  │ Line Chart
│  │  1k│   ╱    ╲─╱  ╲                 │  │
│  │    └─────────────────────          │  │
│  │     1   5   10  15  20  25         │  │
│  └────────────────────────────────────┘  │
│                                          │
│  CATEGORY BREAKDOWN                      │
│  Food & Dining     ████████  ₹9,800 35% │
│  Shopping          ██████    ₹7,840 28% │ Horizontal
│  Transportation    █████     ₹5,600 20% │ Bar Chart
│  Others            ████      ₹5,320 17% │
│                                          │
│  TOP MERCHANTS                           │
│  1. Amazon        ₹3,450  12%  (8 txns) │
│  2. Swiggy        ₹2,800  10% (14 txns) │
│  3. Uber          ₹1,920   7%  (9 txns) │
│                                          │
│          [💬 Ask AI about this]          │
│                                          │
├──────────────────────────────────────────┤
│  🏠 Home    📊 Analytics    💬 Chat      │
└──────────────────────────────────────────┘
```

#### AI Chat Screen
```
┌──────────────────────────────────────────┐
│  Everypaisa                     ⚙️       │
├──────────────────────────────────────────┤
│                                          │
│  🤖 Hi! Ask me about your finances.     │
│     I process everything on your phone.  │
│                                          │
│  [How much on food?] [Top expenses?]     │ Suggestion
│  [Budget status?] [vs last month?]       │ Chips
│                                          │
│  👤 What did I spend on food this month? │
│                                          │
│  🤖 This month (Feb 1-16, 2026), you    │
│     spent ₹9,800 on Food & Dining       │ AI Response
│     across 14 transactions.              │ (streaming)
│     Your top food merchants:             │
│     • Swiggy: ₹2,800 (8 orders)         │
│     • Zomato: ₹1,950 (5 orders)         │
│     • Dominos: ₹1,250 (2 orders)        │
│     This is 5% more than last month.     │
│                                          │
│  👤 Am I over budget?                    │
│                                          │
│  🤖 Your food budget is ₹8,000/month.   │
│     You've spent ₹9,800 — that's 122%!  │
│     You're ₹1,800 over budget with 12   │
│     days remaining in February.          │
│                                          │
├──────────────────────────────────────────┤
│  [Type your question...]          [Send] │
├──────────────────────────────────────────┤
│  🏠 Home    📊 Analytics    💬 Chat      │
└──────────────────────────────────────────┘
```

#### Settings Screen
```
┌──────────────────────────────────────────┐
│  ← Settings                              │
├──────────────────────────────────────────┤
│                                          │
│  APPEARANCE                              │
│  🌙 Dark Mode                   [Toggle] │
│  🎨 Dynamic Colors (Material You)[Toggle]│
│                                          │
│  EXPENSE TRACKING                        │
│  💰 Default Currency          INR (₹)  > │
│  📊 Budget Settings                    > │
│  📱 SMS Scan Range        Last 3 Months> │
│  📂 Manage Categories                  > │
│  🤖 Smart Rules                        > │
│  📊 Exchange Rates                     > │
│                                          │
│  ACCOUNTS                                │
│  🏦 Manage Accounts                    > │
│                                          │
│  AI ASSISTANT                            │
│  🧠 AI Model          Downloaded (1 GB)> │
│  🔧 Developer Mode              [Toggle]│
│                                          │
│  SECURITY                                │
│  🔐 App Lock                    [Toggle] │
│                                          │
│  DATA                                    │
│  📤 Export Data                         > │
│  💾 Backup & Restore                    > │
│  📭 Unrecognized SMS (12)              > │
│  🗑️ Clear All Data                      │
│                                          │
│  ABOUT                                   │
│  ℹ️ Version              1.0.0 (Build 1) │
│  📄 Privacy Policy                     > │
│  ❓ FAQ                                 > │
│  ⭐ Rate on Play Store                  > │
│                                          │
│  Made with ❤️ in India                   │
└──────────────────────────────────────────┘
```

---

## 7. Technical Architecture

### 7.1 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 1.9+ |
| **UI** | Jetpack Compose (Material 3 / Material You) |
| **Architecture** | MVVM + Clean Architecture + UDF (Unidirectional Data Flow) |
| **DI** | Hilt (Dagger) |
| **Database** | Room (SQLite) with TypeConverters |
| **Preferences** | DataStore (Protocol Buffers) |
| **Async** | Kotlin Coroutines + Flow + StateFlow |
| **Navigation** | Compose Navigation (type-safe with Kotlin Serialization) |
| **AI/ML** | MediaPipe LLM (Qwen 2.5) — fully on-device |
| **Background** | WorkManager for periodic SMS scanning |
| **SMS** | BroadcastReceiver (real-time) + ContentResolver (history scan) |
| **Charts** | Vico or custom Compose Canvas charts |
| **Biometric** | AndroidX Biometric API |
| **Splash** | Android 12+ Splash Screen API |
| **Widgets** | Glance (Compose for app widgets) |
| **Min SDK** | API 31 (Android 12) |
| **Target SDK** | API 34 (Android 14) |

### 7.2 Module Structure

```
everypaisa/
├── app/                              # Main application module
│   └── src/main/java/com/everypaisa/tracker/
│       ├── EveryPaisaApp.kt          # @HiltAndroidApp Application class
│       ├── MainActivity.kt           # Single Activity (FragmentActivity)
│       ├── navigation/               # Route definitions + NavHost
│       ├── ui/                       # UI layer (screens, components, theme, viewmodels)
│       ├── presentation/            # Feature-specific screens + viewmodels
│       ├── domain/                   # Use cases, domain models, interfaces
│       ├── data/                     # Room DB, entities, DAOs, repositories, mappers
│       ├── di/                       # Hilt modules (DatabaseModule, RepositoryModule)
│       ├── worker/                   # WorkManager workers (SMS scan, widget update)
│       ├── receiver/                 # BroadcastReceiver for SMS
│       ├── widget/                   # Glance widgets
│       ├── utils/                    # Formatters, date utils
│       └── core/                     # Constants
│
├── parser-core/                      # Separate module: SMS parser engine
│   └── src/main/kotlin/com/everypaisa/parser/core/
│       ├── ParsedTransaction.kt
│       ├── TransactionType.kt
│       ├── MandateInfo.kt
│       ├── BankParserFactory.kt
│       └── bank/                     # 35+ bank-specific parsers
│
├── build.gradle.kts
├── settings.gradle.kts               # include :app, :parser-core
└── gradle.properties
```

### 7.3 Data Flow (UDF Pattern)

```
User opens app
    ↓
EveryPaisaNavHost → determines start (AppLock → Permission → Home)
    ↓
HomeScreen observes HomeViewModel.uiState (StateFlow)
    ↓
HomeViewModel combines flows: TransactionRepo + BudgetRepo + SubscriptionRepo
    ↓
Repositories query Room DAOs → return Flow<List<Entity>>
    ↓
Data flows up as StateFlow → Compose UI recomposes
    ↓
User taps FAB → triggers OptimizedSmsReaderWorker
    ↓
Worker: ContentResolver → BankParserFactory.parse() → SmsTransactionProcessor
    ↓
TransactionRepository.insertTransaction() → Room Flow auto-updates → UI refreshes
```

### 7.4 State Management Example

```kotlin
// HomeViewModel.kt
data class HomeUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val monthlyTotal: BigDecimal = BigDecimal.ZERO,
    val budgetSummary: BudgetOverallSummary? = null,
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetGroupRepository,
    private val subscriptionRepo: SubscriptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Combine multiple flows into single UI state
        combine(
            transactionRepo.getAllTransactions(),
            budgetRepo.getOverallSummary(),
            subscriptionRepo.getActiveSubscriptions()
        ) { transactions, budget, subscriptions ->
            HomeUiState(
                transactions = transactions,
                budgetSummary = budget,
                subscriptions = subscriptions,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }
}
```

---

## 8. Data Model & Database Schema

### 8.1 Core Entity: TransactionEntity

```kotlin
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["transaction_hash"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: BigDecimal,
    val merchantName: String,
    val category: String,
    val transactionType: TransactionType,
    val dateTime: LocalDateTime,
    val description: String? = null,
    val smsBody: String? = null,
    val smsSender: String? = null,
    val bankName: String? = null,
    val accountLast4: String? = null,
    val transactionHash: String,             // SHA-256 for deduplication
    val currency: String = "INR",
    val isDeleted: Boolean = false,          // Soft delete
    val fromAccount: String? = null,
    val toAccount: String? = null
)

enum class TransactionType {
    INCOME, EXPENSE, CREDIT, TRANSFER, INVESTMENT
}
```

### 8.2 All Database Entities

| Entity | Purpose | Key Fields |
|---|---|---|
| `TransactionEntity` | Core transaction data | amount, merchantName, category, transactionType, dateTime, transactionHash, currency |
| `SubscriptionEntity` | Recurring payments | merchantName, amount, nextPaymentDate, state (Active/Paused/Cancelled), umn, currency |
| `CategoryEntity` | Expense categories | name, color, isSystem, isIncome, displayOrder |
| `MerchantMappingEntity` | Merchant→Category overrides | merchantName (PK), category |
| `AccountBalanceEntity` | Bank account tracking | bankName, accountLast4, balance, accountType |
| `CardEntity` | Credit/Debit card info | last4, bankName, cardType, nickname |
| `ChatMessage` | AI chat history | role, content, timestamp |
| `UnrecognizedSmsEntity` | Unparsed bank SMS | smsBody, sender, timestamp |
| `RuleEntity` | Smart rules | name, conditions (JSON), actions (JSON), priority, isActive |
| `RuleApplicationEntity` | Rule audit log | ruleId, transactionId, appliedAt |
| `ExchangeRateEntity` | Currency rates | fromCurrency, toCurrency, rate, expiresAt |
| `BudgetEntity` | Budget definitions | name, amount, startDate, endDate, periodType |
| `BudgetCategoryEntity` | Budget↔Category map | budgetId (FK), categoryName, budgetAmount |
| `TransactionSplitEntity` | Split transactions | transactionId (FK), category, amount |
| `CategoryBudgetLimitEntity` | Monthly category caps | categoryName, limitAmount |

### 8.3 Default Categories (20 categories, seeded on install)

| Category | Color | Type |
|---|---|---|
| Food & Dining | #FC8019 | Expense |
| Groceries | #5AC85A | Expense |
| Shopping | #E91E63 | Expense |
| Transportation | #29B6F6 | Expense |
| Bills & Utilities | #FFA726 | Expense |
| Entertainment | #AB47BC | Expense |
| Healthcare | #EF5350 | Expense |
| Education | #42A5F5 | Expense |
| Personal Care | #EC407A | Expense |
| Travel | #26C6DA | Expense |
| Investments | #66BB6A | Expense |
| Subscriptions | #7E57C2 | Expense |
| Transfers | #78909C | Expense |
| Others | #BDBDBD | Expense |
| Salary | #4CAF50 | Income |
| Refunds | #8BC34A | Income |
| Cashback | #CDDC39 | Income |
| Interest | #009688 | Income |
| Dividends | #00BCD4 | Income |
| Income | #4CAF50 | Income |

---

## 9. SMS Parsing Engine

### 9.1 Parser Architecture

```
SMS (body + sender)
    ↓
BankParserFactory.parse(body, sender)
    ↓
Match sender to parser: "HDFCBK" → HDFCBankParser
    ↓
Parser.parse(body) → ParsedTransaction? or null
    ↓
null → UnrecognizedSmsEntity | parsed → SmsTransactionProcessor → Room
```

### 9.2 ParsedTransaction

```kotlin
data class ParsedTransaction(
    val amount: BigDecimal,
    val merchantName: String,
    val dateTime: LocalDateTime,
    val transactionType: TransactionType,  // INCOME, EXPENSE, CREDIT, TRANSFER, INVESTMENT
    val bankName: String,
    val accountLast4: String?,
    val currency: String = "INR",
    val description: String? = null,
    val mandateInfo: MandateInfo? = null   // For subscription detection
)

data class MandateInfo(
    val umn: String,          // Unique Mandate Number
    val merchant: String,
    val amount: BigDecimal
)
```

### 9.3 SMS Examples by Bank

| Bank | Sample SMS | Parsed Output |
|---|---|---|
| HDFC Debit | `Rs 500.00 debited from A/c XX1234 on 16-02-26 at MCD STORE (UPI Ref No 123)` | amount=500, merchant="MCD STORE", type=EXPENSE, bank="HDFC", acct="1234" |
| ICICI Card | `Rs.1,200 spent on ICICI Card XX5678 at AMAZON on 16-FEB-26. Avl bal: Rs.48,800` | amount=1200, merchant="AMAZON", type=CREDIT, bank="ICICI", acct="5678" |
| SBI Credit | `INR 2,500.00 credited to A/c XX9012 on 16.02.26. Available Balance: INR 45,678.50` | amount=2500, type=INCOME, bank="SBI", acct="9012" |
| PhonePe | `Rs 350 paid to SWIGGY via PhonePe UPI on 16-02-2026. UPI Ref: 123456` | amount=350, merchant="SWIGGY", type=EXPENSE, bank="PhonePe" |
| GPay | `You paid Rs. 250 to Uber India using Google Pay. UPI ID: xxx@xxx` | amount=250, merchant="Uber India", type=EXPENSE, bank="GPay" |
| E-Mandate | `HDFC: e-Mandate (Debit) of Rs 499 for Netflix. UMN: xxx` | amount=499, merchant="Netflix", mandate={umn:"xxx"} |

### 9.4 Testing Strategy

- 10+ SMS samples per bank parser (unit tests)
- Edge cases: amounts with/without commas, various date formats
- Negative cases: OTP, promotional, non-transaction SMS
- Performance: parse 1000 SMS in <5 seconds
- Target: >95% accuracy on top 10 banks

---

## 10. AI Assistant (On-Device)

### 10.1 Technical Details

| Property | Value |
|---|---|
| Model | Qwen 2.5 via MediaPipe LLM Inference |
| Size | ~1 GB (downloaded on-demand) |
| Processing | CPU/GPU on device |
| Context | ~4096 tokens |
| Privacy | Zero network calls during inference |

### 10.2 Context Injection

```kotlin
data class ChatContext(
    val currentDate: LocalDate,
    val monthSummary: MonthSummary,       // income, expense, count
    val recentTransactions: List<TransactionSummary>,  // last 50
    val activeSubscriptions: List<SubscriptionSummary>,
    val topCategories: List<CategorySpending>,  // top 5
    val quickStats: QuickStats  // avg daily, highest single, etc.
)
```

### 10.3 Example Interactions

| User Query | AI Response Pattern |
|---|---|
| "How much on food this month?" | Sum Food & Dining + breakdown by merchant |
| "Am I over budget?" | Compare spend vs limit + remaining days/allowance |
| "What are my subscriptions?" | List active subscriptions + monthly total |
| "Compare vs last month" | Side-by-side totals + biggest changes |
| "Top 3 expenses this week" | Rank recent transactions by amount |

---

## 11. Privacy & Security

### 11.1 Core Privacy Architecture

```
┌────────────────────────────────────┐
│          User's Phone              │
│  ┌──────────────────────────────┐  │
│  │        Everypaisa            │  │
│  │  SMS Reader → Parser → Room  │  │  100% local
│  │  AI Model → Inference        │  │  100% local
│  │  DataStore → Preferences     │  │  100% local
│  └──────────────────────────────┘  │
│  ❌ No internet for core features  │
│  ❌ No cloud servers               │
│  ❌ No analytics/tracking SDKs     │
│  ❌ No ad SDKs                     │
│  ❌ No data ever transmitted       │
└────────────────────────────────────┘
```

### 11.2 Play Store Data Safety Declaration

| Field | Value |
|---|---|
| Data shared with third parties | **No** |
| Data collected | **No data collected** |
| Security practices | Data encrypted at rest (device encryption) |
| Data deletion | Available via Settings → Clear All Data |

---

## 12. Performance Requirements

| Metric | Target |
|---|---|
| Cold start | < 3 seconds |
| Dashboard load | < 2 seconds |
| SMS parse (single) | < 500ms |
| SMS parse (1000 batch) | < 5 seconds |
| DB query | < 100ms |
| AI first token | < 3 seconds |
| APK size | < 15 MB |
| Memory usage | < 150 MB |
| Battery (background) | < 2%/day |
| Frame rate | 60 fps |
| Crash-free rate | > 99% |

---

## 13. Success Metrics

| Phase | Metric | Target |
|---|---|---|
| Launch (3 months) | Downloads | 10,000+ |
| Launch | Day-30 retention | >35% |
| Launch | Play Store rating | 4.3+ |
| Launch | SMS parse accuracy (top 10 banks) | >95% |
| Growth (12 months) | MAU | 25,000+ |
| Growth | Downloads (cumulative) | 100,000+ |
| Growth | Rating | 4.5+ |
| Growth | AI Chat adoption | >15% of users |
| Growth | Budget feature adoption | >25% of users |

---

## 14. Phased Delivery Plan

### Phase 1: MVP (Weeks 1-10)
Core expense tracking: SMS parsing, home dashboard, transaction list/detail, settings, dark mode, onboarding.

### Phase 2: Analytics & Budgets (Weeks 11-18)
Analytics charts, auto-categorization, subscription detection, budget management, smart rules, manual transaction add.

### Phase 3: AI & Advanced (Weeks 19-26)
AI chat, account management, multi-currency, data export, app lock (biometric), expand to 35+ banks.

### Phase 4: Polish & Launch (Weeks 27-30)
Widgets, FAQ, unrecognized SMS view, What's New dialog, Play Store listing, beta → production launch.

**Total estimated timeline: ~30 weeks (7.5 months)**

---

## 15. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Low SMS permission grant rate | High | Compelling education screen with privacy proof |
| Bank changes SMS format | High | Modular parsers, fallback GenericParser, fast update cycle |
| Play Store rejects SMS permission | Critical | Strict policy compliance, declaration form, privacy policy |
| AI model too large | Medium | Optional download, core works without AI |
| Room migration failures | High | Test all migrations, manual fallbacks |
| Competition from super-apps | Medium | Multi-bank aggregation + privacy as core USP |

---

## Appendix

### A. Play Store SMS Policy Compliance

1. ✅ SMS is core functionality (expense tracking from bank SMS)
2. ✅ No alternative API exists
3. ✅ Read-only (never sends SMS)
4. ✅ Data processed locally, never transmitted
5. ✅ Privacy policy clearly explains SMS usage
6. ✅ Permissions Declaration Form will be submitted

### B. Open Questions

1. Manual cash entry in MVP or Phase 2?
2. AI model bundled or on-demand download?
3. Web dashboard needed?
4. Tablet-optimized layout at launch?
5. Monetization: Free forever? Freemium? One-time?
6. Open-source parser-core for community bank support?

---

*This is a living document. Updated as decisions are made and development progresses.*
