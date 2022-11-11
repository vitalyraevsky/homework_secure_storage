package com.otus.securehomework.data.crypto

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

private const val AES_TRANSFORMATION = "AES/CBC/PKCS7Padding"

class Secure @Inject constructor() {

    private val iv = byteArrayOf(50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 61, 62, 63, 64, 65, 66)
    private val vector = IvParameterSpec(iv)
    private val cipher = Cipher.getInstance(AES_TRANSFORMATION)

    fun encryptAes(plainText: String, key: Key): String {
        cipher.init(Cipher.ENCRYPT_MODE, key, vector)
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String, key: Key): String {
        cipher.init(Cipher.DECRYPT_MODE, key, vector)
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }
}