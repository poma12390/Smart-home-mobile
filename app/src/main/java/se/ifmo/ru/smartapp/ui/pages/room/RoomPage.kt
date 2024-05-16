package se.ifmo.ru.smartapp.ui.pages.room

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.R
import se.ifmo.ru.smartapp.ui.data.RangeSwitch
import se.ifmo.ru.smartapp.ui.data.Sensor
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.pages.PageNames.SENSOR_PAGE
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
    val roomColor = sharedPref.getLong("cur_room_color", Color.LightGray.value.toLong())
    val roomName = sharedPref.getString("cur_room_name", "подвал")
    Log.i("opening room", roomId.toString())

    LaunchedEffect(Unit) {

        coroutineScope {
            launch {
                viewModel.fetchRoomDetails(roomId)
            }

            launch{
                // Запуск fetchHomeState каждые 5 секунд
                while (isActive) {
                    delay(5000)
                    viewModel.fetchRoomDetails(roomId)
                }
            }
        }
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        RoomControlPanel(switches, sensors, rangeSwitches, navController, roomStateId, roomName!! , roomColor)
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
    roomName: String,
    roomColor: Long
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        androidx.compose.material.Text(
                            text = roomName,
                            textAlign = TextAlign.Center,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .weight(6f)
                                .offset(x = (-10).dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { moveToPage(scope, navController, "main") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(roomColor),
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
fun DeviceSwitchCard(switch: Switch, roomStateId: Long) {
    var isEnabled by remember { mutableStateOf(switch.enabled) }
    val updater = PageUtils.getSwitchUpdater()

    // Function to toggle switch state with validity check
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

    // Determine the background color based on the switch type and state
    val backgroundColor = if (roomStateId < switch.stateId) {
        Color.Gray  // Gray background for blocked state
    } else {
        when {
            isEnabled && switch.type == "light" -> Color(0xFFFFA726) // Orange for light
            isEnabled && switch.type == "power" -> Color(0xFF42A5F5) // Blue for power
            isEnabled && switch.type == "lock" -> Color(0xFF8BC34A)  // Lime for lock
            else -> Color(0xffe7e0ec)             // Default color for disabled state
        }
    }

    val iconResource = if (isEnabled) {
        getIconResource(switch.type)
    } else {
        getIconResourceBW(switch.type)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (roomStateId < switch.stateId) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                androidx.compose.material3.Switch(
                    checked = isEnabled,
                    onCheckedChange = { if (roomStateId >= switch.stateId) toggleSwitch() },
                    thumbContent = {
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(50))
                                .size(20.dp)
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,  // Green for enabled state
                        uncheckedThumbColor = Color.Gray  // Gray for disabled state
                    )
                )
            }

            Text(
                text = switch.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
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

    val label = when (rangeSwitch.type) {
        "climat" -> "Температура"
        "light" -> "Яркость"
        else -> "Параметр"
    }

    val backgroundColor = when {
        roomStateId < rangeSwitch.stateId -> Color.Gray
        rangeSwitch.type == "climat" && isEnabled -> {
            val range = rangeSwitch.maxValue - rangeSwitch.minValue
            when {
                sliderValue <= rangeSwitch.minValue + range / 3 -> Color(0xFF48C2FA) // Light Blue
                sliderValue <= rangeSwitch.minValue + 2 * range / 3 -> Color(0xFFFFC166) // Light Orange
                else -> Color(0xFFFC4E4E) // Light Red
            }
        }
        rangeSwitch.type == "light" && isEnabled -> {
            Color(0xFFF3B435)
        }
        else -> Color(0xffe7e0ec)
    }

    val iconResource = if (!isEnabled || roomStateId < rangeSwitch.stateId) {
        getIconResourceBW(rangeSwitch.type)
    } else {
        getIconResource(rangeSwitch.type)
    }

    val switchThumbColor = if (roomStateId < rangeSwitch.stateId) Color.Gray else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (roomStateId < rangeSwitch.stateId) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = rangeSwitch.name,
                        modifier = Modifier.size(40.dp)
                    )
                }
                androidx.compose.material3.Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        if (roomStateId >= rangeSwitch.stateId) {
                            toggleSwitch()
                        }
                    },
                    thumbContent = {
                        Box(
                            modifier = Modifier
                                .background(switchThumbColor, RoundedCornerShape(50))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(text = rangeSwitch.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "${"%.0f".format(sliderValue)}°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = sliderValue.toFloat(),
                onValueChange = { newValue ->
                    if (roomStateId >= rangeSwitch.stateId) {
                        sliderValue = newValue.toDouble()
                    } else {
                        Log.w(
                            "Action blocked",
                            "Slider for ${rangeSwitch.name} cannot be changed due to state restrictions"
                        )
                    }
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
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun getIconResource(type: String): Int {
    return when (type) {
        "light" -> R.drawable.lamp
        "climat" -> R.drawable.cooling
        "lock" -> R.drawable.lock
        "power" -> R.drawable.power
        "temperature" -> R.drawable.temperature
        "humidity" -> R.drawable.humidity
        else -> R.drawable.default_room
    }
}

@Composable
fun getIconResourceBW(type: String): Int {
    return when (type) {
        "light" -> R.drawable.lamp_bw
        "climat" -> R.drawable.cooling_bw
        "power" -> R.drawable.power_bw
        "lock" -> R.drawable.lock_bw
        else -> R.drawable.default_room
    }
}


@Composable
fun DeviceSensorCard(sensor: Sensor, navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Определение единицы измерения и цвета фона на основе типа датчика
    val (unit, backgroundColor) = when (sensor.type) {
        "temperature" -> "°C" to Color(0xFFFFF3E0) // Светло-оранжевый
        "humidity" -> "%" to Color(0xFFE0F7FA) // Светло-голубой
        "light" -> "%" to Color(0xFFFFF9C4) // Светло-желтый
        else -> "" to Color(0xFFE0E0E0) // Светло-серый
    }

    val iconResource = getIconResource(sensor.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                saveSensorIdToCache(
                    context,
                    sensor.id,
                    sensor.name,
                    sensor.value.toFloat(),
                    sensor.type
                )
                moveToPage(scope, navController, SENSOR_PAGE.pageName)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Отображение большого значка датчика
                Image(
                    painter = painterResource(id = iconResource),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = sensor.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = String.format("%.1f $unit", sensor.value),
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}



private fun saveSensorIdToCache(
    context: Context,
    sensorId: Long,
    sensorName: String,
    temperature: Float,
    type: String
) {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putLong("cur_sensor", sensorId)
        putFloat("cur_sensor_temperature", temperature)
        putString("cur_sensor_name", sensorName)
        putString("cur_sensor_type", type)

        apply()
    }
    Log.i("saved sensorId", sensorId.toString())
}