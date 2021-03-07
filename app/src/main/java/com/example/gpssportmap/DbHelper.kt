package com.example.gpssportmap

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "gpsSportMap.db"
        const val DATABASE_VERSION = 1

        const val USERS_TABLE_NAME = "USERS"

        const val USER_ID = "_id"
        const val USER_SPORTMAP_ID = "sportmapId"
        const val USER_FIRSTNAME = "firstname"
        const val USER_LASTNAME = "lastname"
        const val USER_USERNAME = "username"

        const val SQL_USER_CREATE_TABLE =
            "create table $USERS_TABLE_NAME (" +
                    "$USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USER_SPORTMAP_ID TEXT NOT NULL," +
                    "$USER_FIRSTNAME TEXT NOT NULL," +
                    "$USER_LASTNAME TEXT NOT NULL," +
                    "$USER_USERNAME TEXT NOT NULL);"

        const val GPSSESSIONS_TABLE_NAME = "GPSSESSIONS"

        const val GPSSESSION_ID = "_id"
        const val GPSSESSION_SPORTMAP_ID = "sportmapId"
        const val GPSSESSION_NAME = "name"
        const val GPSSESSION_RECORDEDAT = "recordedAt"
        const val GPSSESSION_DURATION = "duration"
        const val GPSSESSION_SPEED = "speed"
        const val GPSSESSION_DISTANCE = "distance"
        const val GPSSESSION_CLIMB = "climb"
        const val GPSSESSION_DESCENT = "descent"
        const val GPSSESSION_PACE_MIN = "paceMin"
        const val GPSSESSION_PACE_MAX = "paceMax"
        const val GPSSESSION_TYPE = "gpsSessionType"
        const val GPSSESSION_LOCATION_COUNT = "gpsLocationCount"
        const val GPSSESSION_USER_ID = "userId"

        const val SQL_GPSSESSION_CREATE_TABLE =
            "create table $GPSSESSIONS_TABLE_NAME (" +
                    "$GPSSESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$GPSSESSION_SPORTMAP_ID TEXT NULL," +
                    "$GPSSESSION_NAME TEXT NOT NULL," +
                    "$GPSSESSION_RECORDEDAT TEXT NULL," +
                    "$GPSSESSION_DURATION TEXT NULL," +
                    "$GPSSESSION_SPEED TEXT NULL," +
                    "$GPSSESSION_DISTANCE TEXT NULL," +
                    "$GPSSESSION_CLIMB TEXT NULL," +
                    "$GPSSESSION_DESCENT TEXT NULL," +
                    "$GPSSESSION_PACE_MIN TEXT NULL," +
                    "$GPSSESSION_PACE_MAX TEXT NULL," +
                    "$GPSSESSION_TYPE TEXT NULL," +
                    "$GPSSESSION_LOCATION_COUNT TEXT NULL," +
                    "$GPSSESSION_USER_ID TEXT NOT NULL);"

        const val GPSLOCATIONS_TABLE_NAME = "GPSLOCATIONS"

        const val GPSLOCATION_ID = "_id"
        const val GPSLOCATION_SPORTMAP_ID = "sportmapId"
        const val GPSLOCATION_RECORDEDAT = "recordedAt"
        const val GPSLOCATION_LATITUDE = "latitude"
        const val GPSLOCATION_LONGITUDE = "longitude"
        const val GPSLOCATION_ACCURACY = "accuracy"
        const val GPSLOCATION_ALTITUDE = "altitude"
        const val GPSLOCATION_VERTICAL_ACCURACY = "verticalAccuracy"
        const val GPSLOCATION_USER_ID = "userId"
        const val GPSLOCATION_SESSIONID = "gpsSessionId"
        const val GPSLOCATION_TYPE_ID = "gpsLocationTypeId"

        const val SQL_GPSLOCATION_CREATE_TABLE =
            "create table $GPSLOCATIONS_TABLE_NAME (" +
                    "$GPSLOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$GPSLOCATION_SPORTMAP_ID TEXT NOT NULL," +
                    "$GPSLOCATION_RECORDEDAT TEXT NOT NULL," +
                    "$GPSLOCATION_LATITUDE TEXT NOT NULL," +
                    "$GPSLOCATION_LONGITUDE TEXT NOT NULL," +
                    "$GPSLOCATION_ACCURACY TEXT NOT NULL," +
                    "$GPSLOCATION_ALTITUDE TEXT NOT NULL," +
                    "$GPSLOCATION_VERTICAL_ACCURACY TEXT NOT NULL," +
                    "$GPSLOCATION_USER_ID TEXT NOT NULL," +
                    "$GPSLOCATION_SESSIONID TEXT NOT NULL," +
                    "$GPSLOCATION_TYPE_ID TEXT NOT NULL);"

        const val GPSLOCATION_TYPE_TABLE_NAME = "GPSLOCATION_TYPES"

        const val GPSLOCATIONTYPE_ID = "_id"
        const val GPSLOCATIONTYPE_NAME = "name"
        const val GPSLOCATIONTYPE_DESCRIPTION = "description"

        const val SQL_GPSLOCATION_TYPE_CREATE_TABLE =
            "create table $GPSLOCATION_TYPE_TABLE_NAME (" +
                    "$GPSLOCATIONTYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$GPSLOCATIONTYPE_NAME TEXT NOT NULL," +
                    "$GPSLOCATIONTYPE_DESCRIPTION TEXT NOT NULL);"

        const val SQL_DELETE_TABLE_USER = "DROP TABLE IF EXISTS $USERS_TABLE_NAME;"
        const val SQL_DELETE_TABLE_GPSSESSION = "DROP TABLE IF EXISTS $GPSSESSIONS_TABLE_NAME;"
        const val SQL_DELETE_TABLE_GPSLOCATION = "DROP TABLE IF EXISTS $GPSLOCATIONS_TABLE_NAME;"
        const val SQL_DELETE_TABLE_GPSLOCATION_TYPES = "DROP TABLE IF EXISTS $GPSLOCATION_TYPE_TABLE_NAME;"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_USER_CREATE_TABLE)
        db?.execSQL(SQL_GPSSESSION_CREATE_TABLE)
        db?.execSQL(SQL_GPSLOCATION_CREATE_TABLE)
        db?.execSQL(SQL_GPSLOCATION_TYPE_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_TABLE_USER)
        db?.execSQL(SQL_DELETE_TABLE_GPSSESSION)
        db?.execSQL(SQL_DELETE_TABLE_GPSLOCATION)
        db?.execSQL(SQL_DELETE_TABLE_GPSLOCATION_TYPES)
        onCreate(db)
    }
}