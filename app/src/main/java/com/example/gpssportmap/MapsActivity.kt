package com.example.gpssportmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.map_actions.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, SensorEventListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()
    private var locationServiceActive = false

    private lateinit var locationManager: LocationManager;
    private var provider: String? = ""
    private var location: Location? = null
    //private val polylineOptions = PolylineOptions().width(10f)
    //    .color(Color.RED)
    private var polyLine: Polyline? = null;
    private var wpPoint = false
    private var cpPoint = false

    //Toggle center
    private var centerToggle: Boolean = true
    //Toggle north up
    private var northUp: Boolean = false

    //Compass
    private var showCompass: Boolean = false
    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometerSensor : Sensor
    private lateinit var magnetometerSensor : Sensor
    private lateinit var compass : ImageView
    private var lastAccelerometer: FloatArray = FloatArray(3)
    private var lastMagnetometer: FloatArray = FloatArray(3)
    private var rotationMatrix: FloatArray = FloatArray(9)
    private var orientation: FloatArray = FloatArray(3)

    private var isLastAccelerometerArrayCopied: Boolean = false
    private var isLastMagnetometerArrayCopied: Boolean = false

    private var lastUpdatedTime: Long = 0
    private var currentDegree: Float = 0f



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MapsActivity", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (savedInstanceState != null) {
            centerToggle = savedInstanceState.getBoolean("centerToggle")
            locationServiceActive = savedInstanceState.getBoolean("locationServiceActive")
            northUp = savedInstanceState.getBoolean("northUp")
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        provider = locationManager.getBestProvider(criteria, true)

        Log.d("Provider", (provider ?: "null"))

        if (provider != null) {
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            location = locationManager.getLastKnownLocation(provider!!);
        } else {
            //Ask for permission.
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        createNotificationChannel()

        if (!checkPermissions()) {
            requestPermissions()
        }

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)

        imageButtonStartStop.setOnClickListener {
            Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
            if (locationServiceActive) {
                stopService(Intent(this, LocationService::class.java))
                mMap.clear()
                imageButtonStartStop.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.baseline_play_arrow_black_24)
                )
            } else {
                Helpers.clearMapPolylineOptions()
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(Intent(this, LocationService::class.java))
                } else {
                    startService(Intent(this, LocationService::class.java))
                }
                imageButtonStartStop.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.baseline_stop_black_24)
                )
            }

            locationServiceActive = !locationServiceActive
        }

        imageButtonCP.setOnClickListener {
            Log.d(TAG, "buttonCP")
            sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP))
            cpPoint = true

        }
        imageButtonWP.setOnClickListener {
            Log.d(TAG, "buttonWP")
            sendBroadcast(Intent(C.NOTIFICATION_ACTION_WP))
            wpPoint = true
        }

        compass = findViewById(R.id.imageViewCompass)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        buttonCompass.setOnClickListener {
            Log.d(TAG, "buttonCompass")
            showCompass = !showCompass
        }

        buttonCenter.setOnClickListener {
            Log.d(TAG, "buttonCenter")
            centerToggle = !centerToggle
        }

        buttonNorthUp.setOnClickListener {
            Log.d(TAG, "buttonNorthUp")
            northUp = !northUp
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isCompassEnabled = false

        var markerLatLng = LatLng(0.0, 0.0) // tallinn

        if (location != null) {
            markerLatLng = LatLng(location!!.latitude, location!!.longitude)
        }

        var wpMarkers = Helpers.getWpMarkerToList()
        var cpMarkers = Helpers.getCpMarkerToList()
        if (!wpMarkers.isEmpty()) {
            wpMarkers.forEach {
                mMap.addMarker(it)
            }
        }
        if (!cpMarkers.isEmpty()) {
            cpMarkers.forEach {
                mMap.addMarker(it)
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 17f))
    }

    fun bearingNorth() {
        val bearingNorth = CameraPosition(mMap.cameraPosition.target,
            mMap.cameraPosition.zoom, mMap.cameraPosition.tilt, 0F)
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(bearingNorth))
    }

    override fun onResume() {
        super.onResume()
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager.requestLocationUpdates(provider!!, 400, 1f, this)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        //Compass
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager.removeUpdates(this)
        }

        //Compass
        sensorManager.unregisterListener(this, accelerometerSensor)
        sensorManager.unregisterListener(this, magnetometerSensor)
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("centerToggle", centerToggle)
        outState.putBoolean("locationServiceActive", locationServiceActive)
        outState.putBoolean("northUp", northUp)
        Log.d(TAG, "onSaveInstance")
    }

    override fun onLocationChanged(location: Location) {
        if (!locationServiceActive) {
            val center = LatLng(location.latitude, location.longitude)

            if (marker != null) {
                marker!!.remove()
            }

            marker = mMap
                .addMarker(
                    MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker))
                )
            Log.d(TAG, "MARKERnotrackadd")

            if (centerToggle) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(center))
            }
            if (northUp) {
                bearingNorth()
            }
        }
    }

    private fun updateMap(lat: Double, lon: Double) {
        // mMap.clear()
        val center = LatLng(lat, lon)

        if (marker != null) {
            marker!!.remove()
        }

        if (polyLine != null) {
            polyLine!!.remove()
        }

        if (cpPoint) {
            marker = mMap
                .addMarker(
                    MarkerOptions()
                        .position(center)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.cpmarker))
                )
            cpPoint = false
            Helpers.addCpMarkerToList(MarkerOptions()
                .position(center)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cpmarker)))
        }
        if (wpPoint) {
            marker = mMap
                .addMarker(
                    MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.wpmarker))
                )
            wpPoint = false
            Helpers.addWpMarkerToList(MarkerOptions()
                .position(center)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.wpmarker)))
        }


        marker = mMap
            .addMarker(
                MarkerOptions()
                    .position(center)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker))
                     // use icon center for lat,lon
            )
        Log.d(TAG, "MARKERTRACKADD")


        polyLine = mMap.addPolyline(Helpers.getMapPolylineOptions())

        if (centerToggle) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(center))
        }
        if (northUp) {
            bearingNorth()
        }
    }

    // ============================================== PERMISSION HANDLING =============================================
    // Returns the current state of the permissions needed.
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                findViewById(R.id.mainMap),
                "Hey, i really need to access GPS!",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("OK", View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        C.REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                C.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.count() <= 0) { // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
                Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// Permission was granted.
                Log.i(TAG, "Permission was granted")
                Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
            } else { // Permission denied.
                Snackbar.make(
                    findViewById(R.id.mainMap),
                    "You denied GPS! What can I do?",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Settings", View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri: Uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
                    .show()
            }
        }


    }

    // ============================================== NOTIFICATION CHANNEL CREATION =============================================
    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                C.NOTIFICATION_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_DEFAULT
            );

            //.setShowBadge(false).setSound(null, null);

            channel.description = "Default channel"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            Log.d(TAG, "broadcast receiver tag")
            when (intent!!.action) {
                C.LOCATION_UPDATE_ACTION -> {
//                    textViewOverallDirect.text =
//                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_OVERALL_DIRECT, 0.0f).toInt()
//                            .toString()
                    textViewOverallTotal.text =
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_OVERALL_TOTAL, 0.0f).toInt()
                            .toString() + " m"

                    var duration = intent.getLongExtra(C.LOCATION_UPDATE_ACTION_OVERALL_TIME, 0)
                    textViewOverallDuration.text = Helpers.getTimeString(duration)
                    textViewOverallSpeed.text = Helpers.getPace(
                        duration,
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_OVERALL_TOTAL, 0.0f)
                    ) + " km/h"

