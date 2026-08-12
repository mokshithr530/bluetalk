package com.bluetalk.app.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@JvmInline
value class SessionKey(val bytes: ByteArray)

class SessionKeyManager {
    private val _activeKey = MutableStateFlow<SessionKey?>(null)

    val activeKey: StateFlow<SessionKey?> = _activeKey.asStateFlow()

    fun clearSessionKey() {
        _activeKey.value = null
    }
}
