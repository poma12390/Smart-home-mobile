package se.ifmo.ru.smartapp.ui.pages.sensor

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import java.io.IOException

class SensorPageViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val _xValues = MutableLiveData<List<Long>>(emptyList())
    val xValues: LiveData<List<Long>> = _xValues
    private val _yValues = MutableLiveData<List<Double>>(emptyList())
    val yValues: LiveData<List<Double>> = _yValues

    // Загрузка токена из кеша
    private val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token = sharedPref.getString("auth_token", "") ?: ""

    fun fetchOutsideState(
        coroutineScope: CoroutineScope,
        navController: NavController,
        sensorId: Long
    ) {
        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/sensors/$sensorId/history")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("request", "ex: ${e.message}")
                PageUtils.moveToPage(coroutineScope, navController, "room")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { json ->
                        try {
                            val jsonObject = JSONObject(json)
                            val secondsArray = jsonObject.getJSONArray("seconds")
                            val valuesArray = jsonObject.getJSONArray("values")

                            val xValuesList = mutableListOf<Long>()
                            for (i in 0 until secondsArray.length()) {
                                xValuesList.add(secondsArray.getLong(i))
                            }

                            val yValuesList = mutableListOf<Double>()
                            for (i in 0 until valuesArray.length()) {
                                yValuesList.add(valuesArray.getDouble(i))
                            }

                            _xValues.postValue(xValuesList)
                            _yValues.postValue(yValuesList)

                        } catch (e: JSONException) {
                            Log.e("request", "JSON parsing error: ${e.message}")
                        }
                    }
                } else {
                    Log.e("request", "Response not successful: ${response.message}")
                    PageUtils.moveToPage(coroutineScope, navController, "room")
                }
            }
        })
    }
}