package com.otus.securehomework.security

import android.util.Base64
import java.nio.charset.Charset
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class EncryptionAes @Inject constructor(private val aesSecretKeyProvider: AesSecretKeyProvider) {

    private fun getInitializationVector(): AlgorithmParameterSpec {
        return GCMParameterSpec(128, FIXED_IV)//IvParameterSpec("encryptionIntVec".toByteArray(Charset.forName("UTF-8")))
    }

    fun getSecretKey() = secretKey ?: aesSecretKeyProvider.getAesSecretKey().also {
        secretKey = it
    }

    fun encryptAes(plainText: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, aesSecretKeyProvider.getAesSecretKey(), getInitializationVector())
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, aesSecretKeyProvider.getAesSecretKey(), getInitializationVector())
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    companion object {
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private val FIXED_IV = byteArrayOf(55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44)
        var secretKey: SecretKey? = null
    }
}