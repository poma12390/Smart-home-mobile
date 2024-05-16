package se.ifmo.ru.smartapp.ui.pages

import android.app.Application
import android.util.Log
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.ifmo.ru.smartapp.network.SwitchUpdater

class PageUtils {

    companion object {
        private var curStateId: Long = 1
        private lateinit var updater: SwitchUpdater

        fun getNewStateId(): Long {
            curStateId++
            Log.i("set global state Id", curStateId.toString())
            return curStateId
        }

        fun getSwitchUpdater(): SwitchUpdater = updater

        fun setStateId(stateId: Long) {
            Log.i("set global state Id", stateId.toString())
            curStateId = stateId
        }

        fun getStateId(): Long {
            return curStateId
        }

        fun moveToPage(
            coroutineScope: CoroutineScope,
            navController: NavController,
            pageName: String
        ) {
            coroutineScope.launch {
                navController.navigate(pageName)
                delay(500)
            }
        }

        fun init(application: Application) {
            this.updater = SwitchUpdater(application)
        }
    }
}