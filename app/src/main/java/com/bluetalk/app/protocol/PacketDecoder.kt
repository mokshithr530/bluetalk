package com.bluetalk.app.protocol

import java.nio.ByteBuffer

object PacketDecoder {
    fun decode(bytes: ByteArray): Result<Packet> {
        try {
            val buffer = ByteBuffer.wrap(bytes)
            val typeOrdinal = buffer.get().toInt()
            val type = PacketType.entries.getOrNull(typeOrdinal) 
                ?: return Result.failure(IllegalArgumentException("Unknown packet type: $typeOrdinal"))
            val length = buffer.getInt()
            val payload = ByteArray(length)
            buffer.get(payload)
            return Result.success(Packet(type, payload))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
