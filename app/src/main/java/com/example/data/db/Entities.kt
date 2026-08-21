package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val escalationTier: Int = 1, // 1: Primary SMS, 2: Call Escalation, 3: Panic Broadcast
    val customNote: String = "Please check on me immediately!"
)

@Entity(tableName = "alert_logs")
data class AlertLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerType: String, // "SOS Long-Press", "Shake Detected", "Secret Tap Pattern", "Voice Keyword", "Wearable BLE", "Manual Alert"
    val status: String, // "SENT", "QUEUED_OFFLINE", "CANCELLED", "CALL_ESCALATED"
    val riskScore: Int, // 0 to 100
    val latitude: Double,
    val longitude: Double,
    val locationAddress: String,
    val mapsUrl: String,
    val recipientCount: Int,
    val mlContextNotes: String
)
