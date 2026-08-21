package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SafetySettings(
    val shakeEnabled: Boolean = true,
    val shakeThreshold: Float = 14.0f, // Acceleration threshold m/s^2
    val longPressDurationSec: Int = 3,
    val secretTapEnabled: Boolean = true,
    val voiceCommandEnabled: Boolean = true,
    val voiceKeyword: String = "CODE RED",
    val wearableSimEnabled: Boolean = true,
    val smsDispatchEnabled: Boolean = true,
    val locationTrackingEnabled: Boolean = true,
    val callEscalationEnabled: Boolean = true,
    val audioTriggerEnabled: Boolean = true,
    val stealthModeEnabled: Boolean = false,
    val stealthDarkModeEnabled: Boolean = false,
    val autoStealthOnEmergency: Boolean = true,
    val realPin: String = "1234",
    val fakePin: String = "9999",
    val autoCallEscalation: Boolean = true,
    val countdownDurationSec: Int = 3,
    val mlContextFilterEnabled: Boolean = true,
    val customEmergencyMessage: String = "EMERGENCY ALERT: I am in danger and need silent assistance. Track my live location:"
)

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("silent_signal_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SafetySettings> = _settings.asStateFlow()

    private fun loadSettings(): SafetySettings {
        return SafetySettings(
            shakeEnabled = prefs.getBoolean("shakeEnabled", true),
            shakeThreshold = prefs.getFloat("shakeThreshold", 14.0f),
            longPressDurationSec = prefs.getInt("longPressDurationSec", 3),
            secretTapEnabled = prefs.getBoolean("secretTapEnabled", true),
            voiceCommandEnabled = prefs.getBoolean("voiceCommandEnabled", true),
            voiceKeyword = prefs.getString("voiceKeyword", "CODE RED") ?: "CODE RED",
            wearableSimEnabled = prefs.getBoolean("wearableSimEnabled", true),
            smsDispatchEnabled = prefs.getBoolean("smsDispatchEnabled", true),
            locationTrackingEnabled = prefs.getBoolean("locationTrackingEnabled", true),
            callEscalationEnabled = prefs.getBoolean("callEscalationEnabled", true),
            audioTriggerEnabled = prefs.getBoolean("audioTriggerEnabled", true),
            stealthModeEnabled = prefs.getBoolean("stealthModeEnabled", false),
            stealthDarkModeEnabled = prefs.getBoolean("stealthDarkModeEnabled", false),
            autoStealthOnEmergency = prefs.getBoolean("autoStealthOnEmergency", true),
            realPin = prefs.getString("realPin", "1234") ?: "1234",
            fakePin = prefs.getString("fakePin", "9999") ?: "9999",
            autoCallEscalation = prefs.getBoolean("autoCallEscalation", true),
            countdownDurationSec = prefs.getInt("countdownDurationSec", 3),
            mlContextFilterEnabled = prefs.getBoolean("mlContextFilterEnabled", true),
            customEmergencyMessage = prefs.getString("customEmergencyMessage", "EMERGENCY ALERT: I need help! My live location:")
                ?: "EMERGENCY ALERT: I need help! My live location:"
        )
    }

    fun updateSettings(newSettings: SafetySettings) {
        prefs.edit().apply {
            putBoolean("shakeEnabled", newSettings.shakeEnabled)
            putFloat("shakeThreshold", newSettings.shakeThreshold)
            putInt("longPressDurationSec", newSettings.longPressDurationSec)
            putBoolean("secretTapEnabled", newSettings.secretTapEnabled)
            putBoolean("voiceCommandEnabled", newSettings.voiceCommandEnabled)
            putString("voiceKeyword", newSettings.voiceKeyword)
            putBoolean("wearableSimEnabled", newSettings.wearableSimEnabled)
            putBoolean("smsDispatchEnabled", newSettings.smsDispatchEnabled)
            putBoolean("locationTrackingEnabled", newSettings.locationTrackingEnabled)
            putBoolean("callEscalationEnabled", newSettings.callEscalationEnabled)
            putBoolean("audioTriggerEnabled", newSettings.audioTriggerEnabled)
            putBoolean("stealthModeEnabled", newSettings.stealthModeEnabled)
            putBoolean("stealthDarkModeEnabled", newSettings.stealthDarkModeEnabled)
            putBoolean("autoStealthOnEmergency", newSettings.autoStealthOnEmergency)
            putString("realPin", newSettings.realPin)
            putString("fakePin", newSettings.fakePin)
            putBoolean("autoCallEscalation", newSettings.autoCallEscalation)
            putInt("countdownDurationSec", newSettings.countdownDurationSec)
            putBoolean("mlContextFilterEnabled", newSettings.mlContextFilterEnabled)
            putString("customEmergencyMessage", newSettings.customEmergencyMessage)
            apply()
        }
        _settings.value = newSettings
    }
}
