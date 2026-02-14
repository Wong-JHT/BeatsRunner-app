@file:Suppress("DEPRECATION")
package com.beatrunner.domain.bluetooth

import com.beatrunner.util.currentTimeMillis
import com.juul.kable.Characteristic
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.characteristicOf

import com.juul.kable.Advertisement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Real implementation of FtmsManager using Kable library for Bluetooth LE connectivity. Supports
 * scanning, connecting, and communicating with FTMS-compatible fitness devices.
 */
@OptIn(ExperimentalUuidApi::class)
class KableFtmsManager() : FtmsManager {

    private val managerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _connectionState =
            MutableStateFlow<BluetoothConnectionState>(
                    BluetoothConnectionState.Disconnected("Not connected")
            )
    override val connectionState: StateFlow<BluetoothConnectionState> =
            _connectionState.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothModels.BluetoothDevice?>(null)
    override val connectedDevice: StateFlow<BluetoothModels.BluetoothDevice?> =
            _connectedDevice.asStateFlow()

    private val _scannedAdvertisements = MutableStateFlow<List<Advertisement>>(emptyList())

    override val scannedDevices: StateFlow<List<BluetoothModels.BluetoothDevice>> =
            _scannedAdvertisements
                    .map { advertisements ->
                        advertisements.map { advertisement ->
                            BluetoothModels.BluetoothDevice(
                                    address = advertisement.identifier.toString(),
                                    name = advertisement.name ?: "Unknown Device",
                                    type = detectDeviceType(advertisement.name),
                                    rssi = advertisement.rssi
                            )
                        }
                    }
                    .stateIn(managerScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _workoutData = MutableStateFlow<BluetoothModels.WorkoutData?>(null)
    override val workoutData: StateFlow<BluetoothModels.WorkoutData?> = _workoutData.asStateFlow()

    private var peripheral: Peripheral? = null
    private var scanJob: Job? = null
    private var observeJob: Job? = null
    private var scanScope: CoroutineScope? = null

    // FTMS Service and Characteristic UUIDs
    private val treadmillDataCharacteristic: Characteristic =
            characteristicOf(
                    Uuid.parse(FTMSProtocol.SERVICE_UUID),
                    Uuid.parse(FTMSProtocol.TREADMILL_DATA_UUID)
            )

    private val controlPointCharacteristic: Characteristic =
            characteristicOf(
                    Uuid.parse(FTMSProtocol.SERVICE_UUID),
                    Uuid.parse(FTMSProtocol.CONTROL_POINT_UUID)
            )

    override fun startScanning(scope: CoroutineScope) {
        if (_connectionState.value is BluetoothConnectionState.Connected ||
                        _connectionState.value is BluetoothConnectionState.Connecting
        ) {
            return
        }

        _connectionState.value = BluetoothConnectionState.Scanning
        _connectionState.value = BluetoothConnectionState.Scanning
        _scannedAdvertisements.value = emptyList()
        scanScope = scope

        // Filter for FTMS Service UUID to only show relevant devices
        // Using manual filtering as Kable specific DSL version is ambiguous/failing
        val scanner = Scanner()
        
        scanJob =
                scanner.advertisements
                        .onEach { advertisement ->
                            // Manual filter for FTMS Service
                             if (advertisement.uuids.none { it == Uuid.parse(FTMSProtocol.SERVICE_UUID) }) return@onEach

                            val currentAdvertisements = _scannedAdvertisements.value.toMutableList()
                            val existingIndex =
                                    currentAdvertisements.indexOfFirst {
                                        it.identifier == advertisement.identifier
                                    }

                            if (existingIndex >= 0) {
                                currentAdvertisements[existingIndex] = advertisement
                            } else {
                                currentAdvertisements.add(advertisement)
                            }
                            _scannedAdvertisements.value = currentAdvertisements
                        }
                        .catch { error ->
                            _connectionState.value =
                                    BluetoothConnectionState.ConnectionError(
                                            "Scan error: ${error.message}"
                                    )
                        }
                        .launchIn(scope)
    }

    private fun detectDeviceType(name: String?): BluetoothModels.DeviceType {
        val deviceName = name?.lowercase() ?: ""
        return when {
            deviceName.contains("treadmill") || deviceName.contains("跑步机") ->
                    BluetoothModels.DeviceType.TREADMILL
            deviceName.contains("bike") || deviceName.contains("自行车") ->
                    BluetoothModels.DeviceType.INDOOR_BIKE
            deviceName.contains("cross") || deviceName.contains("elliptical") ->
                    BluetoothModels.DeviceType.CROSS_TRAINER
            // Default to Treadmill if we found it via FTMS service but can't determine type from name
            // This is a safer default than UNKNOWN for our specific app context
            else -> BluetoothModels.DeviceType.TREADMILL 
        }
    }

    override fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        if (_connectionState.value == BluetoothConnectionState.Scanning) {
            _connectionState.value =
                    BluetoothConnectionState.Disconnected("Scanning stopped")
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun connect(deviceAddress: String) {
        try {
            _connectionState.value = BluetoothConnectionState.Connecting
            stopScanning()

            val advertisement =
                    _scannedAdvertisements.value.find { it.identifier.toString() == deviceAddress }

            val device =
                    if (advertisement != null) {
                        BluetoothModels.BluetoothDevice(
                                address = advertisement.identifier.toString(),
                                name = advertisement.name ?: "Unknown Device",
                                type = detectDeviceType(advertisement.name),
                                rssi = advertisement.rssi
                        )
                    } else {
                        BluetoothModels.BluetoothDevice(
                                address = deviceAddress,
                                name = "Unknown Device",
                                type = BluetoothModels.DeviceType.UNKNOWN,
                                rssi = null
                        )
                    }

            peripheral = advertisement?.let { Peripheral(it) }


            peripheral?.connect()

            _connectedDevice.value = device
            _connectionState.value = BluetoothConnectionState.Connected

            startObservingData()
        } catch (e: Exception) {
            _connectionState.value =
                    BluetoothConnectionState.ConnectionError("Connection failed: ${e.message}")
            peripheral = null
            _connectedDevice.value = null
        }
    }

    private fun startObservingData() {
        val currentPeripheral = peripheral ?: return

        observeJob =
                managerScope.launch {
                    try {
                        currentPeripheral.observe(treadmillDataCharacteristic).collect { data ->
                            val parsedData = FTMSProtocol.parseTreadmillData(data)
                            if (parsedData != null) {
                                _workoutData.value =
                                        parsedData.copy(timestamp = currentTimeMillis())
                            }
                        }
                    } catch (e: Exception) {
                        println("Error observing data: ${e.message}")
                        // Don't disconnect on read error, just log
                    }
                }
    }

    override suspend fun disconnect() {
        observeJob?.cancel()
        observeJob = null

        try {
            peripheral?.disconnect()
        } catch (e: Exception) {
            println("Error during disconnect: ${e.message}")
        }

        peripheral = null
        _connectedDevice.value = null
        _workoutData.value = null
        _connectionState.value = BluetoothConnectionState.Disconnected("Disconnected")
    }

    override fun setSpeed(speedKmh: Float) {
        managerScope.launch {
            try {
                val command = FTMSProtocol.createSetSpeedCommand(speedKmh)
                peripheral?.write(controlPointCharacteristic, command)
            } catch (e: Exception) {
                println("Error setting speed: ${e.message}")
            }
        }
    }

    override fun setIncline(inclinePercent: Float) {
        managerScope.launch {
            try {
                val command = FTMSProtocol.createSetInclineCommand(inclinePercent)
                peripheral?.write(controlPointCharacteristic, command)
            } catch (e: Exception) {
                println("Error setting incline: ${e.message}")
            }
        }
    }

    override fun setResistance(level: Int) {
        println("Resistance control not supported for treadmills")
    }

    override fun sendCommand(command: BluetoothModels.TreadmillCommand) {
        command.targetSpeed?.let { setSpeed(it) }
        command.targetIncline?.let { setIncline(it) }
        command.targetResistance?.let { setResistance(it) }
    }

    override fun start() {
        managerScope.launch {
            try {
                peripheral?.write(
                        controlPointCharacteristic,
                        FTMSProtocol.createRequestControlCommand()
                )
                delay(100)
                peripheral?.write(controlPointCharacteristic, FTMSProtocol.createStartCommand())
            } catch (e: Exception) {
                println("Error starting workout: ${e.message}")
            }
        }
    }

    override fun stop() {
        managerScope.launch {
            try {
                peripheral?.write(controlPointCharacteristic, FTMSProtocol.createStopCommand())
            } catch (e: Exception) {
                println("Error stopping workout: ${e.message}")
            }
        }
    }

    override fun pause() {
        stop()
    }

    override fun resume() {
        start()
    }
}
