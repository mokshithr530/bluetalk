package com.bluetalk.app.bluetooth

sealed interface BluetoothConnectionState {
    data object Idle : BluetoothConnectionState
    data object PermissionRequired : BluetoothConnectionState
    data object Ready : BluetoothConnectionState
    data object Scanning : BluetoothConnectionState
    data object Listening : BluetoothConnectionState
    data class Connecting(val deviceName: String?) : BluetoothConnectionState
    data class Connected(val deviceName: String?) : BluetoothConnectionState
    data class Error(val message: String) : BluetoothConnectionState
}
