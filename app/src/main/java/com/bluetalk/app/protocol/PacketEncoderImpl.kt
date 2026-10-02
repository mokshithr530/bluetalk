package com.bluetalk.app.protocol

import java.nio.ByteBuffer

class PacketEncoderImpl : IPacketEncoder {
    override fun encode(payload: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(4 + payload.size)
        buffer.putInt(payload.size)
        buffer.put(payload)
        return buffer.array()
    }

    private var incomingBuffer = ByteBuffer.allocate(64 * 1024)
    private var currentPosition = 0

    override fun decode(stream: ByteArray): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        
        // Very basic implementation. Real one handles chunking across stream reads.
        if (currentPosition + stream.size > incomingBuffer.capacity()) {
            val newBuffer = ByteBuffer.allocate(incomingBuffer.capacity() * 2)
            newBuffer.put(incomingBuffer.array(), 0, currentPosition)
            incomingBuffer = newBuffer
        }
        
        incomingBuffer.position(currentPosition)
        incomingBuffer.put(stream)
        currentPosition += stream.size
        
        incomingBuffer.flip()
        
        while (incomingBuffer.remaining() >= 4) {
            incomingBuffer.mark()
            val length = incomingBuffer.int
            if (incomingBuffer.remaining() >= length) {
                val payload = ByteArray(length)
                incomingBuffer.get(payload)
                packets.add(payload)
            } else {
                incomingBuffer.reset()
                break
            }
        }
        
        val remaining = incomingBuffer.remaining()
        if (remaining > 0) {
            incomingBuffer.compact()
            currentPosition = remaining
        } else {
            incomingBuffer.clear()
            currentPosition = 0
        }
        
        return packets
    }
}
