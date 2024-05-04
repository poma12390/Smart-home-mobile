package se.ifmo.ru.smartapp.ui.data

import androidx.lifecycle.MutableLiveData

data class Room(val id: Long, val name: String, val type: String, val stateId: Long)

fun getRoomStateIdById(id: Long, rooms: MutableLiveData<List<Room>>): Long? {
    return rooms.value?.find { it.id == id }?.stateId
}

