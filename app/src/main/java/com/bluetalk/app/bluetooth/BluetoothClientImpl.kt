package com.bluetalk.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.protocol.ProtocolConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothClientImpl(private val context: Context) : IBluetoothClient {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)

    @SuppressLint("MissingPermission")
    override suspend fun connect(device: DeviceIdentity): Result<IBluetoothConnection> = withContext(Dispatchers.IO) {
        val adapter = bluetoothManager?.adapter ?: return@withContext Result.failure(Exception("Bluetooth not available"))
        try {
            val remoteDevice = adapter.getRemoteDevice(device.id)
            val socket = remoteDevice.createRfcommSocketToServiceRecord(ProtocolConstants.BLUETALK_UUID)
            adapter.cancelDiscovery()
            socket.connect()
            Result.success(BluetoothConnectionImpl(socket))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class BluetoothConnectionImpl(private val socket: android.bluetooth.BluetoothSocket) : IBluetoothConnection {
    private val inputStream = socket.inputStream
    private val outputStream = socket.outputStream

    override val incomingBytes: Flow<ByteArray> = flow {
        val buffer = ByteArray(4096)
        while (true) {
            val bytes = try {
                inputStream.read(buffer)
            } catch (e: IOException) {
                break
            }
            if (bytes == -1) break
            emit(buffer.copyOf(bytes))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun write(bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            outputStream.write(bytes)
            outputStream.flush()
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            try { socket.close() } catch (e: Exception) {}
        }
    }
}
