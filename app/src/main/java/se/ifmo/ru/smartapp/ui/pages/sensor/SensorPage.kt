package se.ifmo.ru.smartapp.ui.pages.sensor

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.R
import se.ifmo.ru.smartapp.ui.pages.PageNames.ROOM_PAGE
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorPageContent(navController: NavController) {
    val application = LocalContext.current.applicationContext as Application
    val factory = SensorPageViewModelFactory(application)
    val viewModel: SensorPageViewModel = viewModel(factory = factory)

    val xValues by viewModel.xValues.observeAsState(initial = emptyList())
    val yValues by viewModel.yValues.observeAsState(initial = emptyList())

    val lineEntries = xValues.zip(yValues).map { (x, y) -> Entry(x.toFloat(), y.toFloat()) }
    val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val nHours = xValues.size/4
    val minTemperature = yValues.minOrNull() ?: -222.0
    val maxTemperature = yValues.maxOrNull() ?: -222.0
    val currentTemperature = sharedPref.getFloat("cur_sensor_temperature", -222f)
    val sensorId: Long = sharedPref.getLong("cur_sensor", 2)
    val sensorType = sharedPref.getString("cur_sensor_type", "")

    val scope = rememberCoroutineScope()
    val switchName = sharedPref.getString("cur_sensor_name", "")

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                viewModel.fetchOutsideState(this, navController, sensorId)
            }
        }
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val temperatureInfoOffset = remember { mutableStateOf(Offset.Zero) }
    val lineChartOffset = remember { mutableStateOf(Offset.Zero) }
    val temperatureInfoSize = remember { mutableStateOf(IntSize.Zero) }
    val lineChartSize = remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = switchName!!,
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .weight(6f)
                                    .offset(x = (-10).dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        PageUtils.moveToPage(scope, navController, ROOM_PAGE.pageName)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.LightGray)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            DraggableBlock(
                offset = temperatureInfoOffset.value,
                onDrag = { offset ->
                    temperatureInfoOffset.value = offset
                    preventOverlap(
                        temperatureInfoOffset,
                        temperatureInfoSize,
                        lineChartOffset,
                        lineChartSize,
                        screenWidth,
                        screenHeight,
                        density
                    )
                },
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onGloballyPositioned = { size -> temperatureInfoSize.value = size },
                allowDrag = { _, _ -> true } // Allow dragging for TemperatureInfoBlock
            ) {
                TemperatureInfoBlock(
                    currentTemperature = currentTemperature,
                    minTemperature = minTemperature,
                    maxTemperature = maxTemperature,
                    nHours = nHours,
                    sensorType = sensorType ?: ""
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            DraggableBlock(
                offset = lineChartOffset.value,
                onDrag = { offset ->
                    lineChartOffset.value = offset
                    preventOverlap(
                        lineChartOffset,
                        lineChartSize,
                        temperatureInfoOffset,
                        temperatureInfoSize,
                        screenWidth,
                        screenHeight,
                        density
                    )
                },
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onGloballyPositioned = { size -> lineChartSize.value = size },
                allowDrag = { position, size ->
                    // Allow dragging only if the position is outside the chart's coordinate plane
                    val chartPadding = with(density) { 16.dp.toPx() }
                    val isOutsideChart = position.y < chartPadding || position.x < chartPadding || position.x > size.width - chartPadding
                    isOutsideChart
                }
            ) {
                LineChartContainer(entries = lineEntries)
            }
        }
    }
}


