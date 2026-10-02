package com.bluetalk.app.bluetooth

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class AndroidBluetoothConnection(
    private val socket: BluetoothSocket
) : BluetoothConnection {
    private val inputStream = socket.inputStream
    private val outputStream = socket.outputStream

    override val incomingBytes: Flow<ByteArray> = flow {
        val buffer = ByteArray(1024)
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
            try {
                socket.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }
}
