package se.ifmo.ru.smartapp.ui.data

data class Switch(
    val id: Long,
    val name: String,
    val type: String, // Считаем, что тип ограничен заранее определенными значениями "power", "lock" и т.д.
    val enabled: Boolean
)
