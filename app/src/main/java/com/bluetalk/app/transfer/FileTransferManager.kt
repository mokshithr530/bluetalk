package com.bluetalk.app.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FileTransferManager {
    private val _transfers = MutableStateFlow<List<FileTransfer>>(emptyList())

    val transfers: StateFlow<List<FileTransfer>> = _transfers.asStateFlow()

    fun clearEphemeralTransfers() {
        _transfers.value = emptyList()
    }
}
