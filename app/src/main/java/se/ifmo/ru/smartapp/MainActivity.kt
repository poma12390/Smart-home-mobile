package se.ifmo.ru.smartapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.ifmo.ru.smartapp.ui.pages.LoginPage
import se.ifmo.ru.smartapp.ui.pages.MainPage
import se.ifmo.ru.smartapp.ui.pages.RegisterPage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = checkIfUserLoggedIn()

        setContent {
            if (isLoggedIn) {
                MainPage(rememberNavController())
            } else {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginPage(navController) }
                    composable("register") { RegisterPage(navController) }
                    composable("main") { MainPage(navController)}
                }
            }
        }
    }
}
private fun checkIfUserLoggedIn(): Boolean {
    // Тут может быть логика проверки состояния сессии пользователя
    return false
}
