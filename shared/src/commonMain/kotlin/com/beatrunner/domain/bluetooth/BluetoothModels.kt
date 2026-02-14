package com.beatrunner.domain.bluetooth

import com.beatrunner.util.currentTimeMillis
import com.juul.kable.Identifier
import kotlinx.serialization.Serializable

/** Bluetooth connection state */
sealed class BluetoothConnectionState {
        object Connecting : BluetoothConnectionState()
        object Scanning : BluetoothConnectionState()
        object Connected : BluetoothConnectionState()
        object Disconnecting : BluetoothConnectionState()
        data class Disconnected(val message: String) : BluetoothConnectionState()
        data class ConnectionError(val message: String) : BluetoothConnectionState()
}

/** Bluetooth models namespace */
object BluetoothModels {
        /** Supported FTMS Device Types */
        enum class DeviceType {
                TREADMILL,
                INDOOR_BIKE,
                CROSS_TRAINER,
                UNKNOWN
        }

        /** Represents a Bluetooth LE device */
        @Serializable
        data class BluetoothDevice(
                val address: String,
                val name: String?,
                val type: DeviceType = DeviceType.UNKNOWN,
                val rssi: Int? = null
        )



        /** Command to send to the FTMS device */
        @Serializable
        data class TreadmillCommand(
                val targetSpeed: Float? = null, // km/h
                val targetIncline: Float? = null, // percentage
                val targetResistance: Int? = null, // unitless or specific scale
                val timestamp: Long = currentTimeMillis()
        )

        /** Unified Real-time workout data for all FTMS devices */
        @Serializable
        data class WorkoutData(
                val speed: Float? = null, // km/h
                val incline: Float? = null, // percentage
                val resistance: Int? = null, // unitless
                val power: Int? = null, // watts
                val cadence: Int? = null, // rpm
                val heartRate: Int? = null, // bpm
                val distance: Float? = null, // meters
                val calories: Int? = null,
                val elapsedTime: Long? = null, // milliseconds
                val timestamp: Long = currentTimeMillis()
        )

        /** Bluetooth error types */
        sealed class BluetoothError : Exception() {
                data class ConnectionFailed(
                        override val message: String = "Failed to connect to device"
                ) : BluetoothError()
                data class PermissionDenied(
                        override val message: String = "Bluetooth permission denied"
                ) : BluetoothError()
                data class DeviceNotFound(override val message: String = "Device not found") :
                        BluetoothError()
                data class CharacteristicNotFound(
                        override val message: String = "Characteristic not found"
                ) : BluetoothError()
                data class WriteError(override val message: String = "Failed to write data") :
                        BluetoothError()
        }
}
