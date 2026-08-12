package com.bluetalk.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidBluetoothController(
    private val context: Context,
) : BluetoothController {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val _connectionState = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Idle)

    override val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    override fun requiredPermissions(): List<String> = bluetoothRuntimePermissions()

    @SuppressLint("MissingPermission")
    override fun refreshAvailability() {
        val missingPermission = requiredPermissions().any { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        _connectionState.value = when {
            missingPermission -> BluetoothConnectionState.PermissionRequired
            bluetoothManager?.adapter == null -> BluetoothConnectionState.Error("Bluetooth is not available on this device.")
            bluetoothManager.adapter?.isEnabled == true -> BluetoothConnectionState.Ready
            else -> BluetoothConnectionState.Idle
        }
    }
}
