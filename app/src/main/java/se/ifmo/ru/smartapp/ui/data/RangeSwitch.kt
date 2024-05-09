package se.ifmo.ru.smartapp.ui.data

data class RangeSwitch(
    val id: Long,
    val name: String,
    val type: String,
    var enabled: Boolean,
    var value: Long,
    var stateId: Long,
    val roomId: Long
)