package com.bluetalk.app.transfer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.bluetalk.app.bluetooth.BluetoothConnection
import com.bluetalk.app.protocol.Packet
import com.bluetalk.app.protocol.PacketEncoder
import com.bluetalk.app.protocol.PacketType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import android.util.Log

class FileTransferManager(private val context: Context) {
    suspend fun sendFile(uri: Uri, connection: BluetoothConnection, cryptoManager: com.bluetalk.app.crypto.CryptoManager? = null) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "unknown_file"

        val inputStream = try { contentResolver.openInputStream(uri) } catch (e: Exception) { null } 
            ?: try { java.io.FileInputStream(File(uri.path!!)) } catch (e: Exception) { null } 
            ?: return@withContext
        val bytes = inputStream.readBytes()
        inputStream.close()
        
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        val hashHex = hashBytes.joinToString("") { "%02x".format(it) }

        val metadata = JSONObject()
        metadata.put("name", name)
        metadata.put("size", bytes.size)
        metadata.put("hash", hashHex)

        val metaPlaintext = metadata.toString().toByteArray()
        val metaPayload = cryptoManager?.let { if (it.isReady()) it.encrypt(metaPlaintext) else metaPlaintext } ?: metaPlaintext
        val metaPacket = Packet(PacketType.FileMetadata, metaPayload)
        connection.write(PacketEncoder.encode(metaPacket))

        // Chunking
        val chunkSize = 4096
        var chunkCount = 0
        for (i in bytes.indices step chunkSize) {
            val end = (i + chunkSize).coerceAtMost(bytes.size)
            val chunkPlaintext = bytes.copyOfRange(i, end)
            val chunk = cryptoManager?.let { if (it.isReady()) it.encrypt(chunkPlaintext) else chunkPlaintext } ?: chunkPlaintext
            val chunkPacket = Packet(PacketType.FileChunk, chunk)
            connection.write(PacketEncoder.encode(chunkPacket))
            
            chunkCount++
            if (chunkCount % 50 == 0) kotlinx.coroutines.yield() // Yield every ~200KB to allow messages
        }
    }

    suspend fun sendFileTcp(uri: Uri, outputStream: java.io.OutputStream, cryptoManager: com.bluetalk.app.crypto.CryptoManager? = null) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "unknown_file"

        val inputStream = try { contentResolver.openInputStream(uri) } catch (e: Exception) { null } 
            ?: try { java.io.FileInputStream(File(uri.path!!)) } catch (e: Exception) { null } 
            ?: return@withContext
        val bytes = inputStream.readBytes()
        inputStream.close()
        
        val metadata = JSONObject()
        metadata.put("name", name)
        metadata.put("size", bytes.size)

        val metaPlaintext = metadata.toString().toByteArray()
        val metaPayload = cryptoManager?.let { if (it.isReady()) it.encrypt(metaPlaintext) else metaPlaintext } ?: metaPlaintext
        val metaPacket = Packet(PacketType.FileMetadata, metaPayload)
        outputStream.write(PacketEncoder.encode(metaPacket))

        // High-speed chunking (64KB chunks over TCP)
        val chunkSize = 65536
        for (i in bytes.indices step chunkSize) {
            val end = (i + chunkSize).coerceAtMost(bytes.size)
            val chunkPlaintext = bytes.copyOfRange(i, end)
            val chunk = cryptoManager?.let { if (it.isReady()) it.encrypt(chunkPlaintext) else chunkPlaintext } ?: chunkPlaintext
            val chunkPacket = Packet(PacketType.FileChunk, chunk)
            outputStream.write(PacketEncoder.encode(chunkPacket))
        }
        outputStream.flush()
    }

    fun saveReceivedFile(fileName: String, bytes: ByteArray) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val outputFile = File(downloadsDir, fileName)
            val fos = FileOutputStream(outputFile)
            fos.write(bytes)
            fos.close()
        } catch (e: Exception) {
            Log.e("FileTransferManager", "Failed to save file: ${e.message}")
        }
    }
}
