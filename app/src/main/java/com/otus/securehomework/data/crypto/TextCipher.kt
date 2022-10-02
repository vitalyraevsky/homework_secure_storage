package com.otus.securehomework.data.crypto

import android.util.Base64
import com.otus.securehomework.data.crypto.key.KeyProvider
import java.nio.charset.Charset
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class TextCipher @Inject constructor(
    private val keyProvider: KeyProvider
) {
    fun encryptAes(plain: String, encoding: Charset = Charsets.UTF_8): String {
        val cipher = getCipher(Cipher.ENCRYPT_MODE)
        val bytes = plain.toByteArray(encoding)
        val encodedBytes = cipher.doFinal(bytes)

        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String, encoding: Charset = Charsets.UTF_8): String {
        val cipher = getCipher(Cipher.DECRYPT_MODE)
        val bytes = encrypted.let {
            Base64.decode(it, Base64.NO_WRAP)
        }

        val decodedBytes = cipher.doFinal(bytes)

        return decodedBytes.toString(encoding)
    }

    private fun getCipher(mode: Int): Cipher {
        return Cipher.getInstance(ALGORITHM).apply {
            init(mode, keyProvider.getAesKey(), getInitializationVector())
        }
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        return IvParameterSpec(iv)
    }

    private companion object {
        const val ALGORITHM = "AES/CBC/PKCS7Padding"
    }
}