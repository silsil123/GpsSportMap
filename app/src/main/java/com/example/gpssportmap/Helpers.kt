package com.example.gpssportmap

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.concurrent.TimeUnit

class Helpers {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        private var mapPolylineOptions: PolylineOptions? = null
        private var wpMarkers: MutableList<MarkerOptions> = ArrayList()
        private var cpMarkers: MutableList<MarkerOptions> = ArrayList()

        @Synchronized
        fun getMapPolylineOptions(): PolylineOptions {
            if (mapPolylineOptions == null) {
                mapPolylineOptions = PolylineOptions()
            }
            return mapPolylineOptions!!;
        }

        fun clearMapPolylineOptions(){
            mapPolylineOptions = PolylineOptions()
        }

        fun addToMapPolylineOptions(lat: Double, lon: Double){
            getMapPolylineOptions().add(LatLng(lat, lon))
        }

        fun addCpMarkerToList(cpmarker: MarkerOptions) {
            cpMarkers.add(cpmarker)
        }

        fun addWpMarkerToList(wpmarker: MarkerOptions) {
            wpMarkers.add(wpmarker)
        }

        fun getWpMarkerToList(): MutableList<MarkerOptions> {
            return wpMarkers
        }

        fun getCpMarkerToList(): MutableList<MarkerOptions> {
            return cpMarkers
        }

        fun getTimeString(millis: Long): String {
            return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(millis)
                ),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(millis)
                )
            )
        }


        fun getPace(millis: Long, distance: Float): String {
            Log.d(TAG, millis.toString() + '-' + distance.toString())
            val speed = millis / 60.0 / distance
            if (speed > 99) return "--:--"
            val minutes = (speed ).toInt();
            val seconds = ((speed - minutes) * 60).toInt()

            return minutes.toString() + ":" + (if (seconds < 10)  "0" else "") +seconds.toString();

        }

    }
}