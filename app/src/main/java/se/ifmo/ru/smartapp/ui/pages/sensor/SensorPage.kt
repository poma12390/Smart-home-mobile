import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.ui.pages.sensor.CustomMarkerView
import se.ifmo.ru.smartapp.ui.pages.sensor.SensorPageViewModel
import se.ifmo.ru.smartapp.ui.pages.sensor.SensorPageViewModelFactory
import se.ifmo.ru.smartapp.R


@Composable
fun SensorPageContent(navController: NavController) {
    val application = LocalContext.current.applicationContext as Application
    val factory = SensorPageViewModelFactory(application)
    val viewModel: SensorPageViewModel = viewModel(factory = factory)

    val xValues by viewModel.xValues.observeAsState(initial = emptyList())
    val yValues by viewModel.yValues.observeAsState(initial = emptyList())

    val lineEntries = xValues.zip(yValues).map { (x, y) -> Entry(x.toFloat(), y.toFloat()) }
    val sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    // val sensorId = sharedPref.getLong("cur_sensor", 1)
    val sensorId : Long = 2

    LaunchedEffect(Unit) {

        coroutineScope {
            launch() {
                viewModel.fetchOutsideState(this, navController, sensorId)
            }
            launch() {
                // Запуск fetchHomeState каждые 5 секунд
                while (isActive) {
                    viewModel.fetchOutsideState(this, navController, 2)
                    delay(5000)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.LightGray)
            .padding(16.dp)
    ) {
        LineChartContainer(entries = lineEntries)
    }
}

@Composable
fun TopSection(){

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
                    color = Color.parseColor("#A020F0") // Фиолетовый цвет
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
                    setDrawGridLines(true) // Включить вертикальные линии сетки
                    gridColor = Color.LTGRAY
                    gridLineWidth = 0.5f
                }
                axisLeft.apply {
                    granularity = 2f // Показать меньше меток на оси Y
                    setDrawGridLines(true) // Включить горизонтальные линии сетки
                    gridColor = Color.LTGRAY
                    gridLineWidth = 0.5f
                }
                axisRight.isEnabled = false
                setBackgroundColor(Color.WHITE) // Установить белый фон
                setDrawGridBackground(false) // Отключить фоновую сетку
                setGridBackgroundColor(Color.WHITE)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Sensor Data").apply {
                color = Color.parseColor("#A020F0") // Фиолетовый цвет
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