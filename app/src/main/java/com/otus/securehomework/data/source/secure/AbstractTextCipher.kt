package com.otus.securehomework.data.source.secure

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec

const val KEY_PROVIDER = "AndroidKeyStore"

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV = "3134003223491201"
private const val GCM_IV_LENGTH = 12

/**
 * Абстрактный класс для безопасного хранилища.
 * Содержит общую логику для шифрования и дешифрования.
 */
abstract class AbstractTextCipher : TextCipher {

    private val FIXED_IV = IV.toByteArray()

    protected val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    /**
     * Шифрует переданный текст с использованием шифрования AES.
     *
     * @param text Открытый текст для шифрования.
     * @return Зашифрованный текст, закодированный в строку Base64.
     */
    override fun encrypt(text: String): String {
        val secretKey = getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, getInitializationVector())
        val encodedBytes = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    /**
     * Расшифровывает переданный зашифрованный текст с использованием дешифрования AES.
     *
     * @param encryptedText Зашифрованный текст, закодированный в строку Base64.
     * @return Расшифрованный открытый текст.
     */
    override fun decrypt(encryptedText: String): String {
        val secretKey = getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, getInitializationVector())
        val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }


    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        FIXED_IV.copyInto(iv, 0, GCM_IV_LENGTH)
        return GCMParameterSpec(128, iv)
    }


    /**
     * Абстрактный метод для получения секретного ключа AES.
     *
     * @return Секретный ключ AES.
     */
    protected abstract fun getAesSecretKey(): SecretKey
}