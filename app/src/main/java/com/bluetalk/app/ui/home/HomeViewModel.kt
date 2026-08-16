package com.bluetalk.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetalk.app.bluetooth.BluetoothController
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val bluetoothController: BluetoothController,
    private val sessionManager: SessionManager,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        bluetoothController.connectionState,
        sessionManager.sessionState,
    ) { bluetoothState, sessionState ->
        HomeUiState(
            bluetoothState = bluetoothState,
            sessionState = sessionState,
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
        sessionManager.createLocalSession(
            localDevice = DeviceIdentity(id = "local", displayName = "This device"),
        )
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
        refreshBluetoothAvailability()
    }
}
