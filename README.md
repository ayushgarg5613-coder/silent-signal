# Silent Signal

## 1. Overview

Silent Signal is an Android emergency safety application built in Kotlin with Jetpack Compose. Its primary purpose is to protect users during a crisis by:

- detecting trigger events such as shake, secret tap, voice keyword, or manual SOS
- reading the user’s current location with Google Play Services FusedLocationProviderClient
- sending an SOS message to trusted contacts and the emergency police number 100
- logging all alert activity in a user-scoped history for the signed-in account
- letting the user control which emergency features are enabled or disabled

This app is designed for a local-first prototype and can later be upgraded to cloud sync with Firebase or a custom backend.

## 2. What the app does

### Core features
- User login and registration
- Same-account alert history
- Emergency contact management
- Trigger-based alert activity
- SMS emergency dispatch to trusted contacts and police number 100
- Live location link in the SOS message
- Permission toggles for SMS, GPS, call escalation, and audio trigger
- Stealth/privacy mode

### Important behavior
When a user triggers an alert, the app does the following:
1. Reads the current location using FusedLocationProviderClient.getCurrentLocation()
2. Checks if the location is recent and has acceptable accuracy
3. Builds a Google Maps URL like:
   https://maps.google.com/?q=28.6139,77.2090
4. Sends an SMS to all saved trusted contact numbers and the emergency number 100
5. Logs the event in the local Room database
6. If risk is high, it can trigger call escalation as well

## 3. File-by-file usage and implementation

### [app/src/main/java/com/example/MainActivity.kt](app/src/main/java/com/example/MainActivity.kt)
- Hosts the Compose app shell
- Checks authentication state
- Displays auth screen if user is not logged in
- Routes to dashboard, contacts, history, and settings after login

### [app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt](app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt)
- Main state holder for the app
- Manages authentication state, settings, current session, and alert triggers
- Calls emergency dispatch logic when a trigger fires
- Keeps account-specific alert history in view state

### [app/src/main/java/com/example/UserSessionStore.kt](app/src/main/java/com/example/UserSessionStore.kt)
- Stores the current active session locally
- Keeps track of account ID, login ID, username, and name
- Supports session management after sign-in and sign-out

### [app/src/main/java/com/example/voice/AuthManager.kt](app/src/main/java/com/example/voice/AuthManager.kt)
- Validates local login and signup flow
- Stores user account identity for the active device
- Provides login and remember-me behavior

### [app/src/main/java/com/example/data/db/AppDatabase.kt](app/src/main/java/com/example/data/db/AppDatabase.kt)
- Creates the Room database instance
- Registers account and alert tables

### [app/src/main/java/com/example/data/db/Entities.kt](app/src/main/java/com/example/data/db/Entities.kt)
- Defines the data model for:
  - UserAccount
  - EmergencyContact
  - AlertLog
- Stores each alert with the linked accountId

### [app/src/main/java/com/example/data/db/Daos.kt](app/src/main/java/com/example/data/db/Daos.kt)
- Provides Room queries for contacts and alert logs
- Lets the app get alerts by active account

### [app/src/main/java/com/example/data/preferences/UserPreferencesManager.kt](app/src/main/java/com/example/data/preferences/UserPreferencesManager.kt)
- Saves app-level user safety settings
- Stores toggles like SMS dispatch, location sharing, call escalation, and audio trigger

### [app/src/main/java/com/example/emergency/EmergencyDispatcher.kt](app/src/main/java/com/example/emergency/EmergencyDispatcher.kt)
- This is the core emergency sending module
- It fetches live GPS using FusedLocationProviderClient
- It validates freshness and accuracy before using the location
- It builds the SOS SMS message with live location
- It sends to all trusted contacts and also to 100
- It logs the alert and triggers haptic/vibration feedback

### [app/src/main/java/com/example/ui/screens/AuthScreen.kt](app/src/main/java/com/example/ui/screens/AuthScreen.kt)
- Login and sign-up UI
- Email or username login form
- Remember-me option
- Forgot password UI

### [app/src/main/java/com/example/ui/screens/HistoryScreen.kt](app/src/main/java/com/example/ui/screens/HistoryScreen.kt)
- Displays all alert events for the signed-in account
- Shows risk score and live location information
- Lets user view map or clear log history

### [app/src/main/java/com/example/ui/screens/ContactsScreen.kt](app/src/main/java/com/example/ui/screens/ContactsScreen.kt)
- Adds emergency contacts
- Supports contact notes, primary designation, and escalation tiers

