package se.ifmo.ru.smartapp

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.data.WeatherData
import se.ifmo.ru.smartapp.ui.pages.LoginPage
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import se.ifmo.ru.smartapp.ui.pages.RegisterPage
import se.ifmo.ru.smartapp.ui.pages.main.MainPageContent
import se.ifmo.ru.smartapp.ui.pages.room.RoomPageContent
import se.ifmo.ru.smartapp.ui.pages.sensor.SensorPageContent


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PageUtils.init(application)
        sharedPref = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("auth_token", "") ?: ""
        val isLoggedIn = checkIfUserLoggedIn()
        setContent {
            MainContent(isLoggedIn)
        }
    }

    @Composable
    fun MainContent(isLoggedIn: Boolean) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = if (isLoggedIn) "main" else "login") {
            composable("login") { LoginPage(navController) }
            composable("register") { RegisterPage(navController) }
            composable("main") { MainPageContent(navController) }
            composable("room") { RoomPageContent(navController) }
            composable("sensor") { SensorPageContent(navController) }

        }
    }


    private fun checkIfUserLoggedIn(): Boolean {
        return token.isNotEmpty()
    }
}