package com.example

import android.content.Context
import android.content.SharedPreferences

data class UserSession(
    val accountId: Long = 0L,
    val loginId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val rememberMe: Boolean = false,
    val lastAlertHistoryCount: Int = 0
)

class UserSessionStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("silent_signal_session", Context.MODE_PRIVATE)

    fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putLong("account_id", session.accountId)
            putString("login_id", session.loginId)
            putString("display_name", session.displayName)
            putString("username", session.username)
            putString("email", session.email)
            putString("phone_number", session.phoneNumber)
            putBoolean("remember_me", session.rememberMe)
            putInt("last_alert_history_count", session.lastAlertHistoryCount)
            apply()
        }
    }

    fun loadSession(): UserSession {
        return UserSession(
            accountId = prefs.getLong("account_id", 0L),
            loginId = prefs.getString("login_id", "") ?: "",
            displayName = prefs.getString("display_name", "") ?: "",
            username = prefs.getString("username", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phoneNumber = prefs.getString("phone_number", "") ?: "",
            rememberMe = prefs.getBoolean("remember_me", false),
            lastAlertHistoryCount = prefs.getInt("last_alert_history_count", 0)
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
