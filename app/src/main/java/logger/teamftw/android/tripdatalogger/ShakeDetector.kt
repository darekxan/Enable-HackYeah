package logger.teamftw.android.tripdatalogger

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector : SensorEventListener {
    companion object {
        private val HIGH_THRESHOLD_GRAVITY = 4.0f
        private val MEDIUM_THRESHOLD_GRAVITY = 2.5f
        private val LOW_THRESHOLD_GRAVITY = 2.0f


        private val SHAKE_SLOP_TIME_MS = 2000
        private val SHAKE_COUNT_RESET_TIME_MS = 3000
    }


    private var mListener: OnShakeListener? = null
    private var mLastShakeTimestamp: Long = 0
    private var time0 = System.currentTimeMillis()
    var eventList = mutableListOf<ShakeEvent>()

    fun setOnShakeListener(listener: OnShakeListener) {
        this.mListener = listener
    }

    interface OnShakeListener {
        fun onShake(event: ShakeEvent)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // ignore
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (mListener != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // gForce will be close to 1 when there is no movement.
            val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
            val now = System.currentTimeMillis()
            if (mLastShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            if (gForce > HIGH_THRESHOLD_GRAVITY) {
                val shakeEvent = ShakeEvent(ShakeEvent.Level.HIGH, relative(now))
                eventList.add(shakeEvent)
                mListener!!.onShake(shakeEvent)
                mLastShakeTimestamp = now
            } else if (gForce > MEDIUM_THRESHOLD_GRAVITY) {
                val shakeEvent = ShakeEvent(ShakeEvent.Level.MEDIUM, relative(now))
                eventList.add(shakeEvent)
                mListener!!.onShake(shakeEvent)
                mLastShakeTimestamp = now
            } else if (gForce > LOW_THRESHOLD_GRAVITY) {
                val shakeEvent = ShakeEvent(ShakeEvent.Level.LOW, relative(now))
                eventList.add(shakeEvent)
                mListener!!.onShake(shakeEvent)
                mLastShakeTimestamp = now
            }
        }
    }

    private fun relative(now: Long) = (now - time0) / 1000
}