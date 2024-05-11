package se.ifmo.ru.smartapp.ui.pages.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.ui.data.RangeSwitch
import se.ifmo.ru.smartapp.ui.data.Sensor
import se.ifmo.ru.smartapp.ui.data.Switch

@Composable
fun RoomPageContent(navController: NavController) {
    val application = LocalContext.current.applicationContext as Application
    // Получение Application контекста
    // Создание фабрики для ViewModel
    val factory = RoomPageViewModelFactory(application)
    // Получение ViewModel
    val viewModel: RoomPageViewModel = viewModel(factory = factory)
    val switches by viewModel.switches.observeAsState(initial = emptyList())
    val sensors by viewModel.sensors.observeAsState(initial = emptyList())
    val rangeSwitches by viewModel.rangeSwitches.observeAsState(initial = emptyList())
    val roomStateId by viewModel.roomStateId.observeAsState(initial = 0)
    val context = LocalContext.current
    val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val roomId = sharedPref.getLong("cur_room", 1)
    Log.i("opening room", roomId.toString())

    LaunchedEffect(Unit) {

        coroutineScope {
            launch() {
                viewModel.fetchRoomDetails(roomId)
            }
        }
    }

    RoomControlPanel(switches, sensors, rangeSwitches)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomControlPanel(
    switches: List<Switch>,
    sensors: List<Sensor>,
    rangeSwitches: List<RangeSwitch>
) {
    Column {
        TopAppBar(
            title = { Text("Living Room") },
            navigationIcon = {
                IconButton(onClick = { /* navigate back logic */ }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.Black,
            ),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(switches.size) { index ->
                DeviceSwitchCard(switches[index])
            }
            items(sensors.size) { index ->
                DeviceSensorCard(sensors[index])
            }
            items(rangeSwitches.size) { index ->
                DeviceRangeSwitchCard(rangeSwitches[index])
            }
        }
    }
}

@Composable
fun deviceCardBackground() = Modifier
    .background(
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
        )
    )
    .padding(2.dp)
    .background(Color(0xFFFAFAD2)) // Beige background

@Composable
fun DeviceSwitchCard(switch: Switch) {
    Card(
        modifier = deviceCardBackground()
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.Done, contentDescription = "Switch")
            Text(text = switch.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            androidx.compose.material3.Switch(
                checked = switch.enabled,
                onCheckedChange = { /* logic here */ }
            )
        }
    }
}

@Composable
fun DeviceSensorCard(sensor: Sensor) {
    Card(
        modifier = deviceCardBackground()
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.ExitToApp, contentDescription = "Sensor")
            Text(text = sensor.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "${sensor.value}", fontSize = 16.sp)
        }
    }
}

@Composable
fun DeviceRangeSwitchCard(rangeSwitch: RangeSwitch) {
    Card(
        modifier = deviceCardBackground()
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Range")
            Text(text = rangeSwitch.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = rangeSwitch.value.toFloat(),
                onValueChange = { newValue ->
                    // update logic here
                },
                valueRange = rangeSwitch.minValue.toFloat()..rangeSwitch.maxValue.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
