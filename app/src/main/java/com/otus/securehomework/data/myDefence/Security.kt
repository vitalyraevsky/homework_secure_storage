package com.otus.securehomework.data.myDefence

import android.security.keystore.KeyProperties
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject


private const val AES_TRANSFORMATION = (KeyProperties.KEY_ALGORITHM_AES + "/"
        + KeyProperties.BLOCK_MODE_CBC + "/"
        + KeyProperties.ENCRYPTION_PADDING_PKCS7)

class Security @Inject constructor(
    private val generator: KeyGenerator,
) {

    private val ivSpec: IvParameterSpec by lazy {
        IvParameterSpec(byteArrayOf(50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 61, 62, 63, 64, 65, 66))
    }

    fun encryptAes(plainText: String): String {
        val cipher = getCipher()
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
    }

    fun decryptAes(encrypted: String): String {
        val cipher = getCipher()
        val decodedBytes = Base64.decode(encrypted.toByteArray(), Base64.DEFAULT)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    fun getCipher(): Cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, generator.getAesSecretKey())
    }
}