package com.otus.securehomework.data.crypto

import android.util.Base64
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class Security @Inject constructor() {
    private val initializationVector = byteArrayOf(16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)

    fun encryptAes(plainText: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, getInitializationVector())
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        return IvParameterSpec(initializationVector)
    }

    fun decryptAes(encrypted: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector())
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    companion object {
        private const val AES_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }
}