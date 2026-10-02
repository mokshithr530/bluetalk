package com.bluetalk.app.protocol

interface IPacketEncoder {
    fun encode(payload: ByteArray): ByteArray
    fun decode(stream: ByteArray): List<ByteArray>
}
