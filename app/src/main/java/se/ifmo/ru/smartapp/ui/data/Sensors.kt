package se.ifmo.ru.smartapp.ui.data

data class Sensor(
    val id: Long,
    val name: String,
    val type: String,
    var value: Long,
    var stateId: Long,
    val roomId: Long
)
