package se.ifmo.ru.smartapp.ui.pages.room

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.ui.data.RangeSwitch
import se.ifmo.ru.smartapp.ui.data.Sensor
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage

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
    val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val roomId = sharedPref.getLong("cur_room", 1)
    Log.i("opening room", roomId.toString())

    LaunchedEffect(Unit) {

        coroutineScope {
            launch() {
                viewModel.fetchRoomDetails(roomId)
            }

            launch() {
                // Запуск fetchHomeState каждые 5 секунд
                while (isActive) {
                    delay(5000)
                    viewModel.fetchRoomDetails(roomId)
                }
            }
        }
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        RoomControlPanel(switches, sensors, rangeSwitches, navController, roomStateId)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomControlPanel(
    switches: List<Switch>,
    sensors: List<Sensor>,
    rangeSwitches: List<RangeSwitch>,
    navController: NavController,
    roomStateId: Long,
) {
    val scope = rememberCoroutineScope()
    Column {
        TopAppBar(
            title = { Text("Living Room") },
            navigationIcon = {
                IconButton(onClick = { moveToPage(scope, navController, "main") }) {
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
                DeviceSwitchCard(switches[index], roomStateId)
            }
            items(sensors.size) { index ->
                DeviceSensorCard(sensors[index], navController)
            }
            items(rangeSwitches.size) { index ->
                DeviceRangeSwitchCard(rangeSwitches[index], roomStateId)
            }
        }
    }
}

@Composable
fun deviceCardBackground() = Modifier
    .padding(2.dp)
    .background(Color.White, RoundedCornerShape(10)) // White background

@Composable
fun DeviceSwitchCard(switch: Switch, roomStateId: Long) {
    var isEnabled by remember { mutableStateOf(switch.enabled) }
    val updater = PageUtils.getSwitchUpdater()

    // Функция для переключения состояния с проверкой допустимости переключения
    fun toggleSwitch() {
        if (roomStateId >= switch.stateId) {
            isEnabled = !isEnabled
            switch.stateId = PageUtils.getNewStateId()
            updater.updateSwitchState(isEnabled, switch.id, switch.stateId)
        } else {
            Log.w(
                "Action blocked",
                "Switch ${switch.name} cannot be toggled due to state restrictions"
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = switch.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.Switch(
                checked = isEnabled,
                onCheckedChange = { toggleSwitch() },
                thumbContent = {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(50))
                            .size(20.dp)
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Green,  // Зеленый для включенного состояния
                    uncheckedThumbColor = Color.Gray  // Серый для выключенного состояния
                )
            )
        }
    }
}

@Composable
fun DeviceRangeSwitchCard(rangeSwitch: RangeSwitch, roomStateId: Long) {
    var isEnabled by remember { mutableStateOf(rangeSwitch.enabled) }
    var sliderValue by remember { mutableDoubleStateOf(rangeSwitch.value) }
    val updater = PageUtils.getSwitchUpdater()

    fun toggleSlider(value: Double) {
        if (roomStateId >= rangeSwitch.stateId) {
            Log.i("room state $roomStateId", "switch state ${rangeSwitch.stateId}")
            rangeSwitch.stateId = PageUtils.getNewStateId()
            updater.updateRangeSwitchState(value, isEnabled, rangeSwitch.id, rangeSwitch.stateId)
        } else {
            Log.w(
                "Action blocked",
                "Switch ${rangeSwitch.name} cannot be toggled due to state restrictions"
            )
        }
    }

    fun toggleSwitch() {
        if (roomStateId >= rangeSwitch.stateId) {
            isEnabled = !isEnabled
            rangeSwitch.stateId = PageUtils.getNewStateId()
            updater.updateRangeSwitchState(
                rangeSwitch.value,
                isEnabled,
                rangeSwitch.id,
                rangeSwitch.stateId
            )
        } else {
            Log.w(
                "Action blocked",
                "Switch ${rangeSwitch.name} cannot be toggled due to state restrictions"
            )
        }
    }

    Card(
        modifier = deviceCardBackground()
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Build, contentDescription = "Device Icon")
                androidx.compose.material3.Switch(
                    checked =  isEnabled,
                    onCheckedChange = {
                        if(roomStateId >= rangeSwitch.stateId){
                            toggleSwitch()
                        }
                    },
                    thumbContent = {
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(50))
                                .size(20.dp)
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(text = rangeSwitch.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Текущее значение: ${"%.1f".format(sliderValue)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = { newValue ->
                        if (roomStateId >= rangeSwitch.stateId) sliderValue = newValue.toDouble()
                    },
                    onValueChangeFinished = {
                        if (roomStateId >= rangeSwitch.stateId) {
                            toggleSlider(sliderValue)
                        }
                    },
                    valueRange = rangeSwitch.minValue.toFloat()..rangeSwitch.maxValue.toFloat(),
                    steps = ((rangeSwitch.maxValue - rangeSwitch.minValue) / 0.1).toInt() - 1,
                    enabled = isEnabled && roomStateId >= rangeSwitch.stateId,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        }
    }
}


@Composable
fun DeviceSensorCard(sensor: Sensor, navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = deviceCardBackground()
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                saveSensorIdToCache(context, sensor.id, sensor.name)
                moveToPage(scope, navController, "sensor")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = sensor.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "${sensor.value}°C", fontSize = 16.sp)
        }
    }
}

fun saveSensorIdToCache(context: Context, sensorId: Long, sensorName: String) {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putLong("cur_sensor", sensorId)
        putString("cur_sensor_name", sensorName)

        apply()
    }
    Log.i("saved sensorId", sensorId.toString())
}