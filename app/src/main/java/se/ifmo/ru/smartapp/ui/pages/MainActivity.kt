package se.ifmo.ru.smartapp.ui.pages

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.data.Room
import java.io.IOException


data class Switch(
    val id: Long,
    val name: String,
    val type: String, // Считаем, что тип ограничен заранее определенными значениями "power", "lock" и т.д.
    val enabled: Boolean
)

private fun parseSwitches(jsonArray: JSONArray): List<Switch> {
    val switches = mutableListOf<Switch>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val switch = Switch(
            id = jsonObject.getLong("id"),
            name = jsonObject.getString("name"),
            type = jsonObject.getString("type"),
            enabled = jsonObject.getBoolean("enabled")
        )
        switches.add(switch)
    }
    return switches
}


class MainPageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainPageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainPageViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val _rooms = MutableLiveData<List<Room>>(emptyList())
    val rooms: LiveData<List<Room>> = _rooms
    private val _switches = MutableLiveData<List<Switch>>(emptyList())
    val switches: LiveData<List<Switch>> = _switches

    // Загрузка токена из кеша
    private val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val token = sharedPref.getString("auth_token", "") ?: ""

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
                                    type = roomObject.getString("type")
                                )
                                roomsList.add(room)
                            }
                            _rooms.postValue(roomsList)
                        } catch (e: JSONException) {
                            // Обработка ошибки парсинга JSON
                        }
                    }
                } else {
                    // Обработка ошибки HTTP
                }
            }
        })
    }
    fun fetchHomeState() {
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
                            val switchesList = parseSwitches(jsonObject.getJSONArray("switches"))
                            _switches.postValue(switchesList)
                        } catch (e: JSONException) {
                            // Обработка ошибки парсинга JSON
                        }
                    }
                } else {
                    // Обработка ошибки HTTP
                }
            }
        })
    }
}



@Composable
fun MainPage(navController: NavController) {

    // Получение Application контекста
    val application = LocalContext.current.applicationContext as Application
    // Создание фабрики для ViewModel
    val factory = MainPageViewModelFactory(application)
    // Получение ViewModel
    val viewModel: MainPageViewModel = viewModel(factory = factory)
    val rooms by viewModel.rooms.observeAsState(initial = emptyList())
    val switches by viewModel.switches.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchRooms()
        viewModel.fetchHomeState()

    }
    // Здесь должен быть код для выполнения HTTP-запроса и обновления списка комнат

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            TopSection()
            HomeSection(switches)
            RoomsSection(rooms)
        }
    }
}

@Composable
fun TopSection() {
    // Подставьте фактическую погоду
    val weather = "28°C"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Today's Weather")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(weather)
        }
        // Подставьте фактическое изображение пользователя

    }
}

@Composable
fun HomeSection(switches: List<Switch>) {
    Column {
        Text("Home", style = MaterialTheme.typography.displayMedium)
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(switches) { switch ->
                DeviceItem(switch)
            }
            item {
                AddDeviceItem() // Кнопка для добавления нового устройства
            }
        }
    }
}

@Composable
fun RoomsSection(rooms: List<Room>) {
    Column {
        Text(
            "Rooms",
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rooms) { room ->
                RoomItem(room)
            }
            item {
                // Кнопка для добавления новой комнаты
                AddRoomItem()
            }
        }
    }
}

@Composable
fun DeviceItem(switch: Switch) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp)
            .height(100.dp)
            .background(
                color = if (switch.enabled) Color(0xFF76FF03) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { /* TODO: обработчик нажатия */ }
    ) {
        Icon(
            imageVector = if (switch.type == "power") Icons.Default.Star else Icons.Default.Lock,
            contentDescription = switch.name,
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .padding(top = 16.dp)
        )
        Text(
            text = switch.name,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun RoomItem(room: Room) {
    val icon = when (room.type) {
        "Living Room" -> Icons.Default // Иконка для гостиной
        "Dining Room" -> Icons.Default // Иконка для столовой
        else -> Icons.Default // Иконка по умолчанию
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(room.name, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AddRoomItem() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .background(color = Color.Gray, shape = RoundedCornerShape(8.dp))
            .clickable { /* обработчик нажатия */ }
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Room")
    }
}

@Composable
fun AddDeviceItem() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(8.dp)
            .size(100.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { /* handle click */ }
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Device", modifier = Modifier.size(48.dp))
    }
}
