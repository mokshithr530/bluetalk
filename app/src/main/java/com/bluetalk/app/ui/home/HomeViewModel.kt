package com.bluetalk.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetalk.app.bluetooth.BluetoothController
import com.bluetalk.app.bluetooth.BluetoothServer
import com.bluetalk.app.bluetooth.BluetoothClient
import com.bluetalk.app.bluetooth.BluetoothConnection
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.ConnectionRole
import com.bluetalk.app.session.ISessionManager
import com.bluetalk.app.session.SessionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.nio.charset.StandardCharsets

class HomeViewModel(
    private val bluetoothController: BluetoothController,
    private val sessionManager: ISessionManager,
    private val bluetoothServer: BluetoothServer,
    private val bluetoothClient: BluetoothClient,
) : ViewModel() {
    private val connectionRole = MutableStateFlow(ConnectionRole.Host)
    private val selectedDevice = MutableStateFlow<DeviceIdentity?>(null)

    val messages = MutableStateFlow<List<String>>(emptyList())
    private var activeConnection: BluetoothConnection? = null

    val uiState: StateFlow<HomeUiState> = combine(
        bluetoothController.connectionState,
        sessionManager.sessionState,
        bluetoothController.nearbyDevices,
        connectionRole,
        selectedDevice,
    ) { bluetoothState, sessionState, nearbyDevices, role, device ->
        HomeUiState(
            bluetoothState = bluetoothState,
            sessionState = sessionState,
            nearbyDevices = nearbyDevices,
            connectionRole = role,
            selectedDevice = device,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        bluetoothController.refreshAvailability()
    }

    fun createPrivateSession() {
        connectionRole.value = ConnectionRole.Host
        selectedDevice.value = null
        sessionManager.createSession("local")
        viewModelScope.launch {
            bluetoothServer.listen()
        }
        viewModelScope.launch {
            bluetoothServer.incomingConnections.collect { connection ->
                activeConnection = connection
                listenToConnection(connection)
            }
        }
    }

    fun joinSession(device: DeviceIdentity) {
        viewModelScope.launch {
            val result = bluetoothClient.connect(device)
            result.onSuccess { connection ->
                activeConnection = connection
                sessionManager.joinSession(device.id, "local")
                listenToConnection(connection)
            }
        }
    }
    
    private fun listenToConnection(connection: BluetoothConnection) {
        viewModelScope.launch {
            connection.incomingBytes.collect { bytes ->
                val text = String(bytes, StandardCharsets.UTF_8).trimEnd(0.toChar())
                messages.value = messages.value + ("Peer: $text")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            messages.value = messages.value + ("Me: $text")
            viewModelScope.launch {
                activeConnection?.write(text.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    fun selectConnectionRole(role: ConnectionRole) {
        if (sessionManager.sessionState.value !is SessionState.NoSession) {
            return
        }

        connectionRole.value = role
        selectedDevice.value = null

        if (role == ConnectionRole.Host) {
            bluetoothController.stopDiscovery()
        }
    }

    fun selectNearbyDevice(device: DeviceIdentity) {
        if (
            connectionRole.value == ConnectionRole.Join &&
            bluetoothController.nearbyDevices.value.any { it.id == device.id }
        ) {
            selectedDevice.value = device
        }
    }

    fun requiredBluetoothPermissions(): List<String> {
        return bluetoothController.requiredPermissions()
    }

    fun refreshBluetoothAvailability() {
        bluetoothController.refreshAvailability()
    }

    fun endSession() {
        sessionManager.endSession()
        viewModelScope.launch {
            activeConnection?.close()
            activeConnection = null
            bluetoothServer.stop()
        }
        messages.value = emptyList()
    }

    fun findNearbyUsers() {
        if (connectionRole.value != ConnectionRole.Join) {
            return
        }

        selectedDevice.value = null
        bluetoothController.startDiscovery()
    }

    override fun onCleared() {
        bluetoothController.stopDiscovery()
        viewModelScope.launch {
            activeConnection?.close()
            bluetoothServer.stop()
        }
        super.onCleared()
    }
}
