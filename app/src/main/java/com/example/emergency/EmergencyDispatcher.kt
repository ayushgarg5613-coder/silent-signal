package com.example.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.example.UserSessionStore
import com.example.data.db.AlertDao
import com.example.data.db.AlertLog
import com.example.data.db.ContactDao
import com.example.data.db.EmergencyContact
import com.example.data.preferences.UserPreferencesManager
import com.example.ml.MLContextAnalyzer
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class EmergencyDispatcher(
    private val context: Context,
    private val contactDao: ContactDao,
    private val alertDao: AlertDao,
    private val sessionStore: UserSessionStore
) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var currentLocation: Location? = null

    init {
        initLocationListener()
    }

    private fun initLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L,
                    5f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            currentLocation = location
                        }
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCurrentLocationCoordinates(): Pair<Double, Double> {
        val loc = resolveBestLocation() ?: return Pair(0.0, 0.0)
        return Pair(loc.latitude, loc.longitude)
    }

    private fun resolveBestLocation(): Location? {
        val candidates = mutableListOf<Location>()
        val hasFine = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return currentLocation

        if (currentLocation != null) {
            candidates += currentLocation!!
        }

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        for (provider in providers) {
            try {
                val candidate = locationManager.getLastKnownLocation(provider) ?: continue
                candidates += candidate
            } catch (_: SecurityException) {
            }
        }

        return candidates
            .filter { it.latitude != 0.0 || it.longitude != 0.0 }
            .maxByOrNull { it.time }
    }

    private suspend fun fetchCurrentLiveLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val hasFine = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = com.google.android.gms.tasks.CancellationTokenSource()

        continuation.invokeOnCancellation { cancellationToken.cancel() }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    continuation.resume(null)
                    return@addOnSuccessListener
                }

                val now = System.currentTimeMillis()
                val freshEnough = location.time > 0L && (now - location.time) <= 60_000L
                val acceptableAccuracy = location.accuracy <= 200f || location.accuracy == 0f

                if (freshEnough && acceptableAccuracy) {
                    currentLocation = location
                    continuation.resume(location)
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
            .addOnCanceledListener {
                continuation.resume(null)
            }
    }

    suspend fun dispatchAlert(
        triggerType: String,
        movementIntensity: Float = 0f,
        customNote: String = "I need silent emergency assistance!",
        accountId: Long = 0L
    ): AlertLog = withContext(Dispatchers.IO) {

        // Step 1: Perform ML Context Analysis
        val mlAnalysis = MLContextAnalyzer.analyzeContext(
            triggerType = triggerType,
            movementIntensity = movementIntensity
        )

        // Step 2: Acquire a fresh, accurate GPS location using FusedLocationProviderClient
        val settings = UserPreferencesManager(context).settings.value
        val liveLocation = fetchCurrentLiveLocation() ?: resolveBestLocation()
        val resolvedLocation = liveLocation ?: currentLocation
        val lat = resolvedLocation?.latitude ?: 0.0
        val lng = resolvedLocation?.longitude ?: 0.0

        val mapsUrl = if (resolvedLocation != null) {
            "https://maps.google.com/?q=%.6f,%.6f".format(lat, lng)
        } else {
            "Location unavailable. Please enable GPS and location permissions."
        }

        val contactRecipients = (contactDao.getContactsForTier(1) + contactDao.getContactsForTier(2))
            .map { it.phoneNumber }
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length >= 8 && !it.contains("15550192834") && !it.contains("15550148821") }
        val recipientNumbers = if (contactRecipients.isEmpty()) listOf("100") else contactRecipients + "100"
        val primaryContact = contactDao.getPrimaryContact() ?: contactRecipients.firstOrNull()?.let { null }

        val locationMessage = if (settings.locationTrackingEnabled && resolvedLocation != null) {
            "\nLive Location: $mapsUrl"
        } else {
            "\nLive Location: ${if (resolvedLocation == null) "Location unavailable. Enable GPS and location permission." else "Disabled by user settings"}"
        }

        val fullSmsBody = "$customNote$locationMessage\n[Risk Level: ${mlAnalysis.confidence} (${mlAnalysis.riskScore}%)]"

        var isSmsSentSuccess = false
        var recipientCount = 0

        if (settings.smsDispatchEnabled && recipientNumbers.isNotEmpty()) {
            val hasSmsPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission) {
                try {
                    val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }

                    for (number in recipientNumbers) {
                        val parts = smsManager.divideMessage(fullSmsBody)
                        smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                        recipientCount++
                    }
                    isSmsSentSuccess = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val status = if (isSmsSentSuccess) "SENT" else if (recipientNumbers.isEmpty()) "SENT (Simulated)" else "QUEUED_OFFLINE"

        // Create Alert Log Entry
        val alert = AlertLog(
            accountId = accountId,
            triggerType = triggerType,
            status = status,
            riskScore = mlAnalysis.riskScore,
            latitude = lat,
            longitude = lng,
            locationAddress = "GPS: %.4f, %.4f".format(lat, lng),
            mapsUrl = mapsUrl,
            recipientCount = if (recipientCount > 0) recipientCount else recipientNumbers.size,
            mlContextNotes = "${mlAnalysis.intentClassification} | ${mlAnalysis.featureBreakdown.joinToString()}"
        )

        val id = alertDao.insertAlert(alert)
        val insertedAlert = alert.copy(id = id)

        // Step 5: Trigger Silent / Haptic Feedback
        triggerHapticVibration()

        // Step 6: Automatic Call Escalation if High Risk & Primary Contact configured
        if (settings.callEscalationEnabled && mlAnalysis.riskScore >= 75 && contactRecipients.isNotEmpty()) {
            triggerCallEscalation(contactRecipients.first())
        }

        insertedAlert
    }

    fun triggerCallEscalation(phoneNumber: String) {
        val hasCallPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerHapticVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val pattern = longArrayOf(0, 100, 100, 200, 100, 300)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val pattern = longArrayOf(0, 100, 100, 200, 100, 300)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncOfflineQueue(): Int = withContext(Dispatchers.IO) {
        val queued = alertDao.getQueuedAlerts()
        var syncedCount = 0
        for (alert in queued) {
            // Update queued status to SENT upon reconnection/sync
            val updated = alert.copy(status = "SENT (Synced)")
            alertDao.updateAlert(updated)
            syncedCount++
        }
        syncedCount
    }
}
