package com.beatrunner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beatrunner.data.local.DeviceStorage
import com.beatrunner.domain.bluetooth.BluetoothConnectionState
import com.beatrunner.domain.bluetooth.BluetoothModels
import com.beatrunner.domain.bluetooth.FtmsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel for Bluetooth device scanning and management */
class BluetoothViewModel(
    private val ftmsManager: FtmsManager,
    private val deviceStorage: DeviceStorage
) : ViewModel() {

    // UI state for device list
    data class DeviceListState(
        val knownDevices: List<DeviceUiModel> = emptyList(),
        val availableDevices: List<DeviceUiModel> = emptyList()
    )

    data class DeviceUiModel(
        val device: BluetoothModels.BluetoothDevice,
        val isConnected: Boolean = false,
        val isAvailable: Boolean = false
    )

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DeviceListState> =
        combine(
            ftmsManager.scannedDevices,
            ftmsManager.connectionState,
            _connectedDeviceAddress
        ) { scannedDevices, connectionState, connectedAddress ->
            val isConnected = connectionState is BluetoothConnectionState.Connected
            val currentConnectedAddress = if (isConnected) connectedAddress else null

            // 1. Process Known Devices
            val knownDevicesRaw = deviceStorage.getKnownDevices()
            val knownDeviceUiModels = knownDevicesRaw.map { device ->
                val isDevConnected = currentConnectedAddress == device.address
                val isDevAvailable = scannedDevices.any { it.address == device.address }

                // If available, update the device info (e.g. RSSI, Name) from scan result
                // But keep it in "Known" list
                val displayDevice = if (isDevAvailable) {
                    scannedDevices.first { it.address == device.address }
                } else {
                    device
                }

                DeviceUiModel(
                    device = displayDevice,
                    isConnected = isDevConnected,
                    isAvailable = isDevAvailable
                )
            }

            // Sort Known Devices: Connected -> Available -> Not Found
            val sortedKnownDevices = knownDeviceUiModels.sortedWith(
                compareByDescending<DeviceUiModel> { it.isConnected }
                    .thenByDescending { it.isAvailable }
            )

            // 2. Process Available (New) Devices
            // Filter out devices that are already in Known list
            val knownAddresses = knownDevicesRaw.map { it.address }.toSet()
            val newAvailableDevices = scannedDevices
                .filter { !knownAddresses.contains(it.address) }
                .map { device ->
                    DeviceUiModel(
                        device = device,
                        isConnected = currentConnectedAddress == device.address, // Should not happen if strictly new
                        isAvailable = true
                    )
                }
                .sortedBy { it.device.name }

            DeviceListState(
                knownDevices = sortedKnownDevices,
                availableDevices = newAvailableDevices
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, DeviceListState())

    fun startScanning() {
        ftmsManager.startScanning(viewModelScope)
    }

    fun stopScanning() {
        ftmsManager.stopScanning()
    }

    fun connectToDevice(device: BluetoothModels.BluetoothDevice) {
        viewModelScope.launch {
            _connectedDeviceAddress.value = device.address
            ftmsManager.connect(device.address)
            deviceStorage.saveDevice(device)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            ftmsManager.disconnect()
            _connectedDeviceAddress.value = null
        }
    }
}
