package com.beatrunner.domain.bluetooth

actual class BluetoothPermissionChecker {
    actual fun hasPermissions(): Boolean {
        // iOS permissions are handled by the OS automatically when Bluetooth is used.
        // We can assume true here, or implement CBCentralManager check if strictly needed.
        // For Kable, it handles the central manager internally.
        return true
    }
}
