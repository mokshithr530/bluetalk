package com.bluetalk.app.session

sealed interface SessionState {
    data object NoSession : SessionState
    data class Active(val session: BluetalkSession) : SessionState
    data class Ending(val sessionId: String) : SessionState
}
