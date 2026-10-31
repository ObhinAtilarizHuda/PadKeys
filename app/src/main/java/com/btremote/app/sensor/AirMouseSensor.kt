package com.btremote.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sign
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class AirMouseSensor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val gyro: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val gameRotVec: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val rotVec: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accel: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun isAvailable(): Boolean =
        gyro != null || gameRotVec != null || rotVec != null || accel != null

    fun isGameModeAvailable(): Boolean =
        gameRotVec != null || rotVec != null

    fun deltas(sensitivity: Int, invert: Boolean = false): Flow<Pair<Float, Float>> = callbackFlow {
        val sm = sensorManager ?: run { close(); return@callbackFlow }

        // 1) Classic gyroscope
        if (gyro != null) {
            val alpha = 0.8f
            var prevX = 0f
            var prevY = 0f
            var initialized = false
            val multiplier = sensitivity.coerceIn(1, 20).toFloat() * 4f
            val sign = if (invert) 1f else -1f
            val deadZone = 0.05f
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return
                    val rawX = event.values[1]
                    val rawY = event.values[0]
                    if (!initialized) {
                        prevX = rawX; prevY = rawY; initialized = true
                    }
                    val fx = alpha * rawX + (1 - alpha) * prevX
                    val fy = alpha * rawY + (1 - alpha) * prevY
                    prevX = fx; prevY = fy
                    val ax = if (abs(fx) < deadZone) 0f else fx
                    val ay = if (abs(fy) < deadZone) 0f else fy
                    fun curve(v: Float): Float {
                        val a = abs(v)
                        return sign(v) * (a * multiplier + a * a * multiplier * 0.15f)
                    }
                    val dx = sign * curve(ax)
                    val dy = sign * curve(ay)
                    if (dx != 0f || dy != 0f) trySend(dx to dy)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { sm.unregisterListener(listener) }
            return@callbackFlow
        }

        // 2) Fused rotation vector (no gyro needed)
        val fused = gameRotVec ?: rotVec
        if (fused != null) {
            val multiplier = sensitivity.coerceIn(1, 20).toFloat() * 35f
            val sign = if (invert) 1f else -1f
            val deadZone = 0.006f
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            var prevYaw = Float.NaN
            var prevPitch = Float.NaN
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    if (event.sensor.type != fused.type) return
                    SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                    SensorManager.getOrientation(rotMatrix, orientation)
                    val yaw = orientation[0]
                    val pitch = orientation[1]
                    if (prevYaw.isNaN() || prevPitch.isNaN()) {
                        prevYaw = yaw; prevPitch = pitch; return
                    }
                    var dYaw = yaw - prevYaw
                    var dPitch = pitch - prevPitch
                    if (dYaw > Math.PI) dYaw -= (2 * Math.PI).toFloat()
                    if (dYaw < -Math.PI) dYaw += (2 * Math.PI).toFloat()
                    if (dPitch > Math.PI) dPitch -= (2 * Math.PI).toFloat()
                    if (dPitch < -Math.PI) dPitch += (2 * Math.PI).toFloat()
                    prevYaw = yaw
                    prevPitch = pitch
                    val ax = if (abs(dYaw) < deadZone) 0f else dYaw
                    val ay = if (abs(dPitch) < deadZone) 0f else dPitch
                    fun curve(v: Float): Float {
                        val a = abs(v)
                        return sign(v) * (a * multiplier + a * a * multiplier * 0.15f)
                    }
                    val dx = sign * curve(ax)
                    val dy = sign * curve(ay)
                    if (dx != 0f || dy != 0f) trySend(dx to dy)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, fused, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { sm.unregisterListener(listener) }
            return@callbackFlow
        }

        // 3) Accelerometer tilt fallback
        if (accel != null) {
            val alpha = 0.85f
            var gx = 0f; var gy = 0f; var gz = 0f
            var initialized = false
            var prevPitch = 0f; var prevRoll = 0f
            var havePrev = false
            val multiplier = sensitivity.coerceIn(1, 20).toFloat() * 25f
            val sign = if (invert) 1f else -1f
            val deadZone = 0.02f
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                    if (!initialized) {
                        gx = event.values[0]; gy = event.values[1]; gz = event.values[2]; initialized = true; return
                    }
                    gx = alpha * event.values[0] + (1 - alpha) * gx
                    gy = alpha * event.values[1] + (1 - alpha) * gy
                    gz = alpha * event.values[2] + (1 - alpha) * gz
                    val pitch = atan2(-gx.toDouble(), hypot(gy.toDouble(), gz.toDouble())).toFloat()
                    val roll = atan2(gy, gz)
                    if (!havePrev) { prevPitch = pitch; prevRoll = roll; havePrev = true; return }
                    var dPitch = pitch - prevPitch
                    var dRoll = roll - prevRoll
                    if (dPitch > Math.PI) dPitch -= (2 * Math.PI).toFloat()
                    if (dPitch < -Math.PI) dPitch += (2 * Math.PI).toFloat()
                    if (dRoll > Math.PI) dRoll -= (2 * Math.PI).toFloat()
                    if (dRoll < -Math.PI) dRoll += (2 * Math.PI).toFloat()
                    prevPitch = pitch; prevRoll = roll
                    val ax = if (abs(dPitch) < deadZone) 0f else dPitch
                    val ay = if (abs(dRoll) < deadZone) 0f else dRoll
                    fun curve(v: Float): Float {
                        val a = abs(v)
                        return sign(v) * (a * multiplier + a * a * multiplier * 0.1f)
                    }
                    val dx = sign * curve(ay)
                    val dy = sign * curve(ax)
                    if (dx != 0f || dy != 0f) trySend(dx to dy)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { sm.unregisterListener(listener) }
            return@callbackFlow
        }

        close()
    }

    fun gameDeltas(sensitivity: Int, invert: Boolean = false): Flow<Pair<Float, Float>> = callbackFlow {
        val sm = sensorManager ?: run { close(); return@callbackFlow }
        val fused = gameRotVec ?: rotVec
        if (fused != null) {
            val multiplier = sensitivity.coerceIn(1, 20).toFloat() * 35f
            val sign = if (invert) 1f else -1f
            val deadZone = 0.006f
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            var prevYaw = Float.NaN
            var prevPitch = Float.NaN
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    if (event.sensor.type != fused.type) return
                    SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                    SensorManager.getOrientation(rotMatrix, orientation)
                    val yaw = orientation[0]
                    val pitch = orientation[1]
                    if (prevYaw.isNaN() || prevPitch.isNaN()) {
                        prevYaw = yaw; prevPitch = pitch; return
                    }
                    var dYaw = yaw - prevYaw
                    var dPitch = pitch - prevPitch
                    if (dYaw > Math.PI) dYaw -= (2 * Math.PI).toFloat()
                    if (dYaw < -Math.PI) dYaw += (2 * Math.PI).toFloat()
                    if (dPitch > Math.PI) dPitch -= (2 * Math.PI).toFloat()
                    if (dPitch < -Math.PI) dPitch += (2 * Math.PI).toFloat()
                    prevYaw = yaw
                    prevPitch = pitch
                    val ax = if (abs(dYaw) < deadZone) 0f else dYaw
                    val ay = if (abs(dPitch) < deadZone) 0f else dPitch
                    fun curve(v: Float): Float {
                        val a = abs(v)
                        return sign(v) * (a * multiplier + a * a * multiplier * 0.15f)
                    }
                    val dx = sign * curve(ax)
                    val dy = sign * curve(ay)
                    if (dx != 0f || dy != 0f) trySend(dx to dy)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, fused, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { sm.unregisterListener(listener) }
            return@callbackFlow
        }
        // Fallback
        deltas(sensitivity, invert).collect { trySend(it) }
        close()
    }
}
