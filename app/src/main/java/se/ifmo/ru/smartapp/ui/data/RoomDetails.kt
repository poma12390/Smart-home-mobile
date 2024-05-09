package se.ifmo.ru.smartapp.ui.data

data class RoomDetails(
    val id: Long,
    val sensors: List<Sensor>,
    val switches: List<Switch>,
    val rangeSwitches: List<RangeSwitch>
)