package com.bluetalk.app.transfer

sealed interface TransferState {
    data object Queued : TransferState
    data class InProgress(val progress: Float) : TransferState
    data object Paused : TransferState
    data object Complete : TransferState
    data object Cancelled : TransferState
    data class Failed(val reason: String) : TransferState
}
