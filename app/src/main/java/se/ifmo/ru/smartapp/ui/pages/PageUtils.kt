package se.ifmo.ru.smartapp.ui.pages

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PageUtils {

    companion object{
        fun moveToPage(coroutineScope: CoroutineScope, navController: NavController, pageName: String) {
            coroutineScope.launch {
                navController.navigate(pageName)
                delay(500)
            }
        }
    }
}