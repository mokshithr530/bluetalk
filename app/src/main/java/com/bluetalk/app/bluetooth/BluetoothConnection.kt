package com.bluetalk.app.bluetooth

import kotlinx.coroutines.flow.Flow

interface BluetoothConnection {
    val incomingBytes: Flow<ByteArray>

    suspend fun write(bytes: ByteArray): Result<Unit>
    suspend fun close()
}
