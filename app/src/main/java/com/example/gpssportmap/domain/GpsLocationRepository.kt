package com.example.gpssportmap.domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.gpssportmap.DbHelper

class GpsLocationRepository(val context: Context) {

    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): GpsLocationRepository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(location: GpsLocation){
        val cv = ContentValues()
        cv.put(DbHelper.GPSLOCATION_SPORTMAP_ID, location.sportMapId)
        cv.put(DbHelper.GPSLOCATION_RECORDEDAT, location.recordedAt)
        cv.put(DbHelper.GPSLOCATION_LATITUDE, location.latitude)
        cv.put(DbHelper.GPSLOCATION_LONGITUDE, location.longitude)
        cv.put(DbHelper.GPSLOCATION_ACCURACY, location.accuracy)
        cv.put(DbHelper.GPSLOCATION_ALTITUDE, location.altitude)
        cv.put(DbHelper.GPSLOCATION_VERTICAL_ACCURACY, location.verticalAccuracy)
        cv.put(DbHelper.GPSLOCATION_USER_ID, location.userId)
        cv.put(DbHelper.GPSLOCATION_SESSIONID, location.sessionId)
        cv.put(DbHelper.GPSLOCATION_TYPE_ID, location.typeId)
        db.insert(DbHelper.GPSLOCATIONS_TABLE_NAME, null, cv)
    }
}