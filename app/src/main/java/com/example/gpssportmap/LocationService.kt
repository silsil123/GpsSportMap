package com.example.gpssportmap

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    // The desired intervals for location updates. Inexact. Updates may be more or less frequent.
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // last received location
    private var currentLocation: Location? = null

    private var distanceOverallDirect = 0f
    private var distanceOverallTotal = 0f
    private var locationStart: Location? = null
    private var distanceOverallTime: Long = 0L


    private var distanceCPDirect = 0f
    private var distanceCPTotal = 0f
    private var locationCP: Location? = null
    private var distanceCPTime: Long = 0L

    private var distanceWPDirect = 0f
    private var distanceWPTotal = 0f
    private var distanceWPTime: Long = 0L

    private var locationWP: Location? = null



    private var jwt: String? = null
    private var trackingSessionId: String? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)


        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        getRestToken();

        getLastLocation()

        createLocationRequest()
        requestLocationUpdates()

    }


    private fun getRestToken() {
        var handler = WebApiSingletonHandler.getInstance(applicationContext)

        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("email", C.REST_USERNAME)
        requestJsonParameters.put("password", C.REST_PASSWORD)


        var httpRequest = JsonObjectRequest(
            Request.Method.POST,
            C.REST_BASE_URL + "account/login",
            requestJsonParameters,
            { response ->
                Log.d(TAG, response.toString())
                jwt = response.getString("token")
                startRestTrackingSession()
            },
            { error ->
                Log.d(TAG, error.toString())
            }
        )

        handler.addToRequestQueue(httpRequest)

    }


    private fun startRestTrackingSession() {
        var handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("name", Date().toString())
        requestJsonParameters.put("description", Date().toString())
        requestJsonParameters.put("paceMin", 6*60)
        requestJsonParameters.put("paceMax", 18*60)


        var httpRequest = object : JsonObjectRequest(
            Request.Method.POST,
            C.REST_BASE_URL + "GpsSessions",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                trackingSessionId = response.getString("id")
            },
            Response.ErrorListener { error ->
                Log.d(TAG, error.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                for ((key, value) in super.getHeaders()) {
                    headers[key] = value
                }
                headers["Authorization"] = "Bearer " + jwt!!
                return headers
            }
        }


        handler.addToRequestQueue(httpRequest)
    }

    private fun saveRestLocation(location: Location, location_type: String) {
        if (jwt == null || trackingSessionId == null) {
            return
        }

        var handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParameters = JSONObject()

        //requestJsonParameters.put("recordedAt", dateFormat.format(Date(location.time)))
        requestJsonParameters.put("recordedAt","0001-01-01T00:00:00")

        requestJsonParameters.put("latitude", location.latitude)
        Log.d(TAG, "TESTlatitude: " + location.latitude)
        requestJsonParameters.put("longitude", location.longitude)
        Log.d(TAG, "TEST+latitude: " + location.longitude)
        requestJsonParameters.put("accuracy", location.accuracy)
        requestJsonParameters.put("altitude", location.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestJsonParameters.put("verticalAccuracy", location.verticalAccuracyMeters)
        }
        requestJsonParameters.put("gpsSessionId", trackingSessionId)
        requestJsonParameters.put("gpsLocationTypeId", location_type)


        var httpRequest = object : JsonObjectRequest(
            Request.Method.POST,
            C.REST_BASE_URL + "GpsLocations",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
            },
            Response.ErrorListener { error ->
                Log.d(TAG, error.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                for ((key, value) in super.getHeaders()) {
                    headers[key] = value
                }
                headers["Authorization"] = "Bearer " + jwt!!
                return headers
            }
        }
        handler.addToRequestQueue(httpRequest)
    }

    private fun requestLocationUpdates() {


        Log.i(TAG, "Requesting location updates")

        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(
                TAG,
                "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")
        if (location.accuracy > 100) {
            return
        }
        if (currentLocation == null) {
            locationStart = location
            locationCP = location
            locationWP = location
        } else {
            distanceOverallDirect = location.distanceTo(locationStart)
            distanceOverallTotal += location.distanceTo(currentLocation)
            distanceOverallTime += (location.time - currentLocation!!.time)

            distanceCPDirect = location.distanceTo(locationCP)
            distanceCPTotal += location.distanceTo(currentLocation)
            distanceCPTime += (location.time - currentLocation!!.time)

            distanceWPDirect = location.distanceTo(locationWP)
            distanceWPTotal += location.distanceTo(currentLocation)
            distanceWPTime += (location.time - currentLocation!!.time)
        }
        // save the location for calculations
        currentLocation = location

        showNotification()

        // save the data to mapPolyLine singleton
        Helpers.addToMapPolylineOptions(location.latitude, location.longitude)

        saveRestLocation(location, C.REST_LOCATIONID_LOC)

        // broadcast new location to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)

        intent.putExtra(C.LOCATION_UPDATE_ACTION_LAT, location.latitude)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LON, location.longitude)

        intent.putExtra(C.LOCATION_UPDATE_ACTION_OVERALL_DIRECT, distanceOverallDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_OVERALL_TOTAL, distanceOverallTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_OVERALL_TIME, distanceOverallTime)

        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_DIRECT, distanceCPDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_TOTAL, distanceCPTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_CP_TIME, distanceCPTime)

        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_DIRECT, distanceWPDirect)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_TOTAL, distanceWPTotal)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_WP_TIME, distanceWPTime)

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private fun createLocationRequest() {
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = UPDATE_INTERVAL_IN_MILLISECONDS
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.w(TAG, "task successfull");
                        if (task.result != null) {
                            onNewLocation(task.result!!)
                        }
                    } else {

                        Log.w(TAG, "Failed to get location." + task.exception)
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        // remove notifications
        NotificationManagerCompat.from(this).cancelAll()


        // don't forget to unregister brodcast receiver!!!!
        unregisterReceiver(broadcastReceiver)


        // broadcast stop to UI
        val intent = Intent(C.LOCATION_UPDATE_STOP)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        // set counters and locations to 0/null
        currentLocation = null
        locationStart = null
        locationCP = null
        locationWP = null

        distanceOverallDirect = 0f
        distanceOverallTotal = 0f
        distanceCPDirect = 0f
        distanceCPTotal = 0f
        distanceWPDirect = 0f
        distanceWPTotal = 0f


        showNotification()

        return START_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        TODO("Return the communication channel to the service.")
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)

    }

    fun showNotification() {
        val intentStartStop = Intent(C.NOTIFICATION_ACTION_START_STOP)
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP)

        val pendingIntentStartStop = PendingIntent.getBroadcast(this, 0, intentStartStop, 0)
        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        val notifyview = RemoteViews(packageName, R.layout.map_actions)

        notifyview.setOnClickPendingIntent(R.id.imageButtonStartStop, pendingIntentStartStop)
        notifyview.setOnClickPendingIntent(R.id.imageButtonCP, pendingIntentCp)
        notifyview.setOnClickPendingIntent(R.id.imageButtonWP, pendingIntentWp)


        //notifyview.setTextViewText(R.id.textViewOverallDirect, "%.1f".format(distanceOverallDirect))
        notifyview.setTextViewText(R.id.textViewOverallTotal, "%.1f".format(distanceOverallTotal))
        notifyview.setTextViewText(R.id.textViewOverallDuration, Helpers.getTimeString(distanceOverallTime))
        notifyview.setTextViewText(R.id.textViewOverallSpeed, Helpers.getPace(distanceOverallTime, distanceOverallTotal))

        //notifyview.setTextViewText(R.id.textViewWPDirect, "%.1f".format(distanceWPDirect))
        notifyview.setTextViewText(R.id.textViewWPTotal, "%.1f".format(distanceWPTotal))
        notifyview.setTextViewText(R.id.textViewWPDuration, Helpers.getTimeString(distanceWPTime))
        notifyview.setTextViewText(R.id.textViewWPSpeed, Helpers.getPace(distanceWPTime, distanceWPTotal))

        //notifyview.setTextViewText(R.id.textViewCPDirect, "%.1f".format(distanceCPDirect))
        notifyview.setTextViewText(R.id.textViewCPTotal, "%.1f".format(distanceCPTotal))
        notifyview.setTextViewText(R.id.textViewCPDuration, Helpers.getTimeString(distanceCPTime))
        notifyview.setTextViewText(R.id.textViewCPSpeed, Helpers.getPace(distanceCPTime, distanceCPTotal))


        // construct and show notification
        var builder = NotificationCompat.Builder(applicationContext, C.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_location_searching_black_18)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyview)
        Log.d(TAG, "notificationShow")

        // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
        // must be called within 5 secs after service starts.
        startForeground(C.NOTIFICATION_ID, builder.build())

    }

    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.NOTIFICATION_ACTION_WP -> {
                    locationWP = currentLocation
                    distanceWPDirect = 0f
                    distanceWPTotal = 0f
                    distanceWPTime = 0
                    saveRestLocation(locationWP!!, C.REST_LOCATIONID_WP)
                    showNotification()
                }
                C.NOTIFICATION_ACTION_CP -> {
                    Log.d(TAG, "notificationReceive")
                    locationCP = currentLocation
                    distanceCPDirect = 0f
                    distanceCPTotal = 0f
                    distanceCPTime = 0

                    //reset WP also, since we know exactly where we are on the map
                    locationWP = currentLocation
                    distanceWPDirect = 0f
                    distanceWPTotal = 0f
                    distanceWPTime = 0

                    saveRestLocation(locationCP!!, C.REST_LOCATIONID_CP)
                    showNotification()
                }
            }
        }

    }


}
