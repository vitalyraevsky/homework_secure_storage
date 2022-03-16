package com.otus.securehomework.security

import android.util.Base64
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


class AesEncryptionService @Inject constructor(
    private val ksWrapper: AesKeystoreWrapperImpl
) {

    fun encrypt(data: String): String? {
        return try {
            val dataBytes = data.toByteArray(Charsets.UTF_8)
            val (ivBytes, encryptedBytes) = ksWrapper.encryptData(dataBytes)

            val iv = Base64.encodeToString(ivBytes, Base64.DEFAULT)
            val encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

            Json.encodeToString(iv to encryptedData)
        } catch (ex: Exception) {
            Log.e("EncryptionService", "Failed to encryption data: $ex")
            null
        }
    }

    fun decrypt(data: String): String? {
        return try {
            val (iv, encryptedData) = Json.decodeFromString<Pair<String, String>>(data)
            val ivBytes = Base64.decode(iv, Base64.DEFAULT)
            val dataBytes = Base64.decode(encryptedData, Base64.DEFAULT)

            val decryptedBytes = ivBytes?.let { ksWrapper.decryptData(dataBytes, it) }
            decryptedBytes?.let { String(it, Charsets.UTF_8) }
        } catch (ex: Exception) {
            Log.e("EncryptionService", "Failed to decryption data: $ex")
            null
        }
    }
}
