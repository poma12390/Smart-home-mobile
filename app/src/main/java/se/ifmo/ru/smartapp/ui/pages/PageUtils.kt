package se.ifmo.ru.smartapp.ui.pages

import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PageUtils {

    companion object{
        private var curStateId: Long = 1

        fun getNewStateId(): Long{
            curStateId++
            Log.i("set global state Id", curStateId.toString())
            return curStateId
        }

        fun setStateId(stateId: Long){
            Log.i("set global state Id", stateId.toString())
            curStateId = stateId
        }

        fun getStateId(): Long{
            return curStateId
        }
        fun moveToPage(coroutineScope: CoroutineScope, navController: NavController, pageName: String) {
            coroutineScope.launch {
                navController.navigate(pageName)
                delay(500)
            }
        }
    }
}