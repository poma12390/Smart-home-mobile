package se.ifmo.ru.smartapp.network.responseParser

import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import se.ifmo.ru.smartapp.ui.data.Room
import se.ifmo.ru.smartapp.ui.data.Switch
import se.ifmo.ru.smartapp.ui.data.getRoomStateIdById

class ResponseParser {
    companion object {
        fun parseSwitches(
            roomId: Long,
            jsonArray: JSONArray,
            rooms: MutableLiveData<List<Room>>,
            switches: MutableLiveData<List<Switch>>
        ): List<Switch> {
            val newSwitches = mutableListOf<Switch>()
            val roomStateId = getRoomStateIdById(roomId, rooms)
            val currentSwitches = switches.value ?: listOf()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val newSwitch = Switch(
                    id = jsonObject.getLong("id"),
                    name = jsonObject.getString("name"),
                    type = jsonObject.getString("type"),
                    enabled = jsonObject.getBoolean("enabled"),
                    stateId = roomStateId,
                    roomId = roomId
                )


                val existingSwitch = currentSwitches.find { it.id == newSwitch.id }
                if (existingSwitch != null && existingSwitch.stateId > roomStateId) {
                    // Если есть существующий переключатель с большим stateId, используем его
                    newSwitches.add(existingSwitch)
                } else {
                    // В противном случае используем новый переключатель из JSON
                    newSwitches.add(newSwitch)
                }
            }


            // Обновляем LiveData с новым списком переключателей
            return newSwitches
        }

        fun parseSensors(
            jsonArray: JSONArray,
            weatherInside: MutableLiveData<Double>,
            electricity: MutableLiveData<Double>
        ) {
            for (i in 0 until jsonArray.length()) {
                val sensor = jsonArray.getJSONObject(i)
                val type = sensor.getString("type")
                val value = sensor.getDouble("value")

                when (type) {
                    "temperature" -> weatherInside.postValue(value)
                    "power" -> electricity.postValue(value)
                }
            }
        }
    }


}