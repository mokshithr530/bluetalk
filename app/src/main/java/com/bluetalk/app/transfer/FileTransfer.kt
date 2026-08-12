package com.bluetalk.app.transfer

data class FileTransfer(
    val id: String,
    val fileName: String,
    val totalBytes: Long,
    val transferredBytes: Long,
    val state: TransferState,
)
