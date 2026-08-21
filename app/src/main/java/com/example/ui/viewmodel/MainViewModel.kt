package com.example.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.UserSession
import com.example.UserSessionStore
import com.example.data.db.AlertLog
import com.example.data.db.AppDatabase
import com.example.data.db.EmergencyContact
import com.example.data.db.UserAccount
import com.example.data.preferences.SafetySettings
import com.example.data.preferences.UserPreferencesManager
import com.example.emergency.EmergencyDispatcher
import com.example.ml.MLContextAnalyzer
import com.example.sensor.SecretTapDetector
import com.example.sensor.ShakeDetector
import com.example.voice.AuthManager
import com.example.voice.VoiceTriggerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val contactDao = db.contactDao()
    private val alertDao = db.alertDao()
    private val userAccountDao = db.userAccountDao()
    private val prefsManager = UserPreferencesManager(application)
    private val authManager = AuthManager(application)
    private val sessionStore = UserSessionStore(application)
    val dispatcher = EmergencyDispatcher(application, contactDao, alertDao, sessionStore)
    private val voiceTriggerManager = VoiceTriggerManager(application) { recognizedPhrase ->
        if (safetySettings.value.voiceCommandEnabled && activeCountdownSec.value == null) {
            _lastHeardVoicePhrase.value = recognizedPhrase
            triggerEmergencyAlert(
                triggerType = "Voice Keyword (\"${safetySettings.value.voiceKeyword}\")",
                intensity = 8f,
                bypassCountdown = true
            )
        }
    }

    val contacts = contactDao.getAllContacts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentUserSession = MutableStateFlow(sessionStore.loadSession())
    val currentUserSession: StateFlow<UserSession> = _currentUserSession.asStateFlow()

    val alertLogs = _currentUserSession
        .combine(
            alertDao.getAllAlerts()
        ) { session, logs ->
            if (session.accountId == 0L) emptyList() else logs.filter { it.accountId == session.accountId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val safetySettings = prefsManager.settings
    val authState = authManager.authState

    private val _liveShakeIntensity = MutableStateFlow(0f)
    val liveShakeIntensity: StateFlow<Float> = _liveShakeIntensity.asStateFlow()

    private val _currentLocationCoords = MutableStateFlow(dispatcher.getCurrentLocationCoordinates())
    val currentLocationCoords: StateFlow<Pair<Double, Double>> = _currentLocationCoords.asStateFlow()

    private val _activeCountdownSec = MutableStateFlow<Int?>(null)
    val activeCountdownSec: StateFlow<Int?> = _activeCountdownSec.asStateFlow()

    private val _activeCountdownTriggerType = MutableStateFlow<String?>(null)
    val activeCountdownTriggerType: StateFlow<String?> = _activeCountdownTriggerType.asStateFlow()

    private val _lastDispatchedAlert = MutableStateFlow<AlertLog?>(null)
    val lastDispatchedAlert: StateFlow<AlertLog?> = _lastDispatchedAlert.asStateFlow()

    private val _stealthModeActive = MutableStateFlow(safetySettings.value.stealthModeEnabled)
    val stealthModeActive: StateFlow<Boolean> = _stealthModeActive.asStateFlow()

    private val _stealthDarkModeActive = MutableStateFlow(safetySettings.value.stealthDarkModeEnabled)
    val stealthDarkModeActive: StateFlow<Boolean> = _stealthDarkModeActive.asStateFlow()

    private val _permissionStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionStatusMap: StateFlow<Map<String, Boolean>> = _permissionStatusMap.asStateFlow()

    private val _voiceListeningActive = MutableStateFlow(false)
    val voiceListeningActive: StateFlow<Boolean> = _voiceListeningActive.asStateFlow()

    private val _lastHeardVoicePhrase = MutableStateFlow<String?>(null)
    val lastHeardVoicePhrase: StateFlow<String?> = _lastHeardVoicePhrase.asStateFlow()

    private var countdownJob: Job? = null

    private fun getCurrentAccountId(): Long = _currentUserSession.value.accountId

    // Shake Detector setup
    private val shakeDetector = ShakeDetector(
        context = application,
        threshold = safetySettings.value.shakeThreshold,
        onShakeDetected = { intensity ->
            if (safetySettings.value.shakeEnabled && activeCountdownSec.value == null) {
                triggerEmergencyAlert("Shake Detected", intensity)
            }
        },
        onIntensityUpdate = { intensity ->
            _liveShakeIntensity.value = intensity
        }
    )

    // Secret Tap Detector setup
    val secretTapDetector = SecretTapDetector(
        targetTaps = 3,
        maxIntervalMs = 1200,
        onTapPatternTriggered = {
            if (safetySettings.value.secretTapEnabled && activeCountdownSec.value == null) {
                triggerEmergencyAlert("Secret Tap Pattern", 10f)
            }
        }
    )

    init {
        if (safetySettings.value.shakeEnabled) {
            shakeDetector.start()
        }

        refreshPermissions()
        refreshVoiceMonitoring()
    }

    fun updateSettings(newSettings: SafetySettings) {
        prefsManager.updateSettings(newSettings)
        shakeDetector.updateThreshold(newSettings.shakeThreshold)
        if (newSettings.shakeEnabled) {
            shakeDetector.start()
        } else {
            shakeDetector.stop()
        }
        refreshVoiceMonitoring()
    }

    fun triggerEmergencyAlert(triggerType: String, intensity: Float = 0f, bypassCountdown: Boolean = false) {
        if (safetySettings.value.autoStealthOnEmergency) {
            toggleStealthDarkMode(true)
        }

        val countdownDuration = safetySettings.value.countdownDurationSec

        if (bypassCountdown || countdownDuration <= 0) {
            executeDispatch(triggerType, intensity)
        } else {
            startCountdown(triggerType, intensity, countdownDuration)
        }
    }

    fun saveSessionFromLogin(account: UserAccount, rememberMe: Boolean) {
        val session = UserSession(
            accountId = account.id,
            loginId = account.loginId,
            displayName = account.displayName,
            username = account.username,
            email = account.email,
            phoneNumber = account.phoneNumber,
            rememberMe = rememberMe,
            lastAlertHistoryCount = 0
        )
        sessionStore.saveSession(session)
        _currentUserSession.value = session
    }

    private fun startCountdown(triggerType: String, intensity: Float, totalSec: Int) {
        countdownJob?.cancel()
        _activeCountdownTriggerType.value = triggerType
        _activeCountdownSec.value = totalSec

        countdownJob = viewModelScope.launch {
            for (sec in totalSec downTo 1) {
                _activeCountdownSec.value = sec
                delay(1000L)
            }
            _activeCountdownSec.value = null
            _activeCountdownTriggerType.value = null
            executeDispatch(triggerType, intensity)
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _activeCountdownSec.value = null
        _activeCountdownTriggerType.value = null
    }

    private fun executeDispatch(triggerType: String, intensity: Float) {
        viewModelScope.launch {
            _currentLocationCoords.value = dispatcher.getCurrentLocationCoordinates()
            val alert = dispatcher.dispatchAlert(
                triggerType = triggerType,
                movementIntensity = intensity,
                customNote = safetySettings.value.customEmergencyMessage,
                accountId = getCurrentAccountId()
            )
            _lastDispatchedAlert.value = alert
        }
    }

    fun addContact(contact: EmergencyContact) {
        viewModelScope.launch {
            contactDao.insertContact(contact)
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            contactDao.updateContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            contactDao.deleteContact(contact)
        }
    }

    fun syncOfflineQueue() {
        viewModelScope.launch {
            dispatcher.syncOfflineQueue()
        }
    }

    fun clearAlertHistory() {
        viewModelScope.launch {
            alertDao.clearAlertsForAccount(getCurrentAccountId())
            _lastDispatchedAlert.value = null
        }
    }

    // Stealth Mode Handling
    fun toggleStealthMode(enabled: Boolean) {
        val current = safetySettings.value
        updateSettings(current.copy(stealthModeEnabled = enabled))
        _stealthModeActive.value = enabled
    }

    fun toggleStealthDarkMode(enabled: Boolean) {
        val current = safetySettings.value
        updateSettings(current.copy(stealthDarkModeEnabled = enabled))
        _stealthDarkModeActive.value = enabled
    }

    fun verifyCalculatorPin(enteredPin: String): Boolean {
        val settings = safetySettings.value
        return when (enteredPin) {
            settings.realPin -> {
                _stealthModeActive.value = false
                true
            }
            settings.fakePin -> {
                // Secret Covert Alert Triggered on Fake Pin!
                triggerEmergencyAlert("Covert Fake PIN Entered", intensity = 5f, bypassCountdown = true)
                false
            }
            else -> false
        }
    }

    fun refreshPermissions() {
        val ctx = getApplication<Application>()
        val permissions = listOf(
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.RECORD_AUDIO
        )
        val map = permissions.associateWith { perm ->
            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
        }
        _permissionStatusMap.value = map
        refreshVoiceMonitoring()
    }

    fun signIn(identifier: String, password: String, rememberMe: Boolean): Result<Unit> {
        val authResult = authManager.signIn(identifier, password, rememberMe)
        if (authResult.isFailure) {
            return authResult
        }

        viewModelScope.launch {
            val account = userAccountDao.findByLoginId(identifier.trim().lowercase())
                ?: userAccountDao.findByEmail(identifier.trim().lowercase())
            if (account != null) {
                saveSessionFromLogin(account, rememberMe)
                if (stealthModeActive.value) {
                    toggleStealthMode(false)
                }
            }
        }

        return Result.success(Unit)
    }

    fun signUp(
        fullName: String,
        username: String,
        email: String,
        phoneNumber: String,
        password: String,
        rememberMe: Boolean
    ): Result<Unit> {
        val authResult = authManager.signUp(fullName, username, email, phoneNumber, password, rememberMe)
        if (authResult.isFailure) {
            return authResult
        }

        viewModelScope.launch {
            val loginId = username.trim().lowercase()
            val account = UserAccount(
                loginId = loginId,
                displayName = fullName.trim(),
                username = username.trim(),
                email = email.trim().lowercase(),
                phoneNumber = phoneNumber.trim(),
                passwordHash = password.trim(),
            )
            val accountId = userAccountDao.saveAccount(account)
            val savedAccount = account.copy(id = accountId)
            saveSessionFromLogin(savedAccount, rememberMe)
            if (stealthModeActive.value) {
                toggleStealthMode(false)
            }
        }

        return Result.success(Unit)
    }

    fun requestPasswordReset(email: String): Result<Unit> {
        return authManager.requestPasswordReset(email)
    }

    fun continueWithGoogle(): Result<Unit> {
        return authManager.continueWithGoogle()
    }

    fun signOut() {
        authManager.signOut()
        sessionStore.clearSession()
        _currentUserSession.value = UserSession()
        stopVoiceMonitoring()
    }

    private fun refreshVoiceMonitoring() {
        val canListen = safetySettings.value.voiceCommandEnabled &&
            safetySettings.value.audioTriggerEnabled &&
            _permissionStatusMap.value[android.Manifest.permission.RECORD_AUDIO] == true

        if (canListen) {
            voiceTriggerManager.startListening(
                expectedKeyword = safetySettings.value.voiceKeyword,
                onHeardPhrase = { heardPhrase ->
                    _lastHeardVoicePhrase.value = heardPhrase
                }
            )
            _voiceListeningActive.value = voiceTriggerManager.listeningState.value
        } else {
            voiceTriggerManager.stopListening()
            _voiceListeningActive.value = false
        }
    }

    private fun stopVoiceMonitoring() {
        voiceTriggerManager.stopListening()
        _voiceListeningActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
    }
}
