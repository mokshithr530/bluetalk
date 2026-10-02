package com.bluetalk.app.session

import com.bluetalk.app.model.DeviceIdentity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SessionManagerImpl : ISessionManager {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.NoSession)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override fun createSession(hostId: String) {
        val session = BluetalkSession(
            id = UUID.randomUUID().toString(),
            name = "Private Session",
            members = listOf(SessionMember(DeviceIdentity(hostId, "Host"), SessionRole.Host))
        )
        _sessionState.value = SessionState.Active(session)
    }

    override fun joinSession(sessionId: String, participantId: String) {
        val session = BluetalkSession(
            id = sessionId,
            name = "Joined Session",
            members = listOf(SessionMember(DeviceIdentity(participantId, "Joiner"), SessionRole.Guest))
        )
        _sessionState.value = SessionState.Active(session)
    }

    override fun endSession() {
        val active = _sessionState.value as? SessionState.Active
        if (active != null) {
            _sessionState.value = SessionState.Ending(active.session.id)
        }
        _sessionState.value = SessionState.NoSession
    }
}
