package com.otus.securehomework.data.security.impl

import android.util.Base64
import com.otus.securehomework.data.security.Aes
import com.otus.securehomework.data.security.Constants.AES_TRANSFORMATION
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class AesImpl @Inject constructor() : Aes {

    private val bytes = byteArrayOf(50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 61, 62, 63, 64, 65, 66)
    private val parameterSpec = IvParameterSpec(bytes)
    private val cipher = Cipher.getInstance(AES_TRANSFORMATION)

    override fun encrypt(plainText: CharSequence, key: Key): String {
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
        return Base64.encodeToString(cipher.doFinal(plainText.toByteArray()), Base64.NO_WRAP)
    }

    override fun decrypt(encrypted: CharSequence, key: Key): CharSequence {
         cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
        return String(cipher.doFinal(Base64.decode(encrypted.toByteArray(), Base64.NO_WRAP)), Charsets.UTF_8)
    }

    private fun CharSequence.toByteArray(): ByteArray {
        val bytes = ByteArray(length)
        for (i in bytes.indices) {
            bytes[i] = this[i].code.toByte()
        }
        return bytes
    }
}