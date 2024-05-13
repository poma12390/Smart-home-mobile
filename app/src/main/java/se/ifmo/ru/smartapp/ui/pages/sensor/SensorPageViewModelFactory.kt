package se.ifmo.ru.smartapp.ui.pages.sensor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SensorPageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorPageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}