package com.bluetalk.app.bluetooth

import android.Manifest
import android.os.Build
import com.bluetalk.app.model.DeviceIdentity
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val connectionState: StateFlow<BluetoothConnectionState>
    val nearbyDevices: StateFlow<List<DeviceIdentity>>

    fun requiredPermissions(): List<String>
    fun refreshAvailability()
    fun startDiscovery()
    fun stopDiscovery()
}

fun bluetoothRuntimePermissions(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
