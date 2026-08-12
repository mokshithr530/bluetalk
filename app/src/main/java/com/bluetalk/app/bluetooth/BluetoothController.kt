package com.bluetalk.app.bluetooth

import android.Manifest
import android.os.Build
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val connectionState: StateFlow<BluetoothConnectionState>

    fun requiredPermissions(): List<String>
    fun refreshAvailability()
}

fun bluetoothRuntimePermissions(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    } else {
        emptyList()
    }
}
