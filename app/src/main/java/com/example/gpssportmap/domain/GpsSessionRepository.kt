package com.example.gpssportmap.domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.gpssportmap.DbHelper

class GpsSessionRepository(val context: Context) {

    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): GpsSessionRepository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(session: GpsSession){
        val cv = ContentValues()
        cv.put(DbHelper.GPSSESSION_SPORTMAP_ID, session.sportMapId)
        cv.put(DbHelper.GPSSESSION_NAME, session.name)
        cv.put(DbHelper.GPSSESSION_RECORDEDAT, session.recordedAt)
        cv.put(DbHelper.GPSSESSION_DURATION, session.duration)
        cv.put(DbHelper.GPSSESSION_SPEED, session.speed)
        cv.put(DbHelper.GPSSESSION_DISTANCE, session.distance)
        cv.put(DbHelper.GPSSESSION_CLIMB, session.climb)
        cv.put(DbHelper.GPSSESSION_DESCENT, session.descent)
        cv.put(DbHelper.GPSSESSION_PACE_MIN, session.paceMin)
        cv.put(DbHelper.GPSSESSION_PACE_MAX, session.paceMax)
        cv.put(DbHelper.GPSSESSION_TYPE, session.type)
        cv.put(DbHelper.GPSSESSION_LOCATION_COUNT, session.locationCount)
        cv.put(DbHelper.GPSSESSION_USER_ID, session.userId)
        db.insert(DbHelper.GPSSESSIONS_TABLE_NAME, null, cv)
    }
}