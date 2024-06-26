package se.ifmo.ru.smartapp.ui.data

data class RangeSwitch(
    val id: Long,
    val name: String,
    val type: String,
    var enabled: Boolean,
    var value: Double,
    var stateId: Long,
    val minValue: Double,
    val maxValue: Double,
    val roomId: Long
)