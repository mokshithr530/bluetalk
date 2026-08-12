package com.bluetalk.app.bluetooth

import com.bluetalk.app.model.DeviceIdentity

interface BluetoothClient {
    suspend fun connect(device: DeviceIdentity): Result<BluetoothConnection>
}