### [app/src/main/java/com/example/ui/screens/StealthSettingsScreen.kt](app/src/main/java/com/example/ui/screens/StealthSettingsScreen.kt)
- Lets the user toggle the emergency features
- Shows permission status and enables/disables app-level control for each critical action

### [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)
- Declares required Android permissions:
  - SEND_SMS
  - ACCESS_FINE_LOCATION
  - ACCESS_COARSE_LOCATION
  - CALL_PHONE
  - RECORD_AUDIO
  - INTERNET

## 4. Technology stack

### Android / Kotlin
- Kotlin for app logic and Compose UI
- Android SDK for permission handling and runtime APIs

### UI
- Jetpack Compose for fast, modern UI development
- Material 3 components for cards, buttons, dialogs, and layouts

### Local storage
- Room database for persistent records
- SharedPreferences for sessions and settings

### Location API
- Google Play Services Location
- FusedLocationProviderClient.getCurrentLocation()
- Accuracy and timestamp validation before sending message

### SMS API
- Android SmsManager
- sendTextMessage / sendMultipartTextMessage

### Runtime permission handling
- ActivityCompat.checkSelfPermission
- ActivityResultContracts.RequestMultiplePermissions

### Async and state handling
- Kotlin coroutines
- StateFlow

## 5. Reality of the live-location logic

The app uses this pattern:

```kotlin
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

fusedLocationClient.getCurrentLocation(
    Priority.PRIORITY_HIGH_ACCURACY,
    cancellationToken.token
)
```

The location is considered valid only when:
- it is not null
- timestamp is within the last 60 seconds
- accuracy is acceptable for emergency use

This prevents using stale or inaccurate GPS data when sending an SOS.

## 6. SMS sending logic to trusted contacts and police

This is the emergency SMS flow implemented in the app:

```kotlin
val smsManager: SmsManager = SmsManager.getDefault()
for (number in recipientNumbers) {
    val parts = smsManager.divideMessage(fullSmsBody)
    smsManager.sendMultipartTextMessage(number, null, parts, null, null)
}
```

The `recipientNumbers` list is built as follows:
- all saved trusted contacts from the contact database
- police number 100 appended to the list

This means the app can send emergency SMS to both the trusted contact and the police number when the user triggers an alert.

## 7. Emergency SMS format

Sample SOS message format:

```text
EMERGENCY ALERT: I need help! My live location:
Live Location: https://maps.google.com/?q=28.6139,77.2090
[Risk Level: HIGH (91%)]
```

This gives a trusted contact a direct link to the current location and an urgency signal.

## 8. Runtime permissions required

The app declares the following permissions in the manifest:

```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

These are requested at runtime when needed, especially for SMS and location access.

> Note: On modern Android, SMS permission restrictions and Play policy may require extra review for production deployment. For a real-world emergency app, the release version should be evaluated against Play policy and OS restrictions.

## 9. How the trigger and alert flow works

1. User signs in.
2. User configures emergency contacts and app settings.
3. Trigger is activated (shake, tap, voice, or manual).
4. MainViewModel triggers EmergencyDispatcher.dispatchAlert().
5. Dispatcher computes risk via MLContextAnalyzer.
6. Location is fetched through FusedLocationProviderClient.
7. SMS is built and sent to trusted contacts and police number 100.
8. Alert is saved to Room with latitude, longitude, mapsUrl, risk score, and status.
9. The history screen shows the alert log for the active account.

## 10. Security and production considerations

For production release, consider:
- Firebase Auth or a secure backend for login
- encrypted cloud storage for emergency data
- user consent screens for permissions
- auditing / rate limits for SOS triggering
- emergency SMS fallback when SMS permission is denied
- careful review of Google Play restrictions for SMS-based SOS apps

## 11. Run instructions

1. Open the project in Android Studio
2. Make sure Android Studio JDK is configured
3. Sync Gradle
4. Run on emulator or physical device
5. Sign in and assign trusted contacts
6. Trigger a manual or automatic emergency event
7. Confirm that the SMS includes a live location and is sent to the trusted contact and the police number 100

## 12. Build status

The application compiles successfully with Android Studio’s JDK configured.

## 13. Summary

This app follows a realistic emergency-application pattern:
- local user account creation and login
- user-specific alert history
- trusted contact-based emergency SMS
- live GPS-based location link
- police number 100 included in emergency dispatch
- permission and app-level control for enabling/disabling features

That is the complete workflow of the SOS implementation.
