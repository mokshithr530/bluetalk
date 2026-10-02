package com.bluetalk.app.bluetooth

import com.bluetalk.app.model.DeviceIdentity
import kotlinx.coroutines.flow.Flow

interface IBluetoothClient {
    suspend fun connect(device: DeviceIdentity): Result<IBluetoothConnection>
}

interface IBluetoothConnection {
    val incomingBytes: Flow<ByteArray>
    suspend fun write(bytes: ByteArray): Result<Unit>
    suspend fun close()
}
