package se.ifmo.ru.smartapp.ui.pages.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.MainActivity
import se.ifmo.ru.smartapp.R
import se.ifmo.ru.smartapp.exceptions.LoginException
import se.ifmo.ru.smartapp.network.SwitchUpdater
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.WeatherData
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage
import se.ifmo.ru.smartapp.ui.pages.room.RoomPageViewModel
import se.ifmo.ru.smartapp.ui.pages.room.RoomPageViewModelFactory


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
    val homeStateId by viewModel.homeStateId.observeAsState(initial = 0)
    val weatherOutside by viewModel.weatherOutside.observeAsState(initial = -222.0)
    val weatherInside by viewModel.weatherInside.observeAsState(initial = -222.0)
    val electricity by viewModel.electricity.observeAsState(initial = -222.0)


    LaunchedEffect(Unit) {
        coroutineScope {
            launch() {
                viewModel.fetchRooms(this, navController, application)
            }
            launch() {
                // Запуск fetchHomeState каждые 5 секунд
                while (isActive) {
                    viewModel.fetchHomeState(this, navController, application)
                    viewModel.fetchOutsideState(this, navController, application)
                    delay(5000)
                }
            }
        }
    }



    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopSection(weatherOutside, weatherInside, electricity, navController)
            HomeSection(switches, homeStateId)
            RoomsSection(rooms, navController)
        }
    }
}


@Composable
fun TopSection(
    weatherOutside: Double,
    weatherInside: Double,
    electricity: Double,
    navController: NavController
) {
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
        Column {
            Text("Погода", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Снаружи", color = Color.Gray)
            Text(
                text = if (weatherOutside < -200.0) "Загрузка..." else "${weatherOutside}°C",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Внутри", color = Color.Gray)
            Text(
                text = if (weatherInside < -200.0) "Загрузка..." else "${weatherInside}°C",
                fontSize = 18.sp
            )
        }

        Column {
            Text("Электричество", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Мощность", color = Color.Gray)
            Text(
                text = if (electricity < -200.0) "Загрузка..." else "$electricity kW",
                fontSize = 18.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                with(sharedPref.edit()) {
                    remove("auth_token")
                    apply()
                }
                moveToPage(coroutineScope, navController, "login")
            }
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFFFA726), shape = CircleShape),
                contentAlignment = Alignment.Center // Центрирование содержимого в Box
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logout), // Замените на ваш ресурс
                    contentDescription = "Account logout",
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}


@Composable
fun HomeSection(switches: List<Switch>, homeStateId: Long) {
    Log.i("drawing switches", switches.toString())

    Column(
        modifier = Modifier.fillMaxWidth() // Make sure the column takes up the full width
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Мой дом", style = MaterialTheme.typography.displayMedium)
        }
        LazyRow(
            contentPadding = PaddingValues(5.dp),
        ) {
            items(switches) { switch ->
                DeviceItem(switch, homeStateId)
            }
        }
    }
}


@Composable
fun DeviceItem(switch: Switch, homeStateId: Long) {
    var isEnabled by remember { mutableStateOf(switch.enabled) }
    val updater = PageUtils.getSwitchUpdater()
    fun toggleSwitch() {
        isEnabled = !isEnabled
        switch.stateId = PageUtils.getNewStateId()
        updater.updateSwitchState(isEnabled, switch.id, switch.stateId)
    }

    val isLocked = homeStateId < switch.stateId
    val backgroundColor = when {
        isLocked -> Color.Gray
        !isEnabled -> Color.LightGray
        switch.type == "power" -> Color(0xFFFCEA51) // Yellow for WiFi
        switch.type == "lock" -> Color(0xFFF89239) // Green for Lock
        else -> Color(0xFF76FF03) // Default enabled color
    }

    val icon = when (switch.type) {
        "power" -> Icons.Default.Wifi
        "lock" -> if (isEnabled) Icons.Default.LockOpen else Icons.Default.Lock
        else -> Icons.Default.DeviceUnknown // Use a default icon if type is unknown
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .clickable(enabled = !isLocked) {
                    toggleSwitch()
                }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = switch.name,
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            text = switch.name,
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun RoomsSection(rooms: List<Room>, navController: NavController) {
    Log.i("rooms", rooms.toString())
    val filteredRooms =
        rooms.filter { room -> room.name != "Home" && room.name != "Дом" && room.name != "Улица" }
            .filter { room -> room.name != "home" } // Filter rooms
    Column {
        Text(
            "Комнаты",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRooms) { room ->
                RoomItem(room, navController)
            }
        }
    }
}

@Composable
fun RoomItem(room: Room, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(120.dp)
            .background(color = getBackgroundColor(room.type), shape = RoundedCornerShape(20.dp))
            .clickable {
                coroutineScope.launch {
                    saveRoomToCache(context, room.id, room.name, room.type)
                    moveToPage(coroutineScope, navController, "room")
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = getIconResource(room.type)),
                contentDescription = room.name,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(room.name, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun getBackgroundColor(roomType: String): Color {
    return when (roomType) {
        "hallway" -> Color(0xFFF5F5DC)
        "living" -> Color(0xFFE0FFE0) // Light green
        "bathroom" -> Color(0xFFE0FFFF) // Light blue
        "kitchen" -> Color(0xFFFFE0B2) // Light orange
        else -> Color.LightGray
    }
}

@Composable
fun getIconResource(roomType: String): Int {
    return when (roomType) {
        "hallway" -> R.drawable.hallway
        "living" -> R.drawable.living_room
        "bathroom" -> R.drawable.bathroom
        "kitchen" -> R.drawable.kitchen
        else -> R.drawable.default_room
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


fun saveRoomToCache(context: Context, roomId: Long, name: String, roomType: String) {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putLong("cur_room", roomId)
        putString("cur_room_name", name)
        putLong(
            "cur_room_color",
            when (roomType) {
                "hallway" -> 0xFFF5F5DC
                "living" -> 0xFFE0FFE0
                "bathroom" -> 0xFFE0FFFF
                "kitchen" -> 0xFFFFE0B2
                else -> Color.LightGray.value.toLong()
            }
        )
        apply()
    }
    Log.i("saved roomId", roomId.toString())
}
