package com.bluetalk.app.messaging

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessageManager {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun addLocalMessage(senderName: String, body: String, replyToMessageId: String? = null) {
        _messages.value = _messages.value + Message(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            body = body,
            replyToMessageId = replyToMessageId,
            timestampMillis = System.currentTimeMillis(),
        )
    }

    fun clearEphemeralMessages() {
        _messages.value = emptyList()
    }
}
