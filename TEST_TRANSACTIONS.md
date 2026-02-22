# EveryPaisa Test Transactions & Sample SMS

This document contains real SMS examples from different banks and regions for testing the multi-currency SMS parser.

## Table of Contents
1. [🇦🇪 UAE Banks (AED)](#uae-banks-aed)
2. [🇮🇳 Indian Banks (INR)](#indian-banks-inr)
3. [🌐 Multi-Currency Examples](#multi-currency-examples)
4. [💳 Digital Wallets & UPI](#digital-wallets--upi)
5. [❌ Failed Transactions & Refunds](#failed-transactions--refunds)

---

## 🇦🇪 UAE Banks (AED)

### E& Money (Etisalat)

**SMS 1 - Purchase at Online Retailer**
```
Dear ADITYA, a purchase of AED 31.89 was successfully completed at Amazon.ae using your e& money card ending with 1304. 
Date: 2026-02-18 11:54:26
You earned AED 0.19 cash rewards with this transaction.
Available balance: AED 30.35
Transaction ID: 292762393
```
- **Amount:** AED 31.89
- **Merchant:** Amazon.ae
- **Card Ending:** 1304
- **Type:** DEBIT
- **Category:** Shopping
- **Rewards:** AED 0.19 cashback

---

### Mashreq Bank (NEO VISA Debit Card)

**SMS 1 - Purchase at Retail Store**
```
Thank you for using NEO VISA Debit Card Card ending 2420 for AED 38.15 at Noon Minutes on 11-FEB-2026 10:01 AM. Available Balance is AED 11,577.35
```
- **Amount:** AED 38.15
- **Merchant:** Noon Minutes
- **Card Ending:** 2420
- **Type:** DEBIT
- **Date:** 11-FEB-2026 10:01 AM
- **Balance:** AED 11,577.35
- **Category:** Shopping / Retail

**SMS 2 - ATM Withdrawal**
```
Your Mashreq Credit Card XX5432 has been debited AED 500.00 for CASH WITHDRAWAL at ATM - DEIRA BRANCH on 19-FEB-2026 08:45 AM. Available limit: AED 25,000
```
- **Amount:** AED 500.00
- **Merchant:** CASH WITHDRAWAL - DEIRA BRANCH
- **Card Ending:** 5432
- **Type:** DEBIT
- **Category:** ATM / Cash Withdrawal

---

### Emirates NBD (ENBD)

**SMS 1 - Purchase at Restaurant**
```
You have been debited for AED 150.00 at Al Reef Bakery on 19-FEB-2026 at 02:34 PM. A/C XX1234. Ref: AL-REEF-02PM. Avl Bal: AED 5,432.10
```
- **Amount:** AED 150.00
- **Merchant:** Al Reef Bakery
- **Account:** 1234
- **Type:** DEBIT
- **Date:** 19-FEB-2026 02:34 PM
- **Balance:** AED 5,432.10
- **Category:** Food & Dining

**SMS 2 - Transfer Received**
```
AED 2,000.00 has been credited to your Emirates NBD Account XX5678 from AHMED AL MANSOURI on 20-FEB-2026 11:15 AM via NEFT. Reference: SALARY-FEB-2026. Available Balance: AED 18,950.75
```
- **Amount:** AED 2,000.00
- **Type:** CREDIT
- **From:** AHMED AL MANSOURI
- **Method:** NEFT
- **Date:** 20-FEB-2026 11:15 AM
- **Category:** Income / Salary

---

### FAB (First Abu Dhabi Bank)

**SMS 1 - Bill Payment**
```
Your FAB Debit Card ending 1111 has paid AED 350.50 to ADWEA - Electricity Bill on 18-FEB-2026 03:22 PM. Available Balance: AED 12,450.25
```
- **Amount:** AED 350.50
- **Merchant:** ADWEA - Electricity Bill
- **Card Ending:** 1111
- **Type:** DEBIT
- **Category:** Utilities / Bills

---

### ADIB (Abu Dhabi Islamic Bank)

**SMS 1 - Online Shopping**
```
Dear Customer, your ADIB Credit Card (XXXX-XXXX-XXXX-6789) has been charged AED 275.00 for purchase at Noon e-Commerce on 21-FEB-2026 at 04:15 PM. Credit Limit Remaining: AED 24,725.00
```
- **Amount:** AED 275.00
- **Merchant:** Noon e-Commerce
- **Card Ending:** 6789
- **Type:** DEBIT
- **Category:** Shopping

---

## 🇮🇳 Indian Banks (INR)

### HDFC Bank

**SMS 1 - Debit Card Purchase**
```
Dear RAMESH, Rs. 2,250.00 has been debited from your A/C XX5432 on 21-FEB-2026 at 15:45 for purchase at SWIGGY using HDFC Debit Card ending 3456. Available Balance: Rs. 45,230.50
```
- **Amount:** INR 2,250
- **Merchant:** SWIGGY
- **Card Ending:** 3456
- **Account:** 5432
- **Type:** DEBIT
- **Category:** Food & Dining

**SMS 2 - EMI Payment**
```
Rs. 15,000 debited from your Credit Card (HDFC) ending 2020 for monthly EMI payment on 20-FEB-2026. Available Limit: Rs. 1,25,000. Txn Ref: HDC-EMI-2020
```
- **Amount:** INR 15,000
- **Type:** DEBIT
- **Category:** Loan / EMI

---

### ICICI Bank

**SMS 1 - Online Payment**
```
Rs.1,500 spent using your Credit Card ending 2020 for Flight Ticket at MakeMyTrip on 20-Feb-2026 08:15 PM. Total Spends this cycle: Rs. 8,750. Available Limit: Rs. 1,25,000
```
- **Amount:** INR 1,500
- **Merchant:** MakeMyTrip
- **Card Ending:** 2020
- **Type:** DEBIT
- **Category:** Travel

**SMS 2 - Salary Credit**
```
Rs. 75,000.00 credited to your ICICI Bank Savings Account XX9876 on 01-FEB-2026 from YOUR EMPLOYER via NEFT. Reference: SALARY-FEB2026. Available Balance: Rs. 2,34,567.89
```
- **Amount:** INR 75,000
- **Type:** CREDIT
- **From:** YOUR EMPLOYER
- **Account:** 9876
- **Category:** Income

---

### SBI (State Bank of India)

**SMS 1 - ATM Withdrawal**
```
Rs. 10,000 withdrawn from ATM (MCC 7011) on 19-FEB-2026 at 02:30 PM using SBI Debit Card ending 1234. Available Balance: Rs. 35,678.50. Ref: SBI-ATM-2026
```
- **Amount:** INR 10,000
- **Type:** DEBIT
- **Card:** 1234
- **Category:** ATM Withdrawal

---

### Axis Bank

**SMS 1 - Shopping Purchase**
```
Rs. 8,999 debited from your Axis Bank Debit Card (XXXX-XXXX-XXXX-5678) on 21-FEB-2026 at 16:20 for purchase at FLIPKART. Available Balance: Rs. 42,100.00
```
- **Amount:** INR 8,999
- **Merchant:** FLIPKART
- **Card Ending:** 5678
- **Type:** DEBIT
- **Category:** Shopping

---

### Kotak Mahindra Bank

**SMS 1 - Online Shopping**
```
Rs. 3,299.00 has been charged to your Kotak Credit Card ending 9999 for purchase at AMAZON.IN on 20-FEB-2026 at 11:45 AM. Available Credit Limit: Rs. 2,50,000
```
- **Amount:** INR 3,299
- **Merchant:** AMAZON.IN
- **Card Ending:** 9999
- **Type:** DEBIT
- **Category:** Shopping

---

## 🌐 Multi-Currency Examples

### USD Transactions

**Bank Transfer**
```
You have withdrawn USD 100.00 from ATM (MCC 7011) at NYC BRANCH on 21-FEB-2026. Available Balance: USD 450.25. Ref: ATM-100-USD
```
- **Amount:** USD 100
- **Type:** DEBIT
- **Category:** ATM Withdrawal

**Payment**
```
USD 45.99 has been debited from your account for subscription to NETFLIX on 15-FEB-2026. Available Balance: USD 1,234.56
```
- **Amount:** USD 45.99
- **Merchant:** NETFLIX
- **Type:** DEBIT
- **Category:** Entertainment

---

### EUR Transactions

**Credit Card Purchase**
```
Your Visa Card ending 7890 has been charged EUR 65.50 for purchase at BOOKING.COM on 18-FEB-2026 at 09:30 AM. Available Balance: EUR 2,345.75
```
- **Amount:** EUR 65.50
- **Merchant:** BOOKING.COM
- **Type:** DEBIT
- **Category:** Travel

---

### GBP Transactions

**Debit Card Purchase**
```
Your Debit Card ending 5678 has been charged £32.50 at TESCO SUPERMARKET on 21-FEB-2026 03:15 PM. Balance: £2,145.80
```
- **Amount:** GBP 32.50
- **Merchant:** TESCO SUPERMARKET
- **Type:** DEBIT
- **Category:** Groceries

---

### SAR Transactions (Saudi Arabia)

**Purchase**
```
Your Saudi National Bank Card (ending 3344) has been debited 149.99 SR for purchase at DANAH on 20-FEB-2026 at 05:45 PM. Available Balance: 5,678.50 SR
```
- **Amount:** SAR 149.99
- **Merchant:** DANAH
- **Type:** DEBIT
- **Category:** Groceries

---

### JPY Transactions (Japan)

**ATM Withdrawal**
```
¥10,000 has been withdrawn from ATM on 19-FEB-2026 at TOKYO SHIBUYA BRANCH. Available Balance: ¥245,678
```
- **Amount:** JPY 10,000
- **Type:** DEBIT
- **Category:** ATM Withdrawal

---

## 💳 Digital Wallets & UPI

### Google Pay (UPI)

**Payment Sent**
```
You sent Rs. 500 to JOHN SHARMA via Google Pay UPI on 21-FEB-2026 at 14:30. Available Balance: Rs. 15,234.50. Ref: UPI-JOHN-500
```
- **Amount:** INR 500
- **Type:** DEBIT
- **Method:** UPI
- **Category:** Transfer / Peer-to-Peer

**Payment Received**
```
Rs. 2,000 received from PRIYA PATEL via Google Pay UPI on 20-FEB-2026 at 11:15. Your Balance: Rs. 15,734.50. Ref: UPI-PRIYA-2000
```
- **Amount:** INR 2,000
- **Type:** CREDIT
- **Method:** UPI
- **Category:** Income

---

### PhonePe

**Bill Payment**
```
Electricity bill of Rs. 1,850 paid to BESCOM via PhonePe on 19-FEB-2026 at 16:45. Available Balance: Rs. 3,456.78. Txn ID: PPE-BESCOM-1850
```
- **Amount:** INR 1,850
- **Merchant:** BESCOM
- **Type:** DEBIT
- **Category:** Bills / Utilities

---

### Amazon Pay

**Refund Received**
```
Refund of Rs. 2,999 received for Order #123456789 from Amazon on 21-FEB-2026. Available Balance: Rs. 18,999.00
```
- **Amount:** INR 2,999
- **Type:** CREDIT (Refund)
- **Merchant:** Amazon
- **Category:** Refund

---

### PayTm

**Wallet Recharge**
```
Your PayTm Wallet has been credited with Rs. 5,000 on 20-FEB-2026 at 10:30 using HDFC Debit Card. Available Balance: Rs. 8,456.50
```
- **Amount:** INR 5,000
- **Type:** CREDIT
- **Method:** Digital Wallet
- **Category:** Wallet Recharge

---

## ❌ Failed Transactions & Refunds

### Failed Purchase

**Original Transaction**
```
Rs. 10,000 debited from your account for purchase at FLIPKART on 19-FEB-2026 using Debit Card ending 1234. Available Balance: Rs. 25,000
```

**Refund SMS**
```
Rs. 10,000 has been refunded to your A/C for failed transaction at FLIPKART on 19-FEB-2026. Transaction reference: FLP-FAILED-10K. Available Balance: Rs. 35,000
```
- **Type:** CREDIT (Refund)
- **Category:** Refund
- **Original Merchant:** FLIPKART

---

### Declined Card

**Decline Notification**
```
Your card transaction of Rs. 5,000 at AMAZON.IN on 21-FEB-2026 has been declined. Reason: CVV mismatch. Please contact customer care.
```
- **Type:** Failed
- **Category:** Failed Transaction

---

### Subscription Cancellation Refund

**Original**
```
Annual Netflix subscription of USD 119.99 has been charged on 15-FEB-2026. Available Balance: USD 1,000
```

**Refund**
```
USD 119.99 refund processed for Netflix subscription cancellation on 21-FEB-2026. Available Balance: USD 1,119.99. Ref: REF-NETFLIX-119
```
- **Type:** CREDIT (Refund)
- **Category:** Refund / Subscription

---

## 🧪 Testing Instructions

### How to Import Test SMS

1. **In EveryPaisa App:**
   - Open app → Home screen
   - Tap "Scan SMS" button
   - Grant SMS read permission
   - App will read existing SMS and parse them

2. **Using Android Emulator (for testing):**
   ```bash
   adb shell service call isms 7 s1 com.everypaisa.tracker s2 "Rs.1000 debited from your account at Swiggy"
   ```

3. **Real Device Testing:**
   - Send these SMS messages to your phone
   - Or forward them as SMS to your account
   - Open EveryPaisa app
   - Tap "Scan SMS" to import

### Expected Parsing Results

Each SMS should parse as:
- ✅ **Amount** extracted correctly
- ✅ **Currency** detected (AED, INR, USD, etc.)
- ✅ **Merchant** name extracted
- ✅ **Card/Account** last 4 digits captured
- ✅ **Date** parsed (when available)
- ✅ **Type** determined (DEBIT/CREDIT)
- ✅ **Category** auto-assigned
- ✅ **Transaction hash** generated for deduplication

---

## 📊 Parser Coverage

| Bank | Parser | Currencies | Status |
|------|--------|-----------|--------|
| HDFC Bank | HDFCBankParser | INR | ✅ Active |
| ICICI Bank | ICICIBankParser | INR | ✅ Active |
| SBI | SBIParser | INR | ✅ Active |
| Axis Bank | AxisBankParser | INR | ✅ Active |
| Kotak | KotakBankParser | INR | ✅ Active |
| IDFC First | IDFCFirstBankParser | INR | ✅ Active |
| Federal | FederalBankParser | INR | ✅ Active |
| PNB | PNBParser | INR | ✅ Active |
| BOB | BOBParser | INR | ✅ Active |
| Canara | CanaraParser | INR | ✅ Active |
| Union Bank | UnionBankParser | INR | ✅ Active |
| Emirates NBD | EmiratesNBDParser | AED | ✅ Active |
| Mashreq | MashreqParser | AED | ✅ Active |
| Citi | CitiBankParser | USD, EUR, GBP | ✅ Active |
| HSBC | HSBCParser | USD, EUR, GBP | ✅ Active |
| Standard Chartered | StandardCharteredParser | USD, EUR, GBP | ✅ Active |
| Google Pay | GooglePayParser | INR, USD | ✅ Active |
| PhonePe | PhonePeParser | INR | ✅ Active |
| PayTm | PaytmParser | INR | ✅ Active |
| Amazon Pay | AmazonPayParser | INR, USD | ✅ Active |
| Generic Parser | GenericBankParser | 30+ currencies | ✅ Fallback |

---

**Last Updated:** February 22, 2026  
**Version:** 2.0
