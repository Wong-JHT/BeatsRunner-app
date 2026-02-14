package com.beatrunner.domain.bluetooth

import kotlin.experimental.and

/**
 * FTMS (Fitness Machine Service) Protocol Implementation
 *
 * Standard FTMS UUIDs:
 * - Service: 0x1826
 * - Treadmill Data: 0x2ACD
 * - Indoor Bike Data: 0x2AD2
 * - Cross Trainer Data: 0x2ACE
 * - Fitness Machine Control Point: 0x2AD9
 * - Fitness Machine Status: 0x2ADA
 *
 * Custom UUID (if needed): 0xAD01
 */
object FTMSProtocol {

    // Standard FTMS Service UUIDs
    const val SERVICE_UUID = "00001826-0000-1000-8000-00805f9b34fb"
    const val TREADMILL_DATA_UUID = "00002acd-0000-1000-8000-00805f9b34fb"
    const val CONTROL_POINT_UUID = "00002ad9-0000-1000-8000-00805f9b34fb"
    const val STATUS_UUID = "00002ada-0000-1000-8000-00805f9b34fb"

    // Custom UUID (device-specific, adjust if needed)
    const val CUSTOM_CONTROL_UUID = "0000ad01-0000-1000-8000-00805f9b34fb"

    // Control Point Op Codes
    const val OP_CODE_REQUEST_CONTROL = 0x00.toByte()
    const val OP_CODE_RESET = 0x01.toByte()
    const val OP_CODE_SET_TARGET_SPEED = 0x02.toByte()
    const val OP_CODE_SET_TARGET_INCLINE = 0x03.toByte()
    const val OP_CODE_START_OR_RESUME = 0x07.toByte()
    const val OP_CODE_STOP_OR_PAUSE = 0x08.toByte()

    /** Convert speed from km/h to FTMS format (0.01 km/h resolution) Example: 5.5 km/h -> 550 */
    fun speedToBytes(speedKmh: Float): ByteArray {
        val value = (speedKmh * 100).toInt().toShort()
        return byteArrayOf(
                (value.toInt() and 0xFF).toByte(),
                ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    /** Convert incline percentage to FTMS format (0.1% resolution) Example: 5.5% -> 55 */
    fun inclineToBytes(inclinePercent: Float): ByteArray {
        val value = (inclinePercent * 10).toInt().toShort()
        return byteArrayOf(
                (value.toInt() and 0xFF).toByte(),
                ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    /** Create command to set target speed */
    fun createSetSpeedCommand(speedKmh: Float): ByteArray {
        val speedBytes = speedToBytes(speedKmh)
        return byteArrayOf(OP_CODE_SET_TARGET_SPEED) + speedBytes
    }

    /** Create command to set target incline */
    fun createSetInclineCommand(inclinePercent: Float): ByteArray {
        val inclineBytes = inclineToBytes(inclinePercent)
        return byteArrayOf(OP_CODE_SET_TARGET_INCLINE) + inclineBytes
    }

    /** Create command to request control */
    fun createRequestControlCommand(): ByteArray {
        return byteArrayOf(OP_CODE_REQUEST_CONTROL)
    }

    /** Create command to start/resume */
    fun createStartCommand(): ByteArray {
        return byteArrayOf(OP_CODE_START_OR_RESUME)
    }

    /** Create command to stop/pause */
    fun createStopCommand(): ByteArray {
        return byteArrayOf(OP_CODE_STOP_OR_PAUSE)
    }

    /**
     * Parse treadmill data from FTMS notification Treadmill Data characteristic format:
     * - Flags (2 bytes)
     * - Instantaneous Speed (2 bytes, 0.01 km/h)
     * - Average Speed (2 bytes, optional) - if Bit 1 set
     * - Total Distance (3 bytes, optional) - if Bit 2 set
     * - Inclination (2 bytes, 0.1%, optional) - if Bit 3 set
     * - Ramp Angle (2 bytes, 0.1%, optional) - if Bit 3 set
     * - etc.
     */
    fun parseTreadmillData(data: ByteArray): BluetoothModels.WorkoutData? {
        if (data.size < 4) return null

        try {
            // Parse flags (2 bytes)
            // FLAGS are 16-bit little endian
            val flags = ((data[1].toInt() and 0xFF) shl 8) or (data[0].toInt() and 0xFF)

            var offset = 2

            // Instantaneous Speed (always present) - 2 bytes
            val speed =
                    if (data.size >= offset + 2) {
                        val speedRaw =
                                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                        (data[offset].toInt() and 0xFF)
                        offset += 2
                        speedRaw / 100f
                    } else {
                        0f
                    }

            // Check Bit 1: Average Speed (2 bytes)
            if ((flags and 0x0002) != 0) {
                 offset += 2
            }

            // Check Bit 2: Total Distance (3 bytes)
            var distance: Float? = null
             if ((flags and 0x0004) != 0) {
                 if (data.size >= offset + 3) {
                     val distRaw = (data[offset].toInt() and 0xFF) or
                                   ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                   ((data[offset + 2].toInt() and 0xFF) shl 16)
                     distance = distRaw.toFloat() // meters
                     offset += 3
                 } else {
                     offset += 3
                 }
            }

            // Check Bit 3: Inclination & Ramp Angle (4 bytes total: 2 for Inc, 2 for Ramp)
            val hasInclination = (flags and 0x0008) != 0
            val incline =
                    if (hasInclination && data.size >= offset + 2) {
                        val inclineRaw =
                                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                        (data[offset].toInt() and 0xFF)
                         // Note: Ramp Angle follows (2 bytes), but we don't need it yet.
                        // We strictly only need to verify we have enough bytes for inclination
                        // But strictly speaking, if bit 3 is set, BOTH Incline and Ramp Angle are present (4 bytes)
                        // However, some implementation might be buggy. Let's safe guard.
                        offset += 2
                        // Skip Ramp Angle if we needed to be precise, but for now just taking Incline
                         if (data.size >= offset + 2) {
                             offset += 2 // Skip Ramp Angle
                         }
                        inclineRaw / 10f
                    } else {
                        0f
                    }

            return BluetoothModels.WorkoutData(
                speed = speed, 
                incline = incline,
                distance = distance
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /** Validate response from control point */
    fun isResponseSuccess(data: ByteArray): Boolean {
        // Response format: [0x80, OpCode, Result]
        // Result: 0x01 = Success, other = Error
        return data.size >= 3 && data[0] == 0x80.toByte() && data[2] == 0x01.toByte()
    }
}
