package se.ifmo.ru.smartapp.ui.pages.main

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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.network.responseParser.ResponseParser.Companion.parseSwitches
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.getRoomStateIdById
import se.ifmo.ru.smartapp.ui.data.roomExist
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import java.io.IOException

class MainPageViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val _rooms = MutableLiveData<List<Room>>(emptyList())
    val rooms: LiveData<List<Room>> = _rooms
    private val _switches = MutableLiveData<List<Switch>>(emptyList())
    val switches: LiveData<List<Switch>> = _switches
    private val _homeStateId = MutableLiveData<Long>(0)
    val homeStateId: LiveData<Long> = _homeStateId

    // Загрузка токена из кеша
    private val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token = sharedPref.getString("auth_token", "") ?: ""


    fun addSyncRooms(rooms: List<Room>) {
        val filteredRooms = rooms.filter { !roomExist(it.id, _rooms) }
        if (filteredRooms.isEmpty()) return
        synchronized(_rooms) {
            val size = _rooms.value?.size
            filteredRooms.forEach { room ->
                Log.i("adding rooms", room.name)
            }
            val currentRooms = _rooms.value ?: emptyList()
            val updatedRooms = currentRooms + filteredRooms
            _rooms.postValue(updatedRooms)

            while (_rooms.value!!.size == size) {

            }
            Log.i("adding ${_rooms.value!!.size - size!!}rooms complete", _rooms.value!!.toString())
        }
    }

    fun setSyncHomeStateId(stateId: Long) {
        if (_homeStateId.value == null || stateId == _homeStateId.value) return
        synchronized(_homeStateId) {
            val prev = _homeStateId.value
            Log.i("setting home state id to", stateId.toString())
            _homeStateId.postValue(stateId)
            while (_homeStateId.value == prev) {

            }
            Log.i("set homeStateId to", _homeStateId.value.toString())

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
    fun fetchRooms(
        coroutineScope: CoroutineScope,
        navController: NavController,
        application: Application
    ) {
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
                    Log.e("open main page fail", "ex: ")
                    val sharedPref =
                        application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        remove("auth_token")
                        apply()
                    }
                    PageUtils.moveToPage(coroutineScope, navController, "login")
                }
            }
        })
    }

    fun fetchHomeState(
        coroutineScope: CoroutineScope,
        navController: NavController,
        application: Application
    ) {
        val request = Request.Builder()
            .url("http://51.250.103.29:8080/api/rooms/home")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("request", "ex: ")
                val sharedPref =
                    application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove("auth_token")
                    apply()
                }
                PageUtils.moveToPage(coroutineScope, navController, "login")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonObject = JSONObject(responseBody)
                            if (!roomExist(
                                    jsonObject.getLong("id"),
                                    _rooms
                                )
                            )
                                addSyncRooms(
                                    listOf(
                                        Room(
                                            jsonObject.getLong("id"),
                                            jsonObject.getString("name"),
                                            jsonObject.getString("type"),
                                            jsonObject.getLong("stateId")
                                        )
                                    )
                                )
                            if (jsonObject.getLong("stateId") > PageUtils.getStateId()) {
                                PageUtils.setStateId(jsonObject.getLong("stateId") + 1)
                            }

                            Log.i(
                                "${jsonObject.getString("name")} stateId ",
                                jsonObject.getLong("stateId").toString()
                            )
                            setSyncHomeStateId(jsonObject.getLong("stateId"))
                            val switchesList = parseSwitches(
                                jsonObject.getLong("id"),
                                jsonObject.getJSONArray("switches"),
                                _rooms,
                                _switches
                            )
                            Log.i("active switches on page", switchesList.toString())
                            _switches.postValue(switchesList)
                        } catch (e: JSONException) {
                            // Обработка ошибки парсинга JSON
                        }
                    }
                } else if (response.code == 504) {
                    // Не удалось получить данные с сервера
                } else {
                    Log.e("open home room fail", "ex: ")
                    val sharedPref =
                        application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        remove("auth_token")
                        apply()
                    }
                    PageUtils.moveToPage(coroutineScope, navController, "login")
                }
            }
        })
    }


}