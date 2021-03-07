package com.example.gpssportmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSession.setOnClickListener {
            Log.d(TAG, "buttonSession")
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        buttonRunList.setOnClickListener {
            Log.d(TAG, "buttonSession")
            val intent = Intent(this, RunListActivity::class.java)
            startActivity(intent)
        }

    }

}