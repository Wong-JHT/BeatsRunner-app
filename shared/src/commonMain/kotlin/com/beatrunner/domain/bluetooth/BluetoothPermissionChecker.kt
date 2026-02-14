package com.beatrunner.domain.bluetooth

expect class BluetoothPermissionChecker {
    fun hasPermissions(): Boolean
}
