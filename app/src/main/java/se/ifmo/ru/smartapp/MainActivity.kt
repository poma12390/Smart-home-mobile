package se.ifmo.ru.smartapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.ifmo.ru.smartapp.ui.pages.login.LoginPage
import se.ifmo.ru.smartapp.ui.pages.PageUtils
import se.ifmo.ru.smartapp.ui.pages.register.RegisterPage
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