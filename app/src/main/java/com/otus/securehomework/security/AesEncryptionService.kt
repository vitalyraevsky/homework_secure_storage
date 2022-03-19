package com.otus.securehomework.security

import android.util.Base64
import androidx.biometric.BiometricPrompt
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class AesEncryptionService @Inject constructor(private val aesKeyProvider: AesKeyProvider) :
    EncryptionService {

    override fun encrypt(plainText: String): String {
        val key = aesKeyProvider.getKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        val encodedText = Base64.encodeToString(encodedBytes, Base64.NO_WRAP)

        val iv = cipher.iv
        val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)

        return Json.encodeToString(Pair<String, String>(encodedText, encodedIv))
    }

    override fun decrypt(encrypted: String): String {
        val (encodedText, encodedIv) = Json.decodeFromString<Pair<String, String>>(encrypted)

        val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
        val decodedBytes = Base64.decode(encodedText, Base64.NO_WRAP)

        val key = aesKeyProvider.getKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        }

        val decoded = cipher.doFinal(decodedBytes)

        return String(decoded, Charsets.UTF_8)
    }

    override fun getBiometricCryptoObject(): BiometricPrompt.CryptoObject {
        val key = aesKeyProvider.getKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

        return BiometricPrompt.CryptoObject(cipher)
    }

    private companion object {
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}