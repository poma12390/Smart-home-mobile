package se.ifmo.ru.smartapp.ui.pages.room

import android.app.Application
import android.content.Context
import android.text.Layout
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun RoomControlPanel(
    switches: List<Switch>,
    sensors: List<Sensor>,
    rangeSwitches: List<RangeSwitch>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        switches.forEach { switch ->
            DeviceSwitch(switch)
        }
        sensors.forEach { sensor ->
            DeviceSensor(sensor)
        }
        rangeSwitches.forEach { rangeSwitch ->
            DeviceRangeSwitch(rangeSwitch)
        }
    }
}

@Composable
fun DeviceSwitch(switch: Switch) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Text(
            text = switch.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
        )
        androidx.compose.material3.Switch(
            checked = switch.enabled,
            onCheckedChange = { /* Your toggle logic here */ },
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun DeviceSensor(sensor: Sensor) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Text(
            text = "${sensor.name}: ${sensor.value}",
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp
        )
    }
}

@Composable
fun DeviceRangeSwitch(rangeSwitch: RangeSwitch) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Text(
            text = rangeSwitch.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color.Black
        )
        Slider(
            value = rangeSwitch.value.toFloat(),
            onValueChange = { /* Slider logic here */ },
            valueRange = 0f..100f, // Set according to your requirements
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}