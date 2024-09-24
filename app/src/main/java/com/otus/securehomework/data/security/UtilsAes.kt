package com.otus.securehomework.data.security

import android.util.Base64
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class UtilsAes @Inject constructor() {

    fun encryptAes(data: CharSequence, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encodedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: CharSequence, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector())
        val decoded = cipher.doFinal(Base64.decode(encrypted.toByteArray(), Base64.NO_WRAP))
        return String(decoded, Charsets.UTF_8)
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        IV.toByteArray().copyInto(iv, 0, GCM_IV_LENGTH)
        return GCMParameterSpec(128, iv)
    }

    private fun CharSequence.toByteArray(): ByteArray =
        ByteArray(length) { offset: Int -> this[offset].code.toByte() }

    companion object {
        private const val AES_TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val IV = "3134003223491201"
        private const val GCM_IV_LENGTH = 12
    }
}