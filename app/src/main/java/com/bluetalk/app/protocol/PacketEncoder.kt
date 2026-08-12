package com.bluetalk.app.protocol

class PacketEncoder {
    fun encode(packet: Packet): ByteArray {
        require(packet.payload.size <= ProtocolConstants.MaxPacketBytes) {
            "Packet payload exceeds ${ProtocolConstants.MaxPacketBytes} bytes."
        }

        return byteArrayOf(
            ProtocolConstants.ProtocolVersion.toByte(),
            packet.type.ordinal.toByte(),
        ) + packet.payload
    }
}
