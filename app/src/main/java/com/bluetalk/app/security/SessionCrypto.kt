package com.bluetalk.app.security

interface SessionCrypto {
    suspend fun seal(plainBytes: ByteArray, key: SessionKey): Result<ByteArray>
    suspend fun open(sealedBytes: ByteArray, key: SessionKey): Result<ByteArray>
}
