package se.ifmo.ru.smartapp.ui.pages.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.data.RangeSwitch
import se.ifmo.ru.smartapp.ui.data.Sensor
import se.ifmo.ru.smartapp.ui.data.Switch
import java.io.IOException

class RoomPageViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val _sensors = MutableLiveData<List<Sensor>>(emptyList())
    val sensors: LiveData<List<Sensor>> = _sensors
    private val _switches = MutableLiveData<List<Switch>>(emptyList())
    val switches: LiveData<List<Switch>> = _switches
    private val _rangeSwitches = MutableLiveData<List<RangeSwitch>>(emptyList())
    val rangeSwitches: LiveData<List<RangeSwitch>> = _rangeSwitches
    private val _roomStateId = MutableLiveData<Long>(0)
    val roomStateId: LiveData<Long> = _roomStateId
    private val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token = sharedPref.getString("auth_token", "") ?: ""


    fun fetchRoomDetails(roomId: Long) {
        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/rooms/$roomId")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки запроса
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    jsonData?.let {
                        val jsonObject = JSONObject(it)
                        val sensorsArray = jsonObject.getJSONArray("sensors")
                        setSyncRoomStateId(jsonObject.getLong("stateId"))
                        val stateId = jsonObject.getLong("stateId")
                        val sensorsList = mutableListOf<Sensor>()
                        for (i in 0 until sensorsArray.length()) {
                            sensorsArray.getJSONObject(i).apply {
                                sensorsList.add(
                                    Sensor(
                                        id = getLong("id"),
                                        name = getString("name"),
                                        type = getString("type"),
                                        value = getDouble("value").toLong(),
                                        stateId = stateId,
                                        roomId = roomId
                                    )
                                )
                            }
                        }
                        _sensors.postValue(sensorsList)

                        val switchesArray = jsonObject.getJSONArray("switches")
                        val switchesList = mutableListOf<Switch>()
                        for (i in 0 until switchesArray.length()) {
                            switchesArray.getJSONObject(i).apply {
                                switchesList.add(
                                    Switch(
                                        id = getLong("id"),
                                        name = getString("name"),
                                        type = getString("type"),
                                        enabled = getBoolean("enabled"),
                                        stateId = stateId,
                                        roomId = roomId
                                    )
                                )
                            }
                        }
                        _switches.postValue(switchesList)

                        val rangeSwitchesArray = jsonObject.getJSONArray("rangeSwitches")
                        val rangeSwitchesList = mutableListOf<RangeSwitch>()
                        for (i in 0 until rangeSwitchesArray.length()) {
                            rangeSwitchesArray.getJSONObject(i).apply {
                                rangeSwitchesList.add(
                                    RangeSwitch(
                                        id = getLong("id"),
                                        name = getString("name"),
                                        type = getString("type"),
                                        enabled = getBoolean("enabled"),
                                        value = getDouble("value").toLong(),
                                        stateId = stateId,
                                        roomId = roomId,
                                        minValue = 0, //TODO fix
                                        maxValue = 100
                                    )
                                )
                            }
                        }
                        _rangeSwitches.postValue(rangeSwitchesList)
                    }
                }

            }
        })
    }

    fun setSyncRoomStateId(stateId: Long) {
        if (_roomStateId.value == null || stateId == _roomStateId.value) return
        synchronized(_roomStateId) {
            val prev = _roomStateId.value
            Log.i("setting room state id to", stateId.toString())
            _roomStateId.postValue(stateId)
            while (_roomStateId.value == prev) {

            }
            Log.i("set roomStateId to", _roomStateId.value.toString())

        }
    }
}