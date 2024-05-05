package se.ifmo.ru.smartapp.ui.data

import androidx.lifecycle.MutableLiveData

data class Switch(
    val id: Long,
    val name: String,
    val type: String, // Считаем, что тип ограничен заранее определенными значениями "power", "lock" и т.д.
    var enabled: Boolean,
    var stateId: Long,
    val roomId: Long

) {
    fun tap() {
        enabled = !enabled
    }

}


fun hasSwitchWithId(roomID: Long, switches: MutableLiveData<List<Switch>>): Boolean {
    return switches.value?.any { it.roomId == roomID } ?: false
}

fun getSwitchStateIdById(id: Long, switches: MutableLiveData<List<Switch>>): Long? {
    val switchList = switches.value ?: return 0

    // Ищем переключатель с заданным id
    val switch = switchList.find { it.id == id }

    // Возвращаем stateId если переключатель найден, иначе возвращаем 0
    return switch?.stateId ?: 0
}


