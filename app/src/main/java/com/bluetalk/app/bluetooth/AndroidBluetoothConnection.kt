package com.bluetalk.app.bluetooth

import kotlinx.coroutines.sync.withLock
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class AndroidBluetoothConnection(
    private val socket: BluetoothSocket
) : BluetoothConnection {
    private val dataIn = DataInputStream(socket.inputStream)
    private val dataOut = DataOutputStream(socket.outputStream)
    private val writeLock = kotlinx.coroutines.sync.Mutex()

    override val incomingBytes: Flow<ByteArray> = flow {
        while (true) {
            try {
                val length = dataIn.readInt()
                if (length < 0 || length > 50 * 1024 * 1024) {
                    android.util.Log.e("BluetoothStream", "Invalid frame length: $length")
                    break // Max 50MB protection
                }
                val buffer = ByteArray(length)
                dataIn.readFully(buffer)
                emit(buffer)
            } catch (e: Exception) {
                android.util.Log.e("BluetoothStream", "Stream read exception", e)
                break
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun write(bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        writeLock.withLock {
            try {
                dataOut.writeInt(bytes.size)
                dataOut.write(bytes)
                dataOut.flush()
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            try {
                socket.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }
}
