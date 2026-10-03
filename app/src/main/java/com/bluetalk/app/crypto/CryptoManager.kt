package com.bluetalk.app.crypto

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import java.security.SecureRandom
import java.security.MessageDigest

class CryptoManager {
    private val keyPair = KeyPairGenerator.getInstance("EC").apply {
        initialize(ECGenParameterSpec("secp256r1"))
    }.generateKeyPair()

    val publicKeyBytes: ByteArray = keyPair.public.encoded

    private var sharedKey: SecretKeySpec? = null

    fun isReady(): Boolean = sharedKey != null

    fun computeSharedSecret(peerPublicKeyBytes: ByteArray) {
        val keyFactory = KeyFactory.getInstance("EC")
        val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(peerPublicKeyBytes))
        
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(keyPair.private)
        keyAgreement.doPhase(peerPublicKey, true)
        
        val sharedSecret = keyAgreement.generateSecret()
        
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(sharedSecret)
        sharedKey = SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = sharedKey ?: return plaintext
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext)
        
        return iv + ciphertext
    }

    fun decrypt(ciphertextAndIv: ByteArray): ByteArray {
        val key = sharedKey ?: return ciphertextAndIv
        try {
            if (ciphertextAndIv.size < 12) return ciphertextAndIv
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ciphertextAndIv.copyOfRange(0, 12)
            val ciphertext = ciphertextAndIv.copyOfRange(12, ciphertextAndIv.size)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            return ByteArray(0)
        }
    }
}
