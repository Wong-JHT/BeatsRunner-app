package com.beatrunner.domain.bluetooth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface FtmsManager {
        val connectionState: StateFlow<BluetoothConnectionState>
        val connectedDevice: StateFlow<BluetoothModels.BluetoothDevice?>
        val scannedDevices: StateFlow<List<BluetoothModels.BluetoothDevice>>
        val workoutData: StateFlow<BluetoothModels.WorkoutData?>

        fun startScanning(scope: CoroutineScope)
        fun stopScanning()
        suspend fun connect(deviceAddress: String)
        suspend fun disconnect()

        // Unified Control Methods
        fun setSpeed(speedKmh: Float)
        fun setIncline(inclinePercent: Float)
        fun setResistance(level: Int)
        fun sendCommand(command: BluetoothModels.TreadmillCommand)

        // Playback Control
        fun start()
        fun stop()
        fun pause()
        fun resume()
}
