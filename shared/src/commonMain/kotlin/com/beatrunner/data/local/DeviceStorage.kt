package com.beatrunner.data.local

import com.beatrunner.domain.bluetooth.BluetoothModels
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Storage for known/previously connected Bluetooth devices
 */
class DeviceStorage(private val settings: Settings) {
    
    companion object {
        private const val KEY_KNOWN_DEVICES = "known_devices"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get list of known devices
     */
    fun getKnownDevices(): List<BluetoothModels.BluetoothDevice> {
        val jsonString = settings.getStringOrNull(KEY_KNOWN_DEVICES) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(BluetoothModels.BluetoothDevice.serializer()), jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save a device to known list
     * If device already exists (by address), it updates it (e.g. name change)
     */
    fun saveDevice(device: BluetoothModels.BluetoothDevice) {
        val currentList = getKnownDevices().toMutableList()
        val index = currentList.indexOfFirst { it.address == device.address }
        
        if (index >= 0) {
            currentList[index] = device
        } else {
            currentList.add(device)
        }
        
        saveList(currentList)
    }

    /**
     * Remove a device from known list
     */
    fun removeDevice(address: String) {
        val currentList = getKnownDevices().filter { it.address != address }
        saveList(currentList)
    }

    private fun saveList(list: List<BluetoothModels.BluetoothDevice>) {
        val jsonString = json.encodeToString(ListSerializer(BluetoothModels.BluetoothDevice.serializer()), list)
        settings[KEY_KNOWN_DEVICES] = jsonString
    }
}
