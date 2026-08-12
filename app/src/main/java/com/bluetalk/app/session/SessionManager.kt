package com.bluetalk.app.session

import com.bluetalk.app.model.DeviceIdentity
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.NoSession)

    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun createLocalSession(localDevice: DeviceIdentity, name: String = "Private Session") {
        val session = BluetalkSession(
            id = UUID.randomUUID().toString(),
            name = name,
            members = listOf(SessionMember(localDevice, SessionRole.Host)),
        )
        _sessionState.value = SessionState.Active(session)
    }

    fun endSession() {
        val active = _sessionState.value as? SessionState.Active
        if (active != null) {
            _sessionState.value = SessionState.Ending(active.session.id)
        }
        _sessionState.value = SessionState.NoSession
    }
}