//                    textViewCPDirect.text =
//                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_DIRECT, 0.0f).toInt()
//                            .toString()
                    textViewCPTotal.text =
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_TOTAL, 0.0f).toInt()
                            .toString() + " m"

                    duration = intent.getLongExtra(C.LOCATION_UPDATE_ACTION_CP_TIME, 0)
                    textViewCPDuration.text = Helpers.getTimeString(duration)
                    textViewCPSpeed.text = Helpers.getPace(
                        duration,
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_CP_TOTAL, 0.0f)
                    ) + " km/h"

//                    textViewWPDirect.text =
//                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, 0.0f).toInt()
//                            .toString()
                    textViewWPTotal.text =
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_TOTAL, 0.0f).toInt()
                            .toString() + " m"

                    duration = intent.getLongExtra(C.LOCATION_UPDATE_ACTION_WP_TIME, 0)
                    textViewWPDuration.text = Helpers.getTimeString(duration)
                    textViewWPSpeed.text = Helpers.getPace(
                        duration,
                        intent.getFloatExtra(C.LOCATION_UPDATE_ACTION_WP_TOTAL, 0.0f)
                    ) + " km/h"

                    updateMap(
                        intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LAT, 0.0),
                        intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LON, 0.0)
                    )
                }
                C.LOCATION_UPDATE_STOP -> {
                }
            }
        }

    }

    // Compass
    override fun onSensorChanged(event: SensorEvent) {
        if (showCompass) {
            if (event.sensor == accelerometerSensor) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                isLastAccelerometerArrayCopied = true
            } else if (event.sensor == magnetometerSensor) {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                isLastMagnetometerArrayCopied = true
            }

            if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250) {
                SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    lastAccelerometer,
                    lastMagnetometer
                )
                SensorManager.getOrientation(rotationMatrix, orientation)

                var azimuthInRadians: Float = orientation[0]
                var azimuthInDegree: Float = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                var rotateAnimation = RotateAnimation(
                    currentDegree,
                    -azimuthInDegree,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotateAnimation.duration = 250
                rotateAnimation.fillAfter = true
                compass.startAnimation(rotateAnimation)



                currentDegree = -azimuthInDegree
                lastUpdatedTime = System.currentTimeMillis()
            }
        } else {
            compass.clearAnimation()
            compass.visibility = View.INVISIBLE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

