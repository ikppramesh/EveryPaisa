# Design Specifications
# Everypaisa — Visual Design System & UI/UX Guide

**Version:** 1.0  
**Date:** February 16, 2026  
**Design System:** Material 3 / Material You (Jetpack Compose)

---

## Table of Contents
1. [Design Philosophy](#1-design-philosophy)
2. [Color System](#2-color-system)
3. [Typography](#3-typography)
4. [Spacing & Grid](#4-spacing--grid)
5. [Iconography](#5-iconography)
6. [Component Library](#6-component-library)
7. [Screen-by-Screen Layouts](#7-screen-by-screen-layouts)
8. [Animations & Transitions](#8-animations--transitions)
9. [Dark Mode](#9-dark-mode)
10. [Accessibility](#10-accessibility)

---

## 1. Design Philosophy

### Guiding Principles

| Principle | Description |
|---|---|
| **Effortless** | The app does the work — user sees results, not processes |
| **Glanceable** | Key numbers (total spend, budget left) visible in <1 second |
| **Trustworthy** | Privacy-first visual language: no cloud icons, no sync animations |
| **Delightful** | Micro-animations, smooth transitions, Material You personality |
| **Inclusive** | WCAG AA contrast, screen reader support, large touch targets |

### Visual Identity

| Property | Value |
|---|---|
| **App Name** | Everypaisa |
| **Typeface** | System default (Roboto on most Android) |
| **Brand Personality** | Smart, clean, trustworthy, Indian |
| **Icon Style** | Rounded, filled Material icons |
| **Corner Radius** | 16dp (cards), 28dp (buttons), 50% (avatars/chips) |
| **Elevation** | Minimal — prefer surface color tinting over shadows |

---

## 2. Color System

### 2.1 Dynamic Theming (Material You)

Everypaisa fully supports **Dynamic Color** on Android 12+.
- The app extracts the user's wallpaper colors via `dynamicDarkColorScheme()` / `dynamicLightColorScheme()`
- On devices without Dynamic Color support, the app falls back to the **Everypaisa Brand Palette**

### 2.2 Everypaisa Brand Palette (Fallback)

#### Light Theme

| Role | Token | Hex | Usage |
|---|---|---|---|
| Primary | `md_theme_light_primary` | `#1B6B4A` | Headers, primary buttons, active icons |
| On Primary | `md_theme_light_onPrimary` | `#FFFFFF` | Text on primary |
| Primary Container | `md_theme_light_primaryContainer` | `#A4F3C7` | Cards, selected states |
| On Primary Container | `md_theme_light_onPrimaryContainer` | `#002112` | Text on primary containers |
| Secondary | `md_theme_light_secondary` | `#4E6355` | Secondary text, icons |
| Secondary Container | `md_theme_light_secondaryContainer` | `#D1E8D6` | Chips, tags |
| Tertiary | `md_theme_light_tertiary` | `#3B6471` | Accent elements |
| Background | `md_theme_light_background` | `#FBFDF8` | Screen background |
| Surface | `md_theme_light_surface` | `#FBFDF8` | Card background |
| Surface Variant | `md_theme_light_surfaceVariant` | `#DDE5DB` | Dividers, borders |
| Error | `md_theme_light_error` | `#BA1A1A` | Error states, expense amounts |
| Outline | `md_theme_light_outline` | `#727970` | Borders |

#### Dark Theme

| Role | Token | Hex | Usage |
|---|---|---|---|
| Primary | `md_theme_dark_primary` | `#89D6AC` | Headers, primary buttons |
| On Primary | `md_theme_dark_onPrimary` | `#003822` | Text on primary |
| Primary Container | `md_theme_dark_primaryContainer` | `#005234` | Cards, selected states |
| On Primary Container | `md_theme_dark_onPrimaryContainer` | `#A4F3C7` | Text on primary containers |
| Background | `md_theme_dark_background` | `#191C1A` | Screen background |
| Surface | `md_theme_dark_surface` | `#191C1A` | Card background |
| Error | `md_theme_dark_error` | `#FFB4AB` | Error states |

#### Semantic Colors

| Color | Light Hex | Dark Hex | Usage |
|---|---|---|---|
| **Income Green** | `#2E7D32` | `#81C784` | Income amounts, positive trends |
| **Expense Red** | `#C62828` | `#EF9A9A` | Expense amounts, negative trends, over-budget |
| **Credit Blue** | `#1565C0` | `#64B5F6` | Credit card charges |
| **Transfer Gray** | `#546E7A` | `#90A4AE` | Transfer amounts |
| **Investment Purple** | `#6A1B9A` | `#CE93D8` | Investment transactions |

### 2.3 Category Colors (Fixed — not affected by dynamic theming)

| Category | Color | Hex |
|---|---|---|
| Food & Dining | 🟠 Orange | `#FC8019` |
| Groceries | 🟢 Green | `#5AC85A` |
| Shopping | 🩷 Pink | `#E91E63` |
| Transportation | 🔵 Light Blue | `#29B6F6` |
| Bills & Utilities | 🟠 Amber | `#FFA726` |
| Entertainment | 🟣 Purple | `#AB47BC` |
| Healthcare | 🔴 Red | `#EF5350` |
| Education | 🔵 Blue | `#42A5F5` |
| Personal Care | 🩷 Rose | `#EC407A` |
| Travel | 🩵 Cyan | `#26C6DA` |
| Investments | 🟢 Green | `#66BB6A` |
| Subscriptions | 🟣 Deep Purple | `#7E57C2` |
| Transfers | 🩶 Blue Gray | `#78909C` |
| Salary | 🟢 Green | `#4CAF50` |
| Refunds | 🟢 Light Green | `#8BC34A` |
| Cashback | 🟡 Yellow-Green | `#CDDC39` |
| Others | 🩶 Gray | `#BDBDBD` |

---

## 3. Typography

### 3.1 Type Scale (Material 3)

| Style | Font | Size | Weight | Line Height | Usage |
|---|---|---|---|---|---|
| Display Large | Roboto | 57sp | 400 | 64sp | Hero amounts on splash |
| Display Medium | Roboto | 45sp | 400 | 52sp | — |
| Display Small | Roboto | 36sp | 400 | 44sp | — |
| Headline Large | Roboto | 32sp | 400 | 40sp | Screen titles |
| Headline Medium | Roboto | 28sp | 400 | 36sp | Section headers |
| Headline Small | Roboto | 24sp | 400 | 32sp | Card titles |
| Title Large | Roboto | 22sp | 500 | 28sp | Top app bar title |
| Title Medium | Roboto | 16sp | 500 | 24sp | List item titles |
| Title Small | Roboto | 14sp | 500 | 20sp | — |
| Body Large | Roboto | 16sp | 400 | 24sp | Primary body text |
| Body Medium | Roboto | 14sp | 400 | 20sp | Transaction descriptions |
| Body Small | Roboto | 12sp | 400 | 16sp | Timestamps, captions |
| Label Large | Roboto | 14sp | 500 | 20sp | Buttons, tabs |
| Label Medium | Roboto | 12sp | 500 | 16sp | Chips, badges |
| Label Small | Roboto | 11sp | 500 | 16sp | Overlines |

### 3.2 Amount Formatting

| Amount Type | Style | Color | Example |
|---|---|---|---|
| Expense | Title Medium, Medium weight | Expense Red | `- ₹1,250.00` |
| Income | Title Medium, Medium weight | Income Green | `+ ₹50,000.00` |
| Credit | Title Medium, Medium weight | Credit Blue | `- ₹3,500.00` |
| Transfer | Title Medium, Regular weight | Transfer Gray | `↔ ₹10,000.00` |
| Total (Hero) | Headline Large, Bold | On Surface | `₹28,560` |
| Budget Remaining | Body Large | Dynamic (green if ok, red if over) | `₹11,440 left` |

### 3.3 Number Formatting

| Rule | Example |
|---|---|
| Indian number system | ₹1,25,000.00 (lakhs) |
| No decimals for round numbers | ₹500 (not ₹500.00) |
| Two decimals for paise | ₹256.50 |
| Compact for large numbers (chart labels) | ₹1.2L, ₹25K |

---

## 4. Spacing & Grid

### 4.1 Spacing Scale

| Token | Value | Usage |
|---|---|---|
| `xxs` | 2dp | Inner icon padding |
| `xs` | 4dp | Between icon and text in a chip |
| `sm` | 8dp | Between list items, internal card padding |
| `md` | 12dp | Card content padding |
| `base` | 16dp | Screen horizontal padding, card padding |
| `lg` | 20dp | Between cards/sections |
| `xl` | 24dp | Major section spacing |
| `xxl` | 32dp | Between major page sections |
| `xxxl` | 48dp | Vertical breathing room |

### 4.2 Layout Grid

| Property | Value |
|---|---|
| Screen padding (horizontal) | 16dp |
| Card internal padding | 16dp |
| Card corner radius | 16dp |
| Card elevation | Level 1 (1dp) or tonal surface |
| Card gap (vertical) | 12dp |
| List item height | 72dp (two-line) or 56dp (single-line) |
| Bottom nav height | 80dp |
| Top app bar height | 64dp |
| FAB size | 56dp (standard) |
| FAB position | Bottom-end, 16dp from edges, above bottom nav |
| Touch target minimum | 48dp × 48dp |
| Chip height | 32dp |
| Chip gap | 8dp |

---

## 5. Iconography

### 5.1 Icon Style

| Property | Value |
|---|---|
| Icon set | Material Symbols (Rounded, Filled) |
| Size - Top bar actions | 24dp |
| Size - Bottom nav | 24dp |
| Size - List item leading | 40dp container, 24dp icon |
| Size - Category icon in chip | 18dp |
| Tint - Active | Primary color |
| Tint - Inactive | On Surface Variant |

### 5.2 Category Icons

| Category | Material Icon |
|---|---|
| Food & Dining | `restaurant` |
| Groceries | `local_grocery_store` |
| Shopping | `shopping_bag` |
| Transportation | `directions_car` |
| Bills & Utilities | `receipt_long` |
| Entertainment | `movie` |
| Healthcare | `local_hospital` |
| Education | `school` |
| Personal Care | `self_improvement` |
| Travel | `flight` |
| Investments | `trending_up` |
| Subscriptions | `subscriptions` |
| Transfers | `swap_horiz` |
| Salary | `payments` |
| Refunds | `undo` |
| Cashback | `redeem` |
| Interest | `percent` |
| Others | `more_horiz` |

### 5.3 Navigation Icons

| Tab | Active Icon | Inactive Icon |
|---|---|---|
| Home | `home` (filled) | `home` (outlined) |
| Analytics | `analytics` (filled) | `analytics` (outlined) |
| Chat | `chat` (filled) | `chat` (outlined) |

### 5.4 Action Icons

| Action | Icon | Location |
|---|---|---|
| Settings | `settings` | Top app bar (trailing) |
| Back | `arrow_back` | Top app bar (leading) |
| Scan SMS | `sync` | FAB on home |
| Add transaction | `add` | FAB on transactions |
| Delete | `delete` | Transaction detail, swipe action |
| Edit | `edit` | Transaction detail |
| Export | `file_download` | Settings |
| Search | `search` | Transaction list |
| Filter | `filter_list` | Transaction list |
| Send (chat) | `send` | Chat input |

---

## 6. Component Library

### 6.1 EveryPaisaScaffold
A wrapper around `Scaffold` providing consistent top bar, bottom nav, and FAB behavior across all screens.

```
EveryPaisaScaffold(
    title: String,
    showBottomNav: Boolean,
    fab: @Composable () -> Unit,
    actions: @Composable () -> Unit,   // top bar trailing icons
    content: @Composable () -> Unit
)
```

### 6.2 SummaryCard (Hero Card)
The prominent card at the top of the Home screen.

```
┌━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┐
│  February 2026                               │
│                                              │
│        ₹ 28,560                              │  Headline Large, Bold
│                                              │
│  ▲ 5% vs last month          45 transactions │  Body Small, green/red
└━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┘
```

**Specs:**
- Background: `PrimaryContainer`
- Text: `OnPrimaryContainer`
- Corner radius: 24dp
- Padding: 20dp
- Width: Match parent (with 16dp screen padding)
- Trend arrow: ▲ green (decrease) or ▼ red (increase) vs previous period

### 6.3 TransactionListItem

```
┌──────────────────────────────────────────────────┐
│  [🍔]  Swiggy                         - ₹320    │
│        Food & Dining   •   1:30 PM   •   GPay   │
└──────────────────────────────────────────────────┘
```

**Specs:**
- Leading: Category icon in a 40dp circle with category color (20% alpha background)
- Title: Merchant name — `Title Medium`
- Subtitle: Category name • Time • Payment method — `Body Small`, `OnSurfaceVariant`
- Trailing: Amount — `Title Medium`, color by transaction type
- Height: 72dp
- Click: Navigate to TransactionDetail
- Long-press: Show delete confirmation
- Swipe-to-delete: Left swipe with red background and trash icon

### 6.4 DateHeader

```
─────────── TODAY, FEB 16 ──────  ₹1,450 ───────
```

**Specs:**
- Text: `Label Medium`, `OnSurfaceVariant`
- Date: ALL CAPS, abbreviated month
- Daily total: Trailing, `Label Medium`, `Primary`
- Divider: Full width, 1dp, `SurfaceVariant`
- Top padding: 16dp
- Bottom padding: 8dp

### 6.5 CategoryChip (FilterChip)

```
  [🍔 Food & Dining]  [🛒 Groceries]  [🛍️ Shopping]
```

**Specs:**
- Type: `FilterChip` or `AssistChip`
- Shape: Rounded pill (50% corner radius)
- Height: 32dp
- Selected: Filled with category color, white text
- Unselected: Outlined, `OnSurfaceVariant` text
- Leading icon: Category icon (18dp)
- Gap: 8dp horizontal
- Container: `LazyRow` with 16dp start/end content padding
- "All" chip always first

### 6.6 BudgetProgressCard

```
┌────────────────────────────────────────────┐
│  💰 Monthly Budget                         │
│  ₹28,560 / ₹40,000                        │
│  ████████████░░░░░░░░░░ 71%               │
│  Daily allowance: ₹763                     │
└────────────────────────────────────────────┘
```

**Specs:**
- Background: `Surface` with `Level 1` tonal elevation
- Progress bar: `LinearProgressIndicator`
  - 0-70%: Primary (green)
  - 70-90%: Warning (amber)
  - 90-100%: Error (red)
  - >100%: Error with pulse animation
- Amount: `Title Medium`
- Percentage: `Label Large`
- Daily allowance: `Body Small`, `OnSurfaceVariant`
- Corner radius: 16dp

### 6.7 AccountSummaryCard

```
┌───────────────────────────────────────┐
│  🏦 HDFC Bank                 XX1234  │
│  Expenses: ₹12,450    Income: ₹50,000│
│  Balance: ₹45,678                     │
└───────────────────────────────────────┘
```

**Specs:**
- Background: `SurfaceVariant` or tinted surface
- Bank name: `Title Medium`, bold
- Account number: `Body Small`, masked (XX1234)
- Expense/Income row: `Body Medium` with semantic colors
- Balance: `Title Medium`
- Click: Navigate to AccountDetail
- Corner radius: 16dp

### 6.8 SubscriptionCard

```
┌────────────────────────────────────────────┐
│  🟣 Netflix              ₹499 / month    │
│  Next payment: Feb 22, 2026               │
│  [Active ✓]                               │
└────────────────────────────────────────────┘
```

### 6.9 AnalyticsChart Components

#### SpendingTrendChart
- Type: Line chart (or bar chart, toggleable)
- X-axis: Days of period (1-28/30/31)
- Y-axis: Amount (auto-scaled, compact format ₹5K)
- Touch: Show tooltip with exact date + amount
- Color: Primary gradient fill under line

#### CategoryBreakdownChart
- Type: Horizontal bar chart
- Bars: Colored by category color
- Labels: Category name + amount + percentage + transaction count
- Sorted: Descending by amount
- Click: Navigate to filtered transaction list

#### MerchantRankingList
- Type: Numbered list
- Each row: Rank # • Merchant name • Amount • % of total • Transaction count

### 6.10 ChatBubble

```
🤖 AI Message (Left-aligned):
┌──────────────────────────────────────┐
│  This month you spent ₹9,800 on     │
│  Food & Dining across 14 txns.      │
│  Your top food merchants:           │
│  • Swiggy: ₹2,800 (8 orders)       │
│  • Zomato: ₹1,950 (5 orders)       │
└──────────────────────────────────────┘

👤 User Message (Right-aligned):
               ┌────────────────────────┐
               │  Am I over budget?     │
               └────────────────────────┘
```

**Specs:**
- AI bubble: `SurfaceVariant` background, left-aligned, max 85% width
- User bubble: `PrimaryContainer` background, right-aligned, max 85% width
- Corner radius: 16dp (with sharp corner on sender side)
- Padding: 12dp
- Text: `Body Large`
- Streaming: Character-by-character with cursor animation
- Avatar: 24dp, AI = robot icon, User = person icon

### 6.11 SuggestionChip (Chat)

```
  [How much on food?]  [Top expenses?]  [Budget status?]
```

**Specs:**
- Type: `SuggestionChip`
- Shape: Rounded pill
- Background: `SecondaryContainer`
- Text: `Label Large`, `OnSecondaryContainer`
- Container: `FlowRow` with 8dp gap
- Location: Below AI greeting, disappear after first user message

### 6.12 SpotlightOverlay (Tutorial)

```
┌──────────────────────────────────────────┐
│                                          │
│       Semi-transparent black overlay     │
│                                          │
│                               ┌───┐     │
│                               │ 🔄│◄────│── Spotlight cutout
│                               └───┘     │
│                                          │
│             ┌─────────────────────┐      │
│             │  Tap here to scan   │      │
│             │  your bank SMS      │      │
│             │  [Got it!]          │      │
│             └─────────────────────┘      │
│                                          │
└──────────────────────────────────────────┘
```

**Specs:**
- Overlay: Black at 60% opacity
- Spotlight: Circular cutout around target element
- Tooltip: `Surface` card with `Body Large` text
- Button: `TextButton` "Got it!"
- Dismiss: Tap anywhere or button
- Show once: Tracked via DataStore flag

### 6.13 ExpandableSection

```
  ▼ ACCOUNTS (3)               ₹85,000
  ─────────────────────────────────────
    HDFC XX1234             ₹45,678
    ICICI XX5678            ₹28,322
    SBI XX9012              ₹11,000
```

**Specs:**
- Header: `Title Small`, `Primary`, clickable
- Chevron: Animated rotation (0° → 180°) on expand
- Content: `AnimatedVisibility` with `expandVertically`
- Trailing: Section total

### 6.14 Empty State

```
┌──────────────────────────────────────────┐
│                                          │
│              [illustration]              │
│                                          │
│         No transactions yet              │  Headline Small
│                                          │
│    Tap the scan button to read your      │  Body Medium
│    bank SMS and track your expenses      │
│                                          │
│         [ Scan Now ]                     │  FilledButton
│                                          │
└──────────────────────────────────────────┘
```

### 6.15 Snackbar (Undo Delete)

```
┌─────────────────────────────────────────────────┐
│  Transaction deleted                    [ UNDO ] │
└─────────────────────────────────────────────────┘
```

**Specs:**
- Duration: 5 seconds
- Action: "UNDO" — restores soft-deleted transaction
- Position: Above bottom nav
- Background: `InverseSurface`
- Text: `InverseOnSurface`

---

## 7. Screen-by-Screen Layouts

### 7.1 Permission Screen

**Purpose:** Explain and request SMS permission  
**Shown:** First launch only (until permission granted)

```
┌──────────────────────────────────────────┐
│                                          │
│              [Shield Icon]               │
│           128dp, Primary color           │
│                                          │
│     Your finances, your privacy          │  Headline Medium
│                                          │
│  Everypaisa reads your bank SMS to       │
│  automatically track your expenses.      │  Body Large
│  Everything stays on your phone.         │
│  We never upload your data anywhere.     │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ ✅ Reads only bank transaction SMS │  │
│  │ ✅ 100% on-device processing      │  │  Feature list
│  │ ✅ No internet needed             │  │  Body Medium
│  │ ✅ No data shared with anyone     │  │
│  │ ✅ Delete your data anytime       │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  │
│  ┃       Allow SMS Access            ┃  │  FilledButton
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  │
│                                          │
│      Skip for now (limited features)     │  TextButton
│                                          │
└──────────────────────────────────────────┘
```

### 7.2 Home Dashboard

*(See wireframe in PRD Section 6.4)*

**Structure (Compose):**
```
EveryPaisaScaffold {
    LazyColumn {
        item { SummaryCard() }
        item { PeriodTabRow() + HorizontalPager { PeriodContent() } }
        item { CategoryChipRow() }
        item { BudgetProgressCard() }   // only if budget set
        item { AccountSummarySection() }
        stickyHeader { DateHeader() }
        items(transactions) { TransactionListItem() }
        item { ViewAllButton() }
    }
    FAB { ScanButton() }
}
```

### 7.3 Transactions Screen

**Structure:**
```
EveryPaisaScaffold(title = "Transactions") {
    Column {
        SearchBar()           // real-time search
        FilterRow {
            PeriodChips()     // Today, Week, Month, etc.
            CategoryDropdown()
            TypeDropdown()    // Expense, Income, Credit, etc.
            CurrencyDropdown()
        }
        LazyColumn {
            // Grouped by date
            forEach(group) {
                stickyHeader { DateHeader(date, dailyTotal) }
                items(group.transactions) { TransactionListItem() }
            }
        }
    }
}
```

### 7.4 Transaction Detail Screen

```
┌──────────────────────────────────────────┐
│  ←  Transaction Detail          🗑️ ✏️   │
├──────────────────────────────────────────┤
│                                          │
│              - ₹1,250.00                 │  Display Small, Expense Red
│              Amazon                      │  Headline Small
│                                          │
│  ┌────────────────────────────────────┐  │
│  │  Category       [🛍️ Shopping    ▾] │  │  Editable dropdown
│  │  Date           Feb 16, 2026       │  │
│  │  Time           11:20 AM           │  │
│  │  Type           Credit Card        │  │
│  │  Bank           ICICI Bank         │  │
│  │  Account        XX5678             │  │
│  │  Currency       INR (₹)           │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Notes                                   │
│  ┌────────────────────────────────────┐  │
│  │  Birthday gift for mom             │  │  TextField
│  └────────────────────────────────────┘  │
│                                          │
│  Transaction Splits                      │
│  ┌────────────────────────────────────┐  │
│  │  Shopping          ₹750           │  │
│  │  + Add Split                       │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ▶ Original SMS                          │  Expandable
│  ┌────────────────────────────────────┐  │
│  │  Rs.1,250 spent on ICICI Card     │  │
│  │  XX5678 at AMAZON on 16-FEB-26.   │  │  Body Small, monospace
│  │  Avl bal: Rs.48,800               │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ☑️ Apply category to all Amazon txns   │  Checkbox
│                                          │
└──────────────────────────────────────────┘
```

### 7.5 Analytics Screen

*(See wireframe in PRD Section 6.4)*

**Structure:**
```
EveryPaisaScaffold(title = "Analytics") {
    LazyColumn {
        item { PeriodSelector() }       // Week | Month | Year | Custom
        item { TypeToggle() }           // Expense | Income | Credit
        item { CurrencyFilter() }
        item { SpendingTrendChart() }   // Line/Bar chart
        item { SectionHeader("Category Breakdown") }
        items(categories) { CategoryBreakdownRow() }
        item { SectionHeader("Top Merchants") }
        items(merchants) { MerchantRankingRow() }
        item { AskAIButton() }          // → Navigate to Chat
    }
}
```

### 7.6 AI Chat Screen

*(See wireframe in PRD Section 6.4)*

**Structure:**
```
EveryPaisaScaffold(title = "AI Assistant") {
    Column {
        LazyColumn(reverseLayout = true, weight = 1f) {
            items(messages) { ChatBubble(it) }
        }
        if (showSuggestions) {
            SuggestionChipRow()
        }
        ChatInputBar {
            TextField()
            SendButton()
        }
    }
}
// Model status bar at top if model not ready
```

### 7.7 Settings Screen

*(See wireframe in PRD Section 6.4)*

**Structure:**
```
EveryPaisaScaffold(title = "Settings", showBottomNav = false) {
    LazyColumn {
        item { SectionHeader("Appearance") }
        item { SwitchPreference("Dark Mode") }
        item { SwitchPreference("Dynamic Colors") }
        item { SectionHeader("Expense Tracking") }
        item { NavigationPreference("Default Currency") }
        item { NavigationPreference("Budget Settings") }
        item { NavigationPreference("SMS Scan Range") }
        item { NavigationPreference("Manage Categories") }
        item { NavigationPreference("Smart Rules") }
        item { NavigationPreference("Exchange Rates") }
        item { SectionHeader("Accounts") }
        item { NavigationPreference("Manage Accounts") }
        item { SectionHeader("AI Assistant") }
        item { NavigationPreference("AI Model") }
        item { SwitchPreference("Developer Mode") }
        item { SectionHeader("Security") }
        item { SwitchPreference("App Lock") }
        item { SectionHeader("Data") }
        item { NavigationPreference("Export Data") }
        item { NavigationPreference("Backup & Restore") }
        item { NavigationPreference("Unrecognized SMS", badge = count) }
        item { DangerButton("Clear All Data") }
        item { SectionHeader("About") }
        item { InfoPreference("Version", "1.0.0") }
        item { NavigationPreference("Privacy Policy") }
        item { NavigationPreference("FAQ") }
        item { NavigationPreference("Rate on Play Store") }
        item { Footer("Made with ❤️ in India") }
    }
}
```

### 7.8 Categories Screen

```
┌──────────────────────────────────────────┐
│  ← Categories                    [+ Add] │
├──────────────────────────────────────────┤
│                                          │
│  EXPENSE CATEGORIES                      │
│  ┌──────────────────────────────────┐   │
│  │ 🍔 Food & Dining          [⋮]   │   │  Drag handle
│  │ 🛒 Groceries              [⋮]   │   │  on left
│  │ 🛍️ Shopping               [⋮]   │   │
│  │ 🚗 Transportation         [⋮]   │   │
│  │ ...                              │   │
│  └──────────────────────────────────┘   │
│                                          │
│  INCOME CATEGORIES                       │
│  ┌──────────────────────────────────┐   │
│  │ 💰 Salary                 [⋮]   │   │
│  │ 💵 Refunds                [⋮]   │   │
│  │ ...                              │   │
│  └──────────────────────────────────┘   │
│                                          │
└──────────────────────────────────────────┘
```

### 7.9 Budget Screen

```
┌──────────────────────────────────────────┐
│  ← Budget                       [⚙️]    │
├──────────────────────────────────────────┤
│                                          │
│  OVERALL BUDGET                          │
│  ┌────────────────────────────────────┐  │
│  │  ₹28,560 / ₹40,000               │  │
│  │  ████████████░░░░░░░ 71%          │  │
│  │  Daily allowance: ₹763            │  │
│  │  14 days remaining                │  │
│  └────────────────────────────────────┘  │
│                                          │
│  CATEGORY BUDGETS                        │
│  ┌────────────────────────────────────┐  │
│  │ 🍔 Food & Dining                  │  │
│  │  ₹9,800 / ₹8,000    122% ⚠️      │  │
│  │  ████████████████████ OVER        │  │
│  ├────────────────────────────────────┤  │
│  │ 🛍️ Shopping                       │  │
│  │  ₹7,840 / ₹10,000    78%         │  │
│  │  ██████████████░░░░░░             │  │
│  ├────────────────────────────────────┤  │
│  │ 🚗 Transportation                 │  │
│  │  ₹5,600 / ₹8,000     70%         │  │
│  │  █████████████░░░░░░░             │  │
│  └────────────────────────────────────┘  │
│                                          │
└──────────────────────────────────────────┘
```

### 7.10 App Lock Screen

```
┌──────────────────────────────────────────┐
│                                          │
│                                          │
│              [Everypaisa Logo]           │
│                                          │
│           Unlock Everypaisa              │
│                                          │
│              [Fingerprint]               │
│          Tap to authenticate             │
│                                          │
│                                          │
│         [Use device PIN instead]         │
│                                          │
└──────────────────────────────────────────┘
```

---

## 8. Animations & Transitions

### 8.1 Navigation Transitions

| Transition | Animation | Duration |
|---|---|---|
| Screen push (forward) | `slideInHorizontally(start)` + `fadeIn` | 300ms |
| Screen pop (back) | `slideOutHorizontally(end)` + `fadeOut` | 300ms |
| Bottom nav switch | `fadeThrough` (Material motion) | 300ms |
| Dialog/Bottom sheet | `slideInVertically(bottom)` + `fadeIn` | 250ms |

### 8.2 Micro-Animations

| Element | Animation | Details |
|---|---|---|
| FAB scan | Rotate 360° | While scanning, continuous |
| Budget progress bar | `animateFloatAsState` | Smooth fill from 0 to value on appear |
| Transaction appear | `animateItemPlacement` | Smooth reorder in LazyColumn |
| Category chip select | Scale bounce `1.0 → 1.1 → 1.0` | 200ms, spring |
| Amount counter | `animateIntAsState` | Count up from 0 to value on load |
| Pull to refresh | Material 3 pull-to-refresh indicator | Standard |
| Swipe to delete | Swipe left reveals red background + trash icon | `SwipeToDismiss` |
| Expandable section | `AnimatedVisibility` with `expandVertically` | 300ms |
| Chat message appear | `fadeIn` + `slideInVertically(from bottom)` | 200ms |
| Streaming text | Character-by-character with blinking cursor | Variable |
| Spotlight pulse | Scale pulse `1.0 → 1.05 → 1.0` | Infinite, 1500ms |

### 8.3 Shared Element Transitions

| From | To | Shared Element |
|---|---|---|
| Transaction list item | Transaction detail | Amount text + Category icon |
| Category chip | Filtered transaction list | Category name + color |

---

## 9. Dark Mode

### 9.1 Theme Switching

| Mode | Implementation |
|---|---|
| **System** (default) | Follow `isSystemInDarkTheme()` |
| **Always Light** | Force light `colorScheme` |
| **Always Dark** | Force dark `colorScheme` |
| **AMOLED Dark** | Dark theme with `#000000` background |

### 9.2 Dark Mode Adjustments

| Element | Light | Dark |
|---|---|---|
| Background | `#FBFDF8` | `#191C1A` |
| Cards | White with subtle shadow | `#1E2320` with border |
| Expense amount | `#C62828` | `#EF9A9A` (lighter for contrast) |
| Income amount | `#2E7D32` | `#81C784` |
| Charts | Filled with primary | Outlined with primary |
| Dividers | `#E0E0E0` | `#2C2C2C` |
| Category colors | Full saturation | 80% saturation (softer) |
| Status bar | Transparent | Transparent |
| Navigation bar | Surface | Surface |

### 9.3 AMOLED Dark Mode

- Pure black `#000000` background for OLED power saving
- Cards: `#0A0A0A` with 1dp border `#1A1A1A`
- All other colors same as dark mode
- Toggle in Settings → Appearance

---

## 10. Accessibility

### 10.1 Requirements

| Requirement | Implementation |
|---|---|
| **Min contrast** | 4.5:1 for normal text, 3:1 for large text (WCAG AA) |
| **Touch targets** | Min 48dp × 48dp for all interactive elements |
| **Content descriptions** | All icons have `contentDescription` |
| **Semantic grouping** | Transaction items grouped with `semantics { }` |
| **Focus order** | Logical tab order matching visual layout |
| **Screen readers** | Full TalkBack support |
| **Font scaling** | Support up to 200% font scale without layout breaks |
| **Motion** | Respect `Settings.Global.ANIMATOR_DURATION_SCALE` |
| **Color independence** | Never use color alone to convey information (icons + text always) |
| **Headings** | All section headers marked with `heading()` semantics |

### 10.2 Screen Reader Announcements

| Screen | Announcement |
|---|---|
| Home | "Home screen. Total expenses this month: ₹28,560. 45 transactions." |
| Transaction item | "Swiggy, Food and Dining, minus 320 rupees, 1:30 PM, Google Pay" |
| Budget | "Monthly budget 71% used. ₹28,560 of ₹40,000. ₹11,440 remaining." |
| Chart | "Spending trend chart for February. Highest day: February 10, ₹5,200" |

---

## Appendix: Compose Theme Setup

```kotlin
// EveryPaisaTheme.kt
@Composable
fun EveryPaisaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoledDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme && amoledDark -> darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF0A0A0A)
        ).copy(/* merge with dark palette */)
        darkTheme -> darkColorScheme(/* Everypaisa dark palette */)
        else -> lightColorScheme(/* Everypaisa light palette */)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EveryPaisaTypography,
        content = content
    )
}
```

---

*This is a living document. Updated as the design evolves during development.*
