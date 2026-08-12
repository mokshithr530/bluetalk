package com.bluetalk.app.bluetooth

interface BluetoothServer {
    suspend fun listen()
    suspend fun stop()
}
