package com.otus.securehomework.data.crypto

import android.util.Base64
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject


class Crypto @Inject constructor() {
    private val cipher = Cipher.getInstance(AES)

    private val iv: AlgorithmParameterSpec
        get() {
            val iv = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
            return IvParameterSpec(iv)
        }

    fun encode(text: String, key: Key): String {
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        val encodedBytes = cipher.doFinal(text.toByteArray())

        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decode(encrypted: String, key: Key): String {
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)

        return String(decoded, Charsets.UTF_8)
    }

    companion object {
        private const val AES = "AES/CBC/PKCS7Padding"
    }
}