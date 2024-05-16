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
import se.ifmo.ru.smartapp.ui.pages.PageNames.*
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
        NavHost(navController, startDestination = if (isLoggedIn) MAIN_PAGE.pageName else LOGIN_PAGE.pageName) {
            composable(LOGIN_PAGE.pageName) { LoginPage(navController) }
            composable(REGISTER_PAGE.pageName) { RegisterPage(navController) }
            composable(MAIN_PAGE.pageName) { MainPageContent(navController) }
            composable(ROOM_PAGE.pageName) { RoomPageContent(navController) }
            composable(SENSOR_PAGE.pageName) { SensorPageContent(navController) }

        }
    }


    private fun checkIfUserLoggedIn(): Boolean {
        return token.isNotEmpty()
    }
}