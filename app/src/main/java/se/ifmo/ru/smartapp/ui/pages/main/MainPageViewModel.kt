package se.ifmo.ru.smartapp.ui.pages.main

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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.exceptions.LoginException
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.getRoomStateIdById
import se.ifmo.ru.smartapp.ui.data.getSwitchStateIdById
import se.ifmo.ru.smartapp.ui.data.hasSwitchWithId
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import java.io.IOException

class MainPageViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val _rooms = MutableLiveData<List<Room>>(emptyList())
    val rooms: LiveData<List<Room>> = _rooms
    private val _switches = MutableLiveData<List<Switch>>(emptyList())
    val switches: LiveData<List<Switch>> = _switches

    // Загрузка токена из кеша
    private val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token = sharedPref.getString("auth_token", "") ?: ""


    fun addSyncRooms(rooms: List<Room>) {
        if (rooms.isEmpty()) return
        synchronized(_rooms) {
            val size = _rooms.value?.size
            rooms.forEach { room ->
                Log.i("adding rooms", room.name)
            }
            val currentRooms = _rooms.value ?: emptyList()
            val updatedRooms = currentRooms + rooms
            _rooms.postValue(updatedRooms)

            while (_rooms.value!!.size == size) {

            }
            Log.i("adding ${_rooms.value!!.size - size!!}rooms complete", _rooms.value!!.toString())
        }
    }

    fun addSyncSwitches(switches: List<Switch>) {
        if (switches.isEmpty()) return
        val size = _switches.value?.size
        synchronized(_switches) {
            switches.forEach { switch ->
                Log.i("adding rooms", switch.name)
            }
            val currentSwitches = _switches.value ?: emptyList()
            val updatedSwitches = currentSwitches + switches
            _switches.postValue(updatedSwitches)
            while (_switches.value!!.size == size) {

            }
            Log.i("adding switches complete", _switches.value!!.toString())
        }
    }

    // Функция для выполнения запроса к API для получения комнат
    fun fetchRooms() {
        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/rooms")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки запроса
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val roomsArray = jsonObject.getJSONArray("rooms")
                            val roomsList = mutableListOf<Room>()
                            for (i in 0 until roomsArray.length()) {
                                val roomObject = roomsArray.getJSONObject(i)
                                val room = Room(
                                    id = roomObject.getLong("id"),
                                    name = roomObject.getString("name"),
                                    type = roomObject.getString("type"),
                                    stateId = 0
                                )
                                roomsList.add(room)
                            }
                            addSyncRooms(roomsList)
                        } catch (e: JSONException) {
                            // Обработка ошибки парсинга JSON
                        }
                    }
                } else {
                    sharedPref.edit().remove("auth_token").apply()
                    throw LoginException()
                }
            }
        })
    }

    fun fetchHomeState(): Long {
        var homeStateId: Long = 0
        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/rooms/home/state")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки запроса
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonObject = JSONObject(responseBody)
                            addSyncRooms(
                                listOf(
                                    Room(
                                        0,
                                        "home",
                                        "home",
                                        jsonObject.getLong("stateId")
                                    )
                                )
                            )
                            homeStateId = jsonObject.getLong("stateId")
                            val switchesList = parseSwitches(0, jsonObject.getJSONArray("switches"))
                            addSyncSwitches(switchesList)
                        } catch (e: JSONException) {
                            // Обработка ошибки парсинга JSON
                        }
                    }
                } else {
                    sharedPref.edit().remove("auth_token").apply()
                    throw LoginException()
                }
            }
        })
        return homeStateId
    }

    private fun parseSwitches(roomId: Long, jsonArray: JSONArray): List<Switch> {
        val switches = mutableListOf<Switch>()
        val firstRequest = !hasSwitchWithId(roomId, _switches)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val switch = Switch(
                id = jsonObject.getLong("id"),
                name = jsonObject.getString("name"),
                type = jsonObject.getString("type"),
                enabled = jsonObject.getBoolean("enabled"),
                stateId = if (firstRequest) {
                    Log.i("roomId", roomId.toString())
                    Log.i("arr", _rooms.value!!.toString())
                    getRoomStateIdById(roomId, _rooms)!!
                } else {
                    getSwitchStateIdById(jsonObject.getLong("id"), _switches)!!
                },
                roomId = roomId
            )
            switches.add(switch)
        }
        return switches
    }
}