package com.bluetalk.app.session

data class BluetalkSession(
    val id: String,
    val name: String,
    val members: List<SessionMember>,
)
