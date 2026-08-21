package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private var threshold: Float = 14.0f,
    private val onShakeDetected: (Float) -> Unit,
    private val onIntensityUpdate: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0
    private val debounceMs: Long = 1800

    fun updateThreshold(newThreshold: Float) {
        threshold = newThreshold
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Gravity constant g ~ 9.81 m/s^2
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH
        val intensity = if (acceleration < 0) 0f else acceleration

        onIntensityUpdate(intensity)

        if (intensity >= threshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > debounceMs) {
                lastShakeTimestamp = now
                onShakeDetected(intensity)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

class SecretTapDetector(
    private val targetTaps: Int = 3,
    private val maxIntervalMs: Long = 1200,
    private val onTapPatternTriggered: () -> Unit
) {
    private var tapTimestamps = mutableListOf<Long>()

    fun registerTap() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)

        // Remove old taps outside time window
        tapTimestamps.removeAll { now - it > maxIntervalMs }

        if (tapTimestamps.size >= targetTaps) {
            tapTimestamps.clear()
            onTapPatternTriggered()
        }
    }
}
