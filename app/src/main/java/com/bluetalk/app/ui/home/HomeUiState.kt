package com.bluetalk.app.ui.home

import com.bluetalk.app.bluetooth.BluetoothConnectionState
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.SessionState

data class HomeUiState(
    val bluetoothState: BluetoothConnectionState = BluetoothConnectionState.Idle,
    val sessionState: SessionState = SessionState.NoSession,
    val nearbyDevices: List<DeviceIdentity> = emptyList(),
)
