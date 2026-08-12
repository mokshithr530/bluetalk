package com.bluetalk.app.protocol

data class Packet(
    val type: PacketType,
    val payload: ByteArray = ByteArray(0),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Packet) return false
        return type == other.type && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int = 31 * type.hashCode() + payload.contentHashCode()
}
