package se.ifmo.ru.smartapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.data.WeatherData
import se.ifmo.ru.smartapp.ui.pages.LoginPage
import se.ifmo.ru.smartapp.ui.pages.RegisterPage
import se.ifmo.ru.smartapp.ui.pages.main.MainPageContent
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), LocationListener {
    companion object{
        var weatherData : WeatherData? = null
    }
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isLoggedIn = checkIfUserLoggedIn()
        setContent {
            MainContent(isLoggedIn)
        }
        getLocation()
    }

    @Composable
    fun MainContent(isLoggedIn: Boolean) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = if (isLoggedIn) "main" else "login") {
            composable("login") { LoginPage(navController) }
            composable("register") { RegisterPage(navController) }
            composable("main") { MainPageContent(navController) }
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(location: Location) {
        lifecycleScope.launch {
            weatherData = fetchWeather(location.latitude, location.longitude)
            Log.i("Current location", location.latitude.toString() + " " + location.longitude.toString())
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkIfUserLoggedIn(): Boolean {
        // Тут может быть логика проверки состояния сессии пользователя
        return false
    }
    private suspend fun fetchWeather(lat: Double, lon: Double): WeatherData = withContext(Dispatchers.IO) {
        val apiKey = "07bae985b8db46f8c13059ba4005fa92"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("Failed : HTTP error code : ${connection.responseCode}")
        } else {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val temp = json.getJSONObject("main").getDouble("temp")
            return@withContext WeatherData(degree = temp)
        }
    }


}
