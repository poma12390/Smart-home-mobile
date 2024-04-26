package se.ifmo.ru.smartapp.ui.pages.main

import android.app.Application
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.json.JSONObject
import se.ifmo.ru.smartapp.MainActivity
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.WeatherData
import java.net.URL



@Composable
fun MainPageContent(navController: NavController) {

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
    val weatherData = MainActivity.weatherData
    // Подставьте фактическую погоду
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
