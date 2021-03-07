package com.example.gpssportmap

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.map_actions.*
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private var jwt: String? = null
    private var trackingSessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        buttonSignIn.setOnClickListener {
            Log.d(TAG, "buttonSignIn")
            C.REST_USERNAME = editTextUsername.text.toString()
            C.REST_PASSWORD = editTextPassword.text.toString()
            Log.d(TAG, "buttonSignInUser: " + C.REST_USERNAME)
            Log.d(TAG, "buttonSignInPass: " + C.REST_PASSWORD)
            getRestToken()
        }

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
                val intent = Intent(this, MainActivity::class.java)
                updateUserSessions()
                startActivity(intent)
            },
            { error ->
                Log.d(TAG, error.toString())
                var alert = AlertDialog.Builder(this)
                    .setTitle("Something went wrong!")
                    .setMessage("Your email or password was incorrect!")
                    .setNeutralButton("OK"){_,_ ->
                        Toast.makeText(applicationContext, "Try again!", Toast.LENGTH_SHORT).show()}
                alert.show()
            }
        )

        handler.addToRequestQueue(httpRequest)

    }

    private fun updateUserSessions(){
        initializeCheckSession()
        getUserId()
        Log.d(TAG, "USERID: " + C.REST_USER_ID)

    }

    private fun initializeCheckSession() {
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

    private fun getUserId() {
        var handler = WebApiSingletonHandler.getInstance(applicationContext)

        var httpRequest = object : JsonObjectRequest(
            Method.GET,
            C.REST_BASE_URL + "GpsSessions/" + trackingSessionId,
            null,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                C.REST_USER_ID = response.getString("appUserId")
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


}