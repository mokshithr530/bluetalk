package com.bluetalk.app.protocol

import java.util.UUID

object ProtocolConstants {
    const val ProtocolVersion = 1
    const val MaxPacketBytes = 64 * 1024
    val BLUETALK_UUID: UUID = UUID.fromString("c0423c4a-6f68-4f24-9b8f-124b178dbf8e")
}
