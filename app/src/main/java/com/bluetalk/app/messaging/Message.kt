package com.bluetalk.app.messaging

data class Message(
    val id: String,
    val senderName: String,
    val body: String,
    val replyToMessageId: String? = null,
    val status: MessageStatus = MessageStatus.LocalOnly,
    val timestampMillis: Long,
)
