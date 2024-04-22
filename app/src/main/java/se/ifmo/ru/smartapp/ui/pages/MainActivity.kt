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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import okhttp3.Call
import androidx.compose.runtime.livedata.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.R
import se.ifmo.ru.smartapp.ui.data.Device
import se.ifmo.ru.smartapp.ui.data.Room
import java.io.IOException


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

    LaunchedEffect(Unit) {
        viewModel.fetchRooms()
    }
    // Здесь должен быть код для выполнения HTTP-запроса и обновления списка комнат

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            TopSection()
            HomeSection()
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
fun HomeSection() {
    // Реализуйте логику включения/выключения устройств по необходимости
    val devices = listOf(
        Device("Front Door", "Open", Icons.Default.Home),
        Device("Wifi", "On", Icons.Default.CheckCircle),
        Device("Lights", "Off", Icons.Default.Star)
    )
    Column {
        Text(
            "Home",
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                DeviceItem(device)
            }
            item {
                // Кнопка для добавления нового устройства
                DeviceItem(Device("Add", "", Icons.Default.Add), isAddButton = true)
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
fun DeviceItem(device: Device, isAddButton: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp)
            .height(100.dp)
            .background(
                color = if (isAddButton) Color.LightGray else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { /* обработчик нажатия */ }
    ) {
        Icon(
            imageVector = device.icon,
            contentDescription = device.name,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = device.name,
            textAlign = TextAlign.Center
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
