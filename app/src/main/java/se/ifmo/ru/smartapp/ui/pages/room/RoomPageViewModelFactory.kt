package se.ifmo.ru.smartapp.ui.pages.room

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.ifmo.ru.smartapp.ui.pages.main.MainPageViewModel

class RoomPageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomPageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}