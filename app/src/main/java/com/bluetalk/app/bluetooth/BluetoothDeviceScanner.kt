package com.bluetalk.app.bluetooth

import com.bluetalk.app.model.DeviceIdentity
import kotlinx.coroutines.flow.Flow

interface BluetoothDeviceScanner {
    val nearbyDevices: Flow<List<DeviceIdentity>>

    suspend fun startScanning()
    suspend fun stopScanning()
}
