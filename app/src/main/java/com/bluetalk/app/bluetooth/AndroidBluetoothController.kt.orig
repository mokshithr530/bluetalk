package com.bluetalk.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bluetalk.app.model.DeviceIdentity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidBluetoothController(
    private val context: Context,
) : BluetoothController {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val _connectionState = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Idle)
    private val _nearbyDevices = MutableStateFlow<List<DeviceIdentity>>(emptyList())
    private val discoveredDevices = linkedMapOf<String, DeviceIdentity>()
    private var discoveryReceiverRegistered = false

    override val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()
    override val nearbyDevices: StateFlow<List<DeviceIdentity>> = _nearbyDevices.asStateFlow()

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

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        refreshAvailability()

        if (_connectionState.value != BluetoothConnectionState.Ready) {
            return
        }

        val adapter = bluetoothManager?.adapter
        if (adapter == null) {
            _connectionState.value = BluetoothConnectionState.Error("Bluetooth is not available on this device.")
            return
        }

        registerDiscoveryReceiver()
        adapter.cancelDiscovery()
        discoveredDevices.clear()
        _nearbyDevices.value = emptyList()

        val started = adapter.startDiscovery()
        _connectionState.value = if (started) {
            BluetoothConnectionState.Scanning
        } else {
            unregisterDiscoveryReceiver()
            BluetoothConnectionState.Error("Bluetooth discovery could not be started.")
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        bluetoothManager?.adapter?.cancelDiscovery()
        unregisterDiscoveryReceiver()
        refreshAvailability()
    }

    private fun registerDiscoveryReceiver() {
        if (discoveryReceiverRegistered) {
            return
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        context.registerReceiver(discoveryReceiver, filter)
        discoveryReceiverRegistered = true
    }

    private fun unregisterDiscoveryReceiver() {
        if (!discoveryReceiverRegistered) {
            return
        }

        context.unregisterReceiver(discoveryReceiver)
        discoveryReceiverRegistered = false
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.bluetoothDeviceExtra() ?: return
                    val id = device.address ?: return
                    val identity = DeviceIdentity(
                        id = id,
                        displayName = device.name ?: "Unknown device",
                    )

                    discoveredDevices[id] = identity
                    _nearbyDevices.value = discoveredDevices.values.toList()
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    unregisterDiscoveryReceiver()
                    refreshAvailability()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.bluetoothDeviceExtra(): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }
}