@Composable
fun TemperatureInfoBlock(
    currentTemperature: Float,
    minTemperature: Double,
    maxTemperature: Double,
    nHours: Int,
    sensorType: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        when (sensorType) {
            "temperature" -> {
                Text(
                    text = if (currentTemperature < -200.0) "Загрузка..." else String.format("Текущая температура: %.1f°C", currentTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (minTemperature < -200.0) "Загрузка..." else String.format("Минимальная температура (за последние %d часов): %.1f°C", nHours, minTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (maxTemperature < -200.0) "Загрузка..." else String.format("Максимальная температура (за последние %d часов): %.1f°C", nHours, maxTemperature),
                    fontSize = 18.sp
                )
            }
            "humidity" -> {
                Text(
                    text = if (currentTemperature < -200.0) "Загрузка..." else String.format("Текущая влажность: %.1f%%", currentTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (minTemperature < -200.0) "Загрузка..." else String.format("Минимальная влажность (за последние %d часов): %.1f%%", nHours, minTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (maxTemperature < -200.0) "Загрузка..." else String.format("Максимальная влажность (за последние %d часов): %.1f%%", nHours, maxTemperature),
                    fontSize = 18.sp
                )
            }
            "light" -> {
                Text(
                    text = if (currentTemperature < -200.0) "Загрузка..." else String.format("Текущая яркость: %.1f %", currentTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (minTemperature < -200.0) "Загрузка..." else String.format("Минимальная яркость (за последние %d часов): %.1f %", nHours, minTemperature),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (maxTemperature < -200.0) "Загрузка..." else String.format("Максимальная яркость (за последние %d часов): %.1f %", nHours, maxTemperature),
                    fontSize = 18.sp
                )
            }
            else -> {
                Text(
                    text = "Неизвестный сенсор",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun LineChartContainer(entries: List<Entry>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        LineChartComponent(entries = entries)
    }
}

@Composable
fun LineChartComponent(entries: List<Entry>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                val dataSet = LineDataSet(entries, "Sensor Data").apply {
                    color = Color.parseColor("#A020F0")
                    setCircleColor(Color.WHITE)
                    circleHoleColor = Color.parseColor("#A020F0")
                    circleRadius = 4f
                    lineWidth = 2f
                    setDrawValues(false)
                    fillAlpha = 110
                    fillColor = Color.parseColor("#A020F0")
                    setDrawFilled(true)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                data = LineData(dataSet)
                description = Description().apply { text = "" }
                setDrawMarkers(true)
                marker = CustomMarkerView(context, R.layout.custom_marker_view)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    gridLineWidth = 0.5f
                    valueFormatter = TimeAxisValueFormatter() // Use the custom formatter
                }
                axisLeft.apply {
                    granularity = 2f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    gridLineWidth = 0.5f
                }
                axisRight.isEnabled = false
                setBackgroundColor(Color.WHITE)
                setDrawGridBackground(false)
                setGridBackgroundColor(Color.WHITE)

                // Enable zoom and scroll
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Sensor Data").apply {
                color = Color.parseColor("#A020F0")
                setCircleColor(Color.WHITE)
                circleHoleColor = Color.parseColor("#A020F0")
                circleRadius = 4f
                lineWidth = 2f
                setDrawValues(false)
                fillAlpha = 110
                fillColor = Color.parseColor("#A020F0")
                setDrawFilled(true)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}


class TimeAxisValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val millis = value.toLong() * 1000 // Convert seconds to milliseconds
        return dateFormat.format(Date(millis))
    }
}

@Composable
fun DraggableBlock(
    offset: Offset,
    onDrag: (Offset) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    onGloballyPositioned: (IntSize) -> Unit,
    allowDrag: (Offset, Size) -> Boolean,
    content: @Composable () -> Unit
) {
    var position by remember { mutableStateOf(offset) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val screenWidthPx = with(density) { screenWidth.toPx() }
                        val screenHeightPx = with(density) { screenHeight.toPx() }

                        val newPosition = when {
                            position.x < 0f -> position.copy(x = 0f)
                            position.x + size.width > screenWidthPx -> position.copy(x = screenWidthPx - size.width)
                            else -> position
                        }.let {
                            when {
                                it.y < 0f -> it.copy(y = 0f)
                                it.y + size.height > screenHeightPx -> it.copy(y = screenHeightPx - size.height)
                                else -> it
                            }
                        }
                        position = newPosition
                        onDrag(newPosition)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newPosition = position + Offset(dragAmount.x, dragAmount.y)
                    if (allowDrag(change.position, Size(size.width.toFloat(), size.height.toFloat()))) {
                        position = newPosition
                        onDrag(newPosition)
                    }
                }
            }
            .onGloballyPositioned { coordinates ->
                size = coordinates.size
                onGloballyPositioned(size)
            }
    ) {
        content()
    }
}


fun preventOverlap(
    offset1: MutableState<Offset>,
    size1: MutableState<IntSize>,
    offset2: MutableState<Offset>,
    size2: MutableState<IntSize>,
    screenWidth: Dp,
    screenHeight: Dp,
    density: Density
) {
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    val overlapX = offset1.value.x < offset2.value.x + size2.value.width && offset1.value.x + size1.value.width > offset2.value.x
    val overlapY = offset1.value.y < offset2.value.y + size2.value.height && offset1.value.y + size1.value.height > offset2.value.y

    if (overlapX && overlapY) {
        val newOffset1 = Offset(
            x = (offset2.value.x + size2.value.width).coerceIn(0f, screenWidthPx - size1.value.width.toFloat()),
            y = offset1.value.y
        )

        val newOffset2 = Offset(
            x = offset2.value.x,
            y = (offset1.value.y + size1.value.height).coerceIn(0f, screenHeightPx - size2.value.height.toFloat())
        )

        val correctedOffset1 = Offset(
            x = newOffset1.x,
            y = newOffset1.y.coerceIn(0f, screenHeightPx - size1.value.height.toFloat())
        )

        val correctedOffset2 = Offset(
            x = newOffset2.x,
            y = newOffset2.y.coerceIn(0f, screenHeightPx - size2.value.height.toFloat())
        )

        if (correctedOffset1.x < screenWidthPx && correctedOffset1.y < screenHeightPx) {
            offset1.value = correctedOffset1
        }

        if (correctedOffset2.x < screenWidthPx && correctedOffset2.y < screenHeightPx) {
            offset2.value = correctedOffset2
        }
    }
}
