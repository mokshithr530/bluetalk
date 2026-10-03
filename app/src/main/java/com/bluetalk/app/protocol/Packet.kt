package com.bluetalk.app.protocol

data class Packet(
    val type: PacketType,
    val payload: ByteArray = ByteArray(0),
)
