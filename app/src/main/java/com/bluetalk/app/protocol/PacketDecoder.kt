package com.bluetalk.app.protocol

class PacketDecoder {
    fun decode(bytes: ByteArray): Result<Packet> {
        if (bytes.size < HeaderSize) {
            return Result.failure(IllegalArgumentException("Packet is missing its header."))
        }

        val version = bytes[0].toInt()
        if (version != ProtocolConstants.ProtocolVersion) {
            return Result.failure(IllegalArgumentException("Unsupported protocol version: $version"))
        }

        val type = PacketType.entries.getOrNull(bytes[1].toInt())
            ?: return Result.failure(IllegalArgumentException("Unknown packet type: ${bytes[1]}"))

        return Result.success(Packet(type = type, payload = bytes.copyOfRange(HeaderSize, bytes.size)))
    }

    private companion object {
        const val HeaderSize = 2
    }
}
