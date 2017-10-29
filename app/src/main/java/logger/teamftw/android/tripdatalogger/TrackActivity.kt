package logger.teamftw.android.tripdatalogger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import com.akhgupta.easylocation.EasyLocationAppCompatActivity
import com.akhgupta.easylocation.EasyLocationRequestBuilder
import com.github.kittinunf.fuel.httpPut
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_track.*
import com.google.android.gms.location.LocationRequest


// TODO MOVE TO SERVICE
class TrackActivity : EasyLocationAppCompatActivity() {
    override fun onLocationProviderDisabled() {
    }

    override fun onLocationPermissionGranted() {
    }

    override fun onLocationProviderEnabled() {
    }

    override fun onLocationPermissionDenied() {
    }

    override fun onLocationReceived(location: Location?) {
        currentLocation = location
        counter.text = "${location!!.latitude} ${location!!.longitude}"

    }

    private var mAccelerometer: Sensor? = null
    private var mSensorManager: SensorManager? = null
    private var activityTracker: ShakeDetector? = null
    private var gson = Gson()
    private var currentLocation: Location? = null
    private val events = mutableListOf<TrackedShake>()

    data class TrackedShake(val lat: Double, val lon: Double)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        activityTracker = ShakeDetector()
        activityTracker!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {

            override fun onShake(event: ShakeEvent) {
                handleShakeEvent(event)
                events.add(TrackedShake(currentLocation!!.latitude, currentLocation!!.longitude))
            }
        })

        send_button.setOnClickListener {
            sendData()
        }
        mSensorManager!!.registerListener(activityTracker, mAccelerometer, SensorManager.SENSOR_DELAY_UI)

        val locationRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(5000)
        val easyLocationRequest = EasyLocationRequestBuilder()
                .setLocationRequest(locationRequest)
                .setFallBackToLastLocationTime(3000)
                .build()
        requestLocationUpdates(easyLocationRequest)
    }

    private fun sendData() {
        val body = gson.toJson(Points(events))
        "http://a02bfe8e.ngrok.io/routes/mark_extreme_points".httpPut().body(body).response { request, response, result ->
            Toast.makeText(this, "Activity sent", Toast.LENGTH_LONG)
        }
        events.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager!!.unregisterListener(activityTracker)

    }

    private fun handleShakeEvent(event: ShakeEvent) {
        tracked.text = "${event.level}"
        tracked.setTextColor(getColor(event.level.resId))
        tracked.postDelayed({ tracked.text = "" }, 1024)
    }
    data class Points(val points: List<TrackedShake>)

}