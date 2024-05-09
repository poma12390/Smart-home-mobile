package se.ifmo.ru.smartapp.ui.pages.main

import android.app.Application
import android.content.Context
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.MainActivity
import se.ifmo.ru.smartapp.exceptions.LoginException
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.WeatherData
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage


@Composable
fun MainPageContent(navController: NavController) {

    val application = LocalContext.current.applicationContext as Application
    // Получение Application контекста
    // Создание фабрики для ViewModel
    val factory = MainPageViewModelFactory(application)
    // Получение ViewModel
    val viewModel: MainPageViewModel = viewModel(factory = factory)
    val rooms by viewModel.rooms.observeAsState(initial = emptyList())
    val switches by viewModel.switches.observeAsState(initial = emptyList())
    val weatherData by MainActivity.weatherData.observeAsState()
    val homeStateId by viewModel.homeStateId.observeAsState(initial = 0)



    LaunchedEffect(Unit) {

        coroutineScope {
            launch() {
                viewModel.fetchRooms(this, navController, application)
            }
            launch() {
                // Запуск fetchHomeState каждые 5 секунд
                while (isActive) {
                    viewModel.fetchHomeState(this, navController, application)
                    delay(5000)
                }
            }
        }
    }


    // Здесь должен быть код для выполнения HTTP-запроса и обновления списка комнат

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            TopSection(weatherData, navController)
            HomeSection(switches, homeStateId, viewModel)
            RoomsSection(rooms)
        }
    }
}


@Composable
fun TopSection(weatherData: WeatherData?, navController: NavController) {
    val sharedPref = LocalContext.current.applicationContext.getSharedPreferences(
        "AppPrefs",
        Context.MODE_PRIVATE
    )
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Today's Weather")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(weatherData?.degree?.toString() ?: "not available")
        }
        Button(onClick = {
            with(sharedPref.edit()) {
                remove("auth_token")
                apply()
            }
            moveToPage(coroutineScope, navController, "login")
        }) {
            Text("Logout")
        }
    }
}


@Composable
fun HomeSection(switches: List<Switch>, homeStateId: Long, viewModel: MainPageViewModel) {
    Log.i("drawing switches", switches.toString())

    Column {
        Text("Home", style = MaterialTheme.typography.displayMedium)
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(switches) { switch ->
                DeviceItem(switch, homeStateId, viewModel)
            }
        }
    }
}

@Composable
fun RoomsSection(rooms: List<Room>) {
    Log.i("rooms", rooms.toString())
    val filteredRooms = rooms.filter { room -> room.name != "Home" }
        .filter { room -> room.name != "home" } // Фильтруем комнаты
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
            items(filteredRooms) { room ->
                RoomItem(room)
            }
        }
    }
}

@Composable
fun DeviceItem(switch: Switch, homeStateId: Long, viewModel: MainPageViewModel) {
    var isEnabled by remember { mutableStateOf(switch.enabled) }
    fun toggleSwitch() {
        isEnabled = !isEnabled
        switch.stateId = PageUtils.getNewStateId()
        viewModel.updateSwitchState(isEnabled, switch.id, switch.stateId)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp)
            .height(100.dp)
            .background(
                color = if (isEnabled) Color(0xFF76FF03) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                if (homeStateId >= switch.stateId) {
                    toggleSwitch()
                } else {
                    Log.w("blocked", "switch " + switch.name)
                }
            }
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
    Log.i("room drawing", "start drawing " + room.name)
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
