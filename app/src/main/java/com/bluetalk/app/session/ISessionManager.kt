package com.bluetalk.app.session

import kotlinx.coroutines.flow.StateFlow

interface ISessionManager {
    val sessionState: StateFlow<SessionState>
    fun createSession(hostId: String)
    fun joinSession(sessionId: String, participantId: String)
    fun endSession()
}
