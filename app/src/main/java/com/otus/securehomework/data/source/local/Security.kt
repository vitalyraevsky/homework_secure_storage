package com.otus.securehomework.data.source.local

import android.util.Base64
import javax.crypto.Cipher
import javax.inject.Inject

private const val AES_TRANSFORMATION = "AES/ECB/PKCS7Padding"

class Security @Inject constructor(
    private val keys: Keys
) {

    fun encryptAes(plainText: String): String {
        val key = keys.getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String): String {
        val key = keys.getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }
}