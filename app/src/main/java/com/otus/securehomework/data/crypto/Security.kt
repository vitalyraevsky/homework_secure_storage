package com.otus.securehomework.data.crypto

import android.util.Base64
import java.math.BigInteger
import java.security.Key
import java.security.MessageDigest
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV = "3134003223491201"
private const val GCM_IV_LENGTH = 12

class Security @Inject constructor() {
    private val FIXED_IV = IV.toByteArray()

    fun encryptAes(plainText: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key,
            getInitializationVector()
        )
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(
            encodedBytes,
            Base64.NO_WRAP
        )
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        FIXED_IV.copyInto(
            iv,
            0,
            GCM_IV_LENGTH
        )
        return GCMParameterSpec(
            128,
            iv
        )
    }

    fun decryptAes(encrypted: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            getInitializationVector()
        )
        val decodedBytes = Base64.decode(
            encrypted,
            Base64.NO_WRAP
        )
        val decoded = cipher.doFinal(decodedBytes)
        return String(
            decoded,
            Charsets.UTF_8
        )
    }
}