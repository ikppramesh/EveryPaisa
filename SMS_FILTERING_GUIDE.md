# SMS Filtering and Failed Transaction Handling

## Overview
Added comprehensive SMS filtering to skip non-transactional messages and automatically handle failed/reversed transactions.

## Features Added

### 1. Non-Transactional SMS Filtering
The app now automatically filters out these types of SMS:

#### OTP & Verification Codes
- Messages containing: "OTP", "one time password", "verification code", "authentication code", "security code", "do not share"
- Example: "Your OTP is 123456. Do not share with anyone."

#### Credit Limit Changes (NOT actual transactions)
- Messages about limit increases/decreases/changes
- Keywords: "limit increased", "limit decreased", "limit changed", "enhanced", "revised", "updated", "new limit", "credit limit is"
- Example: "Your credit limit has been increased to Rs. 20,00,000"

#### Balance Inquiries (NOT transactions)
- Balance check messages
- Keywords: "available balance", "current balance", "avl bal", "bal is", "outstanding balance", "minimum balance"
- Example: "Your available balance is Rs. 15,000"

#### Account Statements
- Statement generation notifications
- Keywords: "statement", "e-statement", "monthly statement", "account summary"
- Example: "Your monthly statement is ready for download"

#### Promotional Messages
- Marketing and promotional content
- Keywords: "download app", "click here", "offer valid", "promotional", "earn rewards", "cashback on", "special offer", "limited time", "subscribe", "t&c apply"
- Example: "Download our app and get 10% cashback on first transaction"

#### Reminders (NOT transactions)
- Payment due reminders
- Keywords: "reminder", "due date", "payment due", "bill due", "overdue", "please pay", "pay now"
- Example: "Reminder: Your credit card payment of Rs. 5,000 is due on 25th"

#### Welcome Messages
- Account opening/activation messages
- Keywords: "welcome to", "thank you for", "congratulations"
- Example: "Welcome to XYZ Bank! Your account is now active"

### 2. Failed Transaction Handling
Automatically detects and handles failed/reversed transactions:

#### Detection Keywords
- "failed", "declined", "unsuccessful", "could not be", "not successful"
- "transaction failed", "payment failed"
- "reversed", "reversal", "refunded", "credited back"

#### Automatic Processing
When a failed transaction SMS is detected:

1. **Parse Transaction Details**: Extract amount, merchant, bank, account from SMS
2. **Search for Original**: Look for matching expense transaction in last 30 days
   - Match by: Same amount, same bank, same account
3. **Delete Original**: Remove the failed expense transaction
4. **Create Refund**: Add new INCOME transaction with:
   - Merchant name: "[Original Merchant] - Refund"
   - Category: "Refunds"
   - Type: INCOME (green)
   - Description: "Failed transaction refund" or "Refund/Reversal"
5. **Log Actions**: Detailed logging for debugging

#### Example Flow
```
Original SMS: "Rs 1500 debited from A/c XX1234 on 15-Jan for ZOMATO"
→ Creates: EXPENSE transaction for Rs 1500

Failed SMS: "Transaction of Rs 1500 to ZOMATO failed. Amount will be credited back"
→ Deletes: Original Rs 1500 expense
→ Creates: INCOME transaction "ZOMATO - Refund" for Rs 1500 (green)
```

## Implementation Details

### Code Changes

#### 1. SmsTransactionProcessor.kt
- Added `isNonTransactionalSms()`: Checks message against filter keywords
- Added `isFailedTransaction()`: Detects failed/reversed transactions
- Added `handleFailedTransaction()`: Processes reversals
- Modified `processMessage()`: Applies filters before parsing

#### 2. TransactionDao.kt
- Added `getTransactionsByAmountRange()`: Query to find transactions by amount range and date

#### 3. TransactionRepository.kt & TransactionRepositoryImpl.kt
- Added `getTransactionsByAmountRange()` method

### Filter Priority
Filters are applied in this order:
1. **Non-transactional filter** (returns false immediately)
2. **Failed transaction handler** (processes reversal, returns false)
3. **Normal parser** (existing transaction parsing)

## Testing

### Test Cases to Verify

1. **OTP Messages**: Should NOT appear in transactions
   - Example: "Your OTP is 123456"

2. **Limit Change Messages**: Should NOT appear in transactions
   - Example: "Your credit limit has been increased to Rs 2,00,000"

3. **Balance Inquiries**: Should NOT appear in transactions
   - Example: "Your available balance is Rs 15,000"

4. **Failed Transactions**: Should remove original and add refund
   - Original: "Rs 500 debited for SWIGGY"
   - Failed: "Transaction failed. Amount will be refunded"
   - Result: Original removed, "SWIGGY - Refund" added as green/income

5. **Promotional Messages**: Should NOT appear in transactions
   - Example: "Download our app and get cashback"

### Monitoring
Check logcat for these tags:
- `⏭️ Skipping non-transactional SMS`: Non-transaction filtered out
- `🔄 Detected failed/reversed transaction`: Failed transaction detected
- `🗑️ Deleted original failed transaction`: Original expense removed
- `✅ Created refund transaction`: Refund transaction added

## Benefits

1. **Cleaner Transaction List**: Only actual transactions appear
2. **Accurate Balance**: No false expenses from notifications
3. **Automatic Reversals**: Failed transactions automatically corrected
4. **Better Analytics**: Charts show only real spending
5. **No Manual Cleanup**: Filters work automatically in background

## Privacy
- All filtering happens on device
- No data sent to internet
- SMS content never leaves the phone
- Filter keywords stored in app code only
