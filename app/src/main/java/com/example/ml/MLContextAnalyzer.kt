package com.example.ml

import java.util.Calendar

data class MLEmergencyAnalysis(
    val riskScore: Int, // 0 - 100
    val confidence: String, // "CRITICAL", "HIGH", "MODERATE", "LOW"
    val intentClassification: String,
    val isFalsePositiveLikely: Boolean,
    val featureBreakdown: List<String>,
    val recommendation: String
)

object MLContextAnalyzer {

    fun analyzeContext(
        triggerType: String,
        movementIntensity: Float,
        tapIntervalMs: Long = 300,
        repeatCountIn30s: Int = 1,
        speedKmH: Float = 0f
    ): MLEmergencyAnalysis {
        var baseScore = when (triggerType) {
            "SOS Long-Press" -> 85
            "Voice Keyword" -> 90
            "Wearable BLE" -> 80
            "Secret Tap Pattern" -> 75
            "Shake Detected" -> 65
            else -> 60
        }

        val features = mutableListOf<String>()

        // Feature 1: Movement Intensity (Accelerometer ML Feature)
        if (movementIntensity > 20.0f) {
            baseScore += 15
            features.add("Extreme Violent Shake Detected (${"%.1f".format(movementIntensity)} m/s²)")
        } else if (movementIntensity > 12.0f) {
            baseScore += 8
            features.add("High Rapid Motion (${"%.1f".format(movementIntensity)} m/s²)")
        } else {
            features.add("Normal Gesture Motion (${"%.1f".format(movementIntensity)} m/s²)")
        }

        // Feature 2: Time of Day (Temporal ML Risk Model)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isLateNight = currentHour >= 22 || currentHour <= 5
        if (isLateNight) {
            baseScore += 15
            features.add("Temporal Risk Factor: Late Night Hours ($currentHour:00)")
        } else {
            features.add("Temporal Context: Standard Daylight")
        }

        // Feature 3: Gesture Cadence & Rapid Repetition
        if (repeatCountIn30s >= 2) {
            baseScore += 12
            features.add("Panic Repetition Pattern: $repeatCountIn30s triggers in 30s")
        }

        // Feature 4: High Transit Velocity Risk
        if (speedKmH > 60f) {
            baseScore += 10
            features.add("High Speed Transport Context (${speedKmH.toInt()} km/h)")
        }

        // Clamp final score
        val finalScore = baseScore.coerceIn(0, 100)

        val isFalsePositive = finalScore < 45
        val (confidence, classification) = when {
            finalScore >= 85 -> "CRITICAL" to "Genuine Physical Distress / High Threat"
            finalScore >= 70 -> "HIGH" to "Urgent Silent Coercion Trigger"
            finalScore >= 50 -> "MODERATE" to "Precautionary Safety Alert"
            else -> "LOW" to "Uncertain Intent / Pocket Tap Suspected"
        }

        val recommendation = when {
            finalScore >= 85 -> "Immediate Tier 1 & Tier 2 Call Escalation. Live Location Streaming Active."
            finalScore >= 70 -> "Dispatch Silent Emergency SMS with Live Map Link to All Contacts."
            finalScore >= 50 -> "Queued Emergency Dispatch with 3-second Safety Countdown."
            else -> "Low Confidence Alert: Require Manual Confirmation or 5-Second Cancel Window."
        }

        return MLEmergencyAnalysis(
            riskScore = finalScore,
            confidence = confidence,
            intentClassification = classification,
            isFalsePositiveLikely = isFalsePositive,
            featureBreakdown = features,
            recommendation = recommendation
        )
    }
}
