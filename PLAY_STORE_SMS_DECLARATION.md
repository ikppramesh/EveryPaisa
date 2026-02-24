Play Store SMS Permission Declaration — EveryPaisa

Overview

- App name: EveryPaisa
- Package: com.everypaisa.tracker (replace with exact package used in Play Console)
- Permissions requested: `android.permission.READ_SMS`, `android.permission.RECEIVE_SMS`

Justification (short)

EveryPaisa's core feature is automatic detection of bank transaction messages sent via SMS. Reading SMS is essential to extract transaction amount, merchant name, date/time and masked account/card indicators. All SMS parsing and storage occur on-device; no SMS content or derived transaction data is uploaded or shared.

Detailed explanation (for Play Console)

- Feature: Automatic SMS-based transaction detection and categorization. Users can also enter transactions manually or import via CSV.
- Why SMS access is required: Bank transaction messages are sent by banks/issuers as SMS with variable formats. To reliably extract transactions in multiple countries and languages we need access to incoming and stored SMS to parse these messages.
- Data use: SMS contents are parsed locally. We compute a local SHA-256 hash for deduplication. Transaction records are stored locally in a Room database and are only exported if the user explicitly exports a CSV file.
- Data sharing: None. There are no analytics, ads, or network uploads in the official build.

Permissions and components

- Manifest entries (reference):
  - `<uses-permission android:name="android.permission.READ_SMS" />`
  - `<uses-permission android:name="android.permission.RECEIVE_SMS" />`
  - `SmsBroadcastReceiver` registered for `android.provider.Telephony.SMS_RECEIVED` with `android:permission="android.permission.BROADCAST_SMS"`.

Screenshots / Evidence checklist

Include these screenshots (or screen-recordings) with the Play Console submission:
1. Screen showing the permission request rationale within the app (permission prompt UI) with the explanatory text visible.
2. Home/feature screen showing transactions created from SMS (demonstrate feature working).
3. Settings screen showing the option to delete all local data.
4. Export screen (if export exists) showing explicit user action to create a CSV.
5. Manifest snippet or app settings page showing that data processing is on-device (e.g., "100% on-device processing" banner).

Data Safety form guidance (recommended answers)

- Data collected: Phone numbers or identifiers (from SMS senders) and SMS content — note these are read locally and stored locally. Map to "User provided" and "Device-stored" as applicable.
- Data shared: No (select "No" for sharing with third parties)
- Encryption: App computes SHA-256 hashes locally. If you do not use other crypto libraries, indicate minimal crypto usage in the export compliance section.

Notes / Additional information

- Provide a publicly accessible Privacy Policy URL (link to the `PRIVACY_POLICY.html` hosted on your website) and include it in the Play Store listing.
- If you intend to add cloud sync, analytics, or crash-reporting later, update the Play Console Data Safety form and privacy policy accordingly.

Replace placeholder contact info and package name before submission.
