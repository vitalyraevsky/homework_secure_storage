package com.otus.securehomework.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class EncryptionMAndMore @Inject constructor(
    private val keyStore: KeyStore
) : Encryption {

    override suspend fun encrypt(bytes: ByteArray?): EncryptionResult {
        val cipher = encryptCipher()
        val encryptedBytes = bytes?.let { cipher.doFinal(bytes) } ?: byteArrayOf()
        return EncryptionResult(bytes = encryptedBytes, iv = cipher.iv)
    }

    override suspend fun decrypt(encryptedBytes: ByteArray?, iv: ByteArray) =
        encryptedBytes?.let {
            decryptCipher(iv).doFinal(it)
        } ?: byteArrayOf()

    private fun encryptCipher() = Cipher.getInstance(AES_TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey(), IvParameterSpec(randomIv()))
    }

    private fun decryptCipher(iv: ByteArray) = Cipher.getInstance(AES_TRANSFORMATION).apply {
        init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
    }

    private fun randomIv() = ByteArray(IV_SIZE).also {
        SecureRandom().nextBytes(it)
    }

    private fun getKey() =
        keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateKey()

    private fun generateKey() = KeyGenerator.getInstance(KEY_ALGORITHM_AES).apply {
        init(
            KeyGenParameterSpec.Builder(
                AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(BLOCK_MODE_CBC)
                .setEncryptionPaddings(PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(false)
                .build()
        )
    }.generateKey()

    companion object {

        private const val KEY_ALGORITHM_AES = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE_CBC = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING_PKCS7 = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val AES_TRANSFORMATION = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$PADDING_PKCS7"

        private const val AES_KEY_ALIAS = "AES_KEY_ALIAS"

        private const val IV_SIZE = 16
    }
}

