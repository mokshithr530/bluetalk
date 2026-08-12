package com.bluetalk.app.session

import com.bluetalk.app.model.DeviceIdentity

data class SessionMember(
    val device: DeviceIdentity,
    val role: SessionRole,
)

enum class SessionRole {
    Host,
    Guest,
}
