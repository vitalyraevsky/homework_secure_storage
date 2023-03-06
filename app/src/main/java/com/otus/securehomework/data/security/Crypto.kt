package com.otus.securehomework.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class Crypto @Inject constructor(
    private val secretKeyGenerator: ISecretKeyGenerator,
) {

    private companion object {
        const val AES_MODE = "AES/CBC/PKCS7Padding"
        const val IV_SIZE = 128
    }

    private val cipher by lazy { Cipher.getInstance(AES_MODE) }

    private val secretKey by lazy { secretKeyGenerator.generateKey() }

    private val iv by lazy { generateIv() }

    fun encrypt(data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encodedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encrypted: String): String {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decodedBytes = cipher.doFinal(encodedBytes)
        return String(decodedBytes, Charsets.UTF_8)
    }

    private fun generateIv(): ByteArray {
        return ByteArray(IV_SIZE).apply {
            SecureRandom().nextBytes(this)
        }
    }
}