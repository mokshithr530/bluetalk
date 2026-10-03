package com.bluetalk.app.protocol

import java.nio.ByteBuffer

object PacketEncoder {
    fun encode(packet: Packet): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 4 + packet.payload.size)
        buffer.put(packet.type.ordinal.toByte())
        buffer.putInt(packet.payload.size)
        buffer.put(packet.payload)
        return buffer.array()
    }
}
