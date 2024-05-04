package se.ifmo.ru.smartapp.ui.data

import androidx.lifecycle.MutableLiveData

data class Switch(
    val id: Long,
    val name: String,
    val type: String, // Считаем, что тип ограничен заранее определенными значениями "power", "lock" и т.д.
    val enabled: Boolean,
    val stateId: Long,
    val roomId: Long
)

fun hasSwitchWithId(roomID: Long, switches: MutableLiveData<List<Switch>>): Boolean {
    return switches.value?.any { it.roomId == roomID } ?: false
}

fun getSwitchStateIdById(id: Long, switches: MutableLiveData<List<Switch>>): Long? {
    return switches.value?.find { it.id == id }?.stateId
}
