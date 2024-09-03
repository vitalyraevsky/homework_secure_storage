package com.otus.securehomework.data.encryptor

import android.util.Base64
import com.otus.securehomework.data.encryptor.secretkeymanager.SecretKeyManager
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class Encryptor @Inject constructor(
    private val secretKeyManager: SecretKeyManager
) {
    companion object {
        private const val AES_KEY_ALIAS = "AES_KEY_OTUS"

        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val secureRandom by lazy { SecureRandom() }

    /**
     * Шифрует
     * @param plainText c использованием шифрования AES
     */
    suspend fun encryptAes(plainText: String): String {
        val secretKey = secretKeyManager.getSecretKey(AES_KEY_ALIAS)
        val iv = generateIV()
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        val cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey, spec)
        }

        val encryptedData = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combinedData = iv + encryptedData

        return Base64.encodeToString(combinedData, Base64.NO_WRAP)
    }

    /**
     * Расшифровывает
     * @param encryptedText c помощью алгоритма AES
     */
    suspend fun decryptAes(encryptedText: String): String {
        val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)

        val secretKey = secretKeyManager.getSecretKey(AES_KEY_ALIAS)
        val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        val cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, spec)
        }

        val encryptedData = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)
        val decoded = cipher.doFinal(encryptedData)

        return String(decoded, Charsets.UTF_8)
    }

    /** Генерирует новый вектор инициализации (IV) */
    private fun generateIV(): ByteArray = ByteArray(GCM_IV_LENGTH).apply {
        secureRandom.nextBytes(this)
    }
}