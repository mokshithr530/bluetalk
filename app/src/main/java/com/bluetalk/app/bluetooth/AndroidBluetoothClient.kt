package com.bluetalk.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.protocol.ProtocolConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AndroidBluetoothClient(
    private val context: Context
) : BluetoothClient {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)

    @SuppressLint("MissingPermission")
    override suspend fun connect(device: DeviceIdentity): Result<BluetoothConnection> = withContext(Dispatchers.IO) {
        val adapter = bluetoothManager?.adapter ?: return@withContext Result.failure(Exception("Bluetooth not available"))
        
        try {
            val remoteDevice = adapter.getRemoteDevice(device.id)
            val socket = remoteDevice.createRfcommSocketToServiceRecord(ProtocolConstants.BLUETALK_UUID)
            
            // Cancel discovery because it otherwise slows down the connection.
            adapter.cancelDiscovery()
            
            socket.connect()
            Result.success(AndroidBluetoothConnection(socket))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
}
