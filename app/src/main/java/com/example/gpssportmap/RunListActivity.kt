package com.example.gpssportmap

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class RunListActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private var jwt: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_list)

    }

    //Delete later after login page is created.
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
            },
            { error ->
                Log.d(TAG, error.toString())
            }
        )

        handler.addToRequestQueue(httpRequest)
    }
}