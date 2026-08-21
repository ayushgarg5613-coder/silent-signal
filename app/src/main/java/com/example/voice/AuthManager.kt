package com.example.voice

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val isAuthenticated: Boolean = false,
    val displayName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val rememberMe: Boolean = false,
    val hasRegisteredAccount: Boolean = false
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("silent_signal_auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun loadAuthState(): AuthState {
        val registeredName = prefs.getString(KEY_NAME, "") ?: ""
        val registeredUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        val registeredPhone = prefs.getString(KEY_PHONE, "") ?: ""
        val registeredEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        return AuthState(
            isAuthenticated = prefs.getBoolean(KEY_LOGGED_IN, false) && rememberMe,
            displayName = prefs.getString(KEY_ACTIVE_NAME, registeredName) ?: registeredName,
            username = prefs.getString(KEY_ACTIVE_USERNAME, registeredUsername) ?: registeredUsername,
            phoneNumber = prefs.getString(KEY_ACTIVE_PHONE, registeredPhone) ?: registeredPhone,
            email = prefs.getString(KEY_ACTIVE_EMAIL, registeredEmail) ?: registeredEmail,
            rememberMe = rememberMe,
            hasRegisteredAccount = registeredPhone.isNotBlank() || registeredEmail.isNotBlank() || registeredUsername.isNotBlank()
        )
    }

    fun signUp(fullName: String, username: String, email: String, phoneNumber: String, password: String, rememberMe: Boolean): Result<Unit> {
        val trimmedName = fullName.trim()
        val trimmedUsername = username.trim().lowercase()
        val trimmedPhone = phoneNumber.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedPassword = password.trim()

        if (trimmedName.isBlank()) return Result.failure(IllegalArgumentException("Enter your full name."))
        if (trimmedUsername.isBlank()) return Result.failure(IllegalArgumentException("Enter a username."))
        if (!trimmedUsername.matches(Regex("^[a-zA-Z0-9._-]{3,20}$"))) {
            return Result.failure(IllegalArgumentException("Username must be 3-20 characters and use letters, numbers, dot, underscore, or dash."))
        }
        if (!isValidEmail(trimmedEmail)) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        if (trimmedPhone.isNotBlank() && (trimmedPhone.length < 8 || !trimmedPhone.all { it.isDigit() || it == '+' || it == ' ' || it == '-' })) {
            return Result.failure(IllegalArgumentException("Enter a valid phone number."))
        }
        if (trimmedPassword.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))

        prefs.edit()
            .putString(KEY_NAME, trimmedName)
            .putString(KEY_USERNAME, trimmedUsername)
            .putString(KEY_PHONE, trimmedPhone)
            .putString(KEY_EMAIL, trimmedEmail)
            .putString(KEY_PASSWORD, trimmedPassword)
            .putString(KEY_ACTIVE_NAME, trimmedName)
            .putString(KEY_ACTIVE_USERNAME, trimmedUsername)
            .putString(KEY_ACTIVE_PHONE, trimmedPhone)
            .putString(KEY_ACTIVE_EMAIL, trimmedEmail)
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .apply()

        _authState.value = AuthState(
            isAuthenticated = true,
            displayName = trimmedName,
            username = trimmedUsername,
            phoneNumber = trimmedPhone,
            email = trimmedEmail,
            rememberMe = rememberMe,
            hasRegisteredAccount = true
        )
        return Result.success(Unit)
    }

    fun signIn(identifier: String, password: String): Result<Unit> {
        return signIn(identifier, password, rememberMe = false)
    }

    fun signIn(identifier: String, password: String, rememberMe: Boolean): Result<Unit> {
        val trimmedIdentifier = identifier.trim().lowercase()
        val trimmedPassword = password.trim()
        if (trimmedIdentifier.isBlank()) return Result.failure(IllegalArgumentException("Enter your email or username."))
        if (trimmedPassword.isBlank()) return Result.failure(IllegalArgumentException("Enter your password."))
        if (trimmedIdentifier.contains("@") && !isValidEmail(trimmedIdentifier)) {
            return Result.failure(IllegalArgumentException("Enter a valid email format."))
        }

        val registeredEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        val registeredUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        val registeredPhone = prefs.getString(KEY_PHONE, "") ?: ""
        val registeredPassword = prefs.getString(KEY_PASSWORD, "") ?: ""
        val registeredName = prefs.getString(KEY_NAME, "") ?: ""

        if (registeredEmail.isBlank() && registeredUsername.isBlank() && registeredPhone.isBlank()) {
            return Result.failure(IllegalStateException("Create an account first."))
        }

        val matchesEmail = trimmedIdentifier == registeredEmail.lowercase()
        val matchesUsername = trimmedIdentifier == registeredUsername.lowercase()
        val matchesPhone = normalizePhone(trimmedIdentifier) == normalizePhone(registeredPhone)

        if (!matchesEmail && !matchesUsername && !matchesPhone) {
            return Result.failure(IllegalArgumentException("Wrong email or username."))
        }
        if (trimmedPassword != registeredPassword) {
            return Result.failure(IllegalArgumentException("Wrong password."))
        }

        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_ACTIVE_NAME, registeredName)
            .putString(KEY_ACTIVE_USERNAME, registeredUsername)
            .putString(KEY_ACTIVE_PHONE, registeredPhone)
            .putString(KEY_ACTIVE_EMAIL, registeredEmail)
            .apply()

        _authState.value = AuthState(
            isAuthenticated = true,
            displayName = registeredName,
            username = registeredUsername,
            phoneNumber = registeredPhone,
            email = registeredEmail,
            rememberMe = rememberMe,
            hasRegisteredAccount = true
        )
        return Result.success(Unit)
    }

    fun requestPasswordReset(email: String): Result<Unit> {
        val trimmedEmail = email.trim().lowercase()
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }

        val registeredEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        if (registeredEmail.isBlank() || trimmedEmail != registeredEmail.lowercase()) {
            return Result.failure(IllegalArgumentException("Wrong email."))
        }

        return Result.success(Unit)
    }

    fun continueWithGoogle(): Result<Unit> {
        return Result.failure(IllegalStateException("Server/network error. Google login requires backend or Firebase Auth setup."))
    }

    fun signOut() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
        prefs.edit().putBoolean(KEY_REMEMBER_ME, false).apply()
        _authState.value = _authState.value.copy(isAuthenticated = false)
    }

    private fun normalizePhone(value: String): String {
        return value.filter { it.isDigit() || it == '+' }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private companion object {
        const val KEY_NAME = "registered_name"
        const val KEY_USERNAME = "registered_username"
        const val KEY_PHONE = "registered_phone"
        const val KEY_EMAIL = "registered_email"
        const val KEY_PASSWORD = "registered_password"
        const val KEY_ACTIVE_NAME = "active_name"
        const val KEY_ACTIVE_USERNAME = "active_username"
        const val KEY_ACTIVE_PHONE = "active_phone"
        const val KEY_ACTIVE_EMAIL = "active_email"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_REMEMBER_ME = "remember_me"
    }
}