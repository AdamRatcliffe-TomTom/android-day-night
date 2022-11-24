package com.example.daynight

import android.content.Context
import android.hardware.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private var tomTomMap: com.tomtom.sdk.map.display.TomTomMap? = null

    private var styleMode: StyleMode by Delegates.observable(
        StyleMode.MAIN
    ) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            tomTomMap?.setStyleMode(newValue)
        }
    }

    private val darkModeThreshold: Float = 50.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMap()
        initLightSensor()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            lightSensorListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }

    private fun initMap() {
        val mapOptions =
            MapOptions(mapKey = resources.getString(R.string.API_KEY), styleMode = styleMode)
        val mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { map ->
            tomTomMap = map
        }
    }

    private fun initLightSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                val lux = event.values[0]
                styleMode = if (lux < darkModeThreshold) StyleMode.DARK else StyleMode.MAIN
            }
        }

        override fun onAccuracyChanged(event: Sensor?, accuracy: Int) {}
    }
}