package com.bluetalk.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetalk.app.bluetooth.BluetoothController
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.ConnectionRole
import com.bluetalk.app.session.SessionManager
import com.bluetalk.app.session.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val bluetoothController: BluetoothController,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val connectionRole = MutableStateFlow(ConnectionRole.Host)
    private val selectedDevice = MutableStateFlow<DeviceIdentity?>(null)

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
        sessionManager.createLocalSession(
            localDevice = DeviceIdentity(id = "local", displayName = "This device"),
        )
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
        super.onCleared()
    }
}
