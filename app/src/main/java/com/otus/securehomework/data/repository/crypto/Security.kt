package com.otus.securehomework.data.repository.crypto

import android.util.Base64
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class Security @Inject constructor() {

    fun encryptAes(plainText: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = getInitializationVector()
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val bytes = plainText.toByteArray()
        val encodedBytes = cipher.doFinal(bytes)
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector())
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        return IvParameterSpec(iv)
    }

    companion object {
        private const val AES_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }

}