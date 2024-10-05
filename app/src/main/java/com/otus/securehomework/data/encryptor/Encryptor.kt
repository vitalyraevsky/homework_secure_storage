package com.otus.securehomework.data.encryptor

import android.util.Base64
import com.otus.securehomework.data.keymanager.SecretKeyManager
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class Encryptor @Inject constructor(
    private val secretKeyManager: SecretKeyManager
) {

    fun encryptAes(plainText: String): String {
        val secretKey = secretKeyManager.getSecretKey()

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, getInitializationVector())
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String): String {
        val secretKey = secretKeyManager.getSecretKey()

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, getInitializationVector())
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        IV.toByteArray().copyInto(iv, 0, GCM_IV_LENGTH)
        return GCMParameterSpec(128, iv)
    }

    private companion object {
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV = "3134003223491201"
        const val GCM_IV_LENGTH = 12
    }
}
