package com.example.gpssportmap.domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.gpssportmap.DbHelper

class GpsLocationTypeRepository(val context: Context) {

    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): GpsLocationTypeRepository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(locationType: GpsLocationType){
        val cv = ContentValues()
        cv.put(DbHelper.GPSLOCATIONTYPE_NAME, locationType.name)
        cv.put(DbHelper.GPSLOCATIONTYPE_DESCRIPTION, locationType.description)
        db.insert(DbHelper.GPSLOCATION_TYPE_TABLE_NAME, null, cv)
    }
}