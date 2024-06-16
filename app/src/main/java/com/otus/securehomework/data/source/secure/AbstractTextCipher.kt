package com.otus.securehomework.data.source.secure

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
const val KEY_PROVIDER = "AndroidKeyStore"

/**
 * Абстрактный класс для безопасного хранилища.
 * Содержит общую логику для шифрования и дешифрования.
 */
abstract class AbstractTextCipher : TextCipher {

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
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Расшифровывает переданный зашифрованный текст с использованием дешифрования AES.
     *
     * @param encryptedText Зашифрованный текст, закодированный в строку Base64.
     * @return Расшифрованный открытый текст.
     */
    override fun decrypt(encryptedText: String): String {
        val decoded = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = decoded.sliceArray(0 until 12) // Длина IV для GCM
        val encryptedBytes = decoded.sliceArray(12 until decoded.size)
        val secretKey = getAesSecretKey()
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Абстрактный метод для получения секретного ключа AES.
     *
     * @return Секретный ключ AES.
     */
    protected abstract fun getAesSecretKey(): SecretKey
}