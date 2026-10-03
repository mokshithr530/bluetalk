package com.bluetalk.app.protocol

enum class PacketType {
    Hello,
    SessionEnd,
    TextMessage,
    FileMetadata,
    FileChunk
}
