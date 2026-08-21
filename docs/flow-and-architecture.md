# Silent Signal: Flow and Architecture

## 1. High-level app flow

1. App opens.
2. If no active session exists, the user lands on the auth screen.
3. User signs up with name, username, email, phone, password.
4. The app stores the account in the Room database and saves a local session.
5. After login, the main app shell opens with dashboard, contacts, history, and settings.
6. Alerts are always associated with the active account ID.
7. Each alert log stores location, risk score, message, and contact count.
8. The history screen filters logs by the active user account.

## 2. Login and account persistence

The app uses two layers for identity:

- AuthManager: lightweight local credential validation for sign-in/sign-up and basic password checks
- UserSessionStore: session persistence for the active user after successful login
- UserAccount Room entity: stores the user record for account-scoped history

This means a user can sign in with the same username/email on the same device and immediately get their saved local data.

## 3. Alert lifecycle

Once the app triggers an emergency event:

1. MLContextAnalyzer computes risk context and confidence.
2. EmergencyDispatcher fetches current coordinates and builds the Google Maps URL.
3. User safety preferences are checked for toggles such as SMS dispatch or location tracking.
4. SMS is sent to emergency contacts when enabled and permission is granted.
5. High-risk triggers may trigger call escalation.
6. The event is saved to an AlertLog with the current accountId.
7. The History screen shows only the logs for the active user.

## 4. Permission model

Android permission checks are still enforced by the operating system.

The app combines two layers:

- OS permission status: whether Android granted SMS or location permission
- App toggle status: whether the user chose to enable or disable SMS dispatch, GPS, etc.

This prevents accidental dispatches while giving the user a control center for choosing what features they want enabled.

## 5. Technology stack

- Kotlin
- Jetpack Compose
- Room SQLite
- SharedPreferences
- StateFlow / coroutines
- Android sensor APIs
- SMS, location, and dialing APIs
- Android permission model

## 6. Feature summary

### Auth screen
- Sign in / sign up flow
- Email or username login
- Remember-me behavior
- Account validation

### Contacts screen
- Add/edit/remove trusted contacts
- Primary and escalation tiers
- Contact notes for emergency messaging

### History screen
- Recent alert events
- Risk score and location summary
- Map open action
- Per-user filtering

### Settings screen
- Stealth mode toggle
- Dark privacy mode
- Sensor sensitivity
- Emergency message customizations
- Enable/disable individual dispatch features
- Permission status overview

## 7. Production upgrade path

For real multi-device sync and same-login ID history across devices, add a backend with these services:

- Firebase or custom REST API for auth
- secure password hashing
- user table with unique login ID
- alert history sync endpoint
- contact sync endpoint
- token-based auth and refresh

The current app is intentionally local-first, which is useful for prototyping and offline use.
