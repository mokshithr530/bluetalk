package com.bluetalk.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import com.bluetalk.app.protocol.ProtocolConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException

class AndroidBluetoothServer(
    private val context: Context
) : BluetoothServer {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private var serverSocket: BluetoothServerSocket? = null
    private var isListening = false

    private val _incomingConnections = MutableSharedFlow<BluetoothConnection>(extraBufferCapacity = 10)
    override val incomingConnections: Flow<BluetoothConnection> = _incomingConnections.asSharedFlow()

    @SuppressLint("MissingPermission")
    override suspend fun listen() = withContext(Dispatchers.IO) {
        val adapter = bluetoothManager?.adapter ?: return@withContext
        
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord("Bluetalk", ProtocolConstants.BLUETALK_UUID)
            isListening = true
            
            while (isActive && isListening) {
                val socket = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }
                
                if (socket != null) {
                    val connection = AndroidBluetoothConnection(socket)
                    _incomingConnections.tryEmit(connection)
                }
            }
        } catch (e: IOException) {
            // Server socket creation failed
        }
    }

    override suspend fun stop() = withContext(Dispatchers.IO) {
        isListening = false
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            // Ignore
        }
        serverSocket = null
    }
}
