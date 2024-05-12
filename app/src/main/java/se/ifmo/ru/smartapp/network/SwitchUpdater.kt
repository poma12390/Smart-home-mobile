package se.ifmo.ru.smartapp.network

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import se.ifmo.ru.smartapp.ui.data.dto.UpdateSwitchDto
import java.io.IOException

class SwitchUpdater(
    application: Application
) {
    private val sharedPref: SharedPreferences =
        application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token: String = sharedPref.getString("auth_token", "") ?: ""
    fun updateSwitchState(newState: Boolean, switchId: Long, newStateId: Long) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val mapper = ObjectMapper()
        val updateSwitch = UpdateSwitchDto(newStateId, newState)
        val bodyString = mapper.writeValueAsString(
            updateSwitch
        )


        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/switches/$switchId")
            .patch(bodyString.toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error change switch $switchId to ", "new stateId = $newStateId")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.i("Change switch $switchId to ", "new stateId = $newStateId")
                }
            }
        })
    }

    fun updateRangeSwitchState(newValue: Double, newState: Boolean, switchId: Long, newStateId: Long) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = ("{" +
                "\"enabled\": \"$newState\"" +
                "\"stateId\": \"$newStateId\"" +
                "\"value\": \"$newValue\"" +
                "}").toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/switches/$switchId")
            .patch(body)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error change switch $switchId to ", "new stateId = $newStateId")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.i("Change switch $switchId to ", "new stateId = $newStateId")
                }
            }
        })
    }
}