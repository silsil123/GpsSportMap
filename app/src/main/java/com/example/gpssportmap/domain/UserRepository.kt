package com.example.gpssportmap.domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.gpssportmap.DbHelper

class UserRepository(val context: Context) {

    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): UserRepository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun add(user: User){
        val contentValues = ContentValues()
        contentValues.put(DbHelper.USER_SPORTMAP_ID, user.sportMapId)
        contentValues.put(DbHelper.USER_FIRSTNAME, user.firstName)
        contentValues.put(DbHelper.USER_LASTNAME, user.lastName)
        contentValues.put(DbHelper.USER_USERNAME, user.userName)
        db.insert(DbHelper.USERS_TABLE_NAME, null, contentValues)
    }

    fun getAll(): List<User>{
        val users = ArrayList<User>()

        val columns = arrayOf(DbHelper.USER_ID, DbHelper.USER_SPORTMAP_ID, DbHelper.USER_FIRSTNAME,
            DbHelper.USER_LASTNAME, DbHelper.USER_USERNAME)

        val cursor = db.query(DbHelper.USERS_TABLE_NAME, columns, null, null, null, null, null)

        while (cursor.moveToNext()){
            users.add(
                User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
                )
            )
        }
        cursor.close()

        return users
    }

}