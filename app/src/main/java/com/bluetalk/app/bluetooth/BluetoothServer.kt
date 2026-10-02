package com.bluetalk.app.bluetooth

import kotlinx.coroutines.flow.Flow

interface BluetoothServer {
    val incomingConnections: Flow<BluetoothConnection>
    suspend fun listen()
    suspend fun stop()
}
