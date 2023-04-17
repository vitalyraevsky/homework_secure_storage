package com.otus.securehomework.data.myDefence

import android.util.Base64
import java.security.InvalidKeyException
import java.security.Key
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"

class Security @Inject constructor(
    private val generator: KeyGenerator,
) {

    private val ivSpec: GCMParameterSpec by lazy {
        GCMParameterSpec(128, byteArrayOf(55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44))
    }

    fun encryptAes(plainText: String): String {
        val key = generator.getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String): String {
        val key = generator.getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }
}