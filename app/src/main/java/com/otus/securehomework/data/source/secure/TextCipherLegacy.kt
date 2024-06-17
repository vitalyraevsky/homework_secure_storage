package com.otus.securehomework.data.source.secure

import android.content.Context
import android.util.Base64
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
private const val SHARED_PREFERENCE_NAME = "AESEncryptedKeysSharedPreferences"
private const val ENCRYPTED_KEY_NAME = "AESEncryptedKey"

/**
 * Реализация безопасного хранилища для уровней API < 23.
 * Использует SharedPreferences для хранения зашифрованного ключа AES.
 *
 * @param context Контекст приложения.
 */
class TextCipherLegacy(
    private val context: Context
) : AbstractTextCipher() {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Получает секретный ключ AES. Если ключ не существует, генерирует новый ключ и сохраняет его.
     *
     * @return Секретный ключ AES.
     */
    override fun getAesSecretKey(): SecretKey {
        val encryptedKeyBase64Encoded = sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
        return encryptedKeyBase64Encoded?.let {
            val encryptedKey = Base64.decode(it, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            SecretKeySpec(key, "AES")
        } ?: generateAndSaveAesSecretKey()
    }

    /**
     * Генерирует новый секретный ключ AES, шифрует его с помощью RSA и сохраняет в SharedPreferences.
     *
     * @return Сгенерированный секретный ключ AES.
     */
    private fun generateAndSaveAesSecretKey(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().nextBytes(key)
        val encryptedKeyBase64Encoded = Base64.encodeToString(rsaEncryptKey(key), Base64.DEFAULT)
        sharedPreferences.edit().putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64Encoded).apply()
        return SecretKeySpec(key, "AES")
    }

    /**
     * Шифрует переданный секретный ключ с использованием RSA.
     *
     * @param secret Секретный ключ для шифрования.
     * @return Зашифрованный секретный ключ.
     */
    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    /**
     * Расшифровывает переданный зашифрованный ключ с использованием RSA.
     *
     * @param encryptedKey Зашифрованный ключ для расшифрования.
     * @return Расшифрованный ключ.
     */
    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    /**
     * Получает приватный ключ RSA из AndroidKeyStore.
     *
     * @return Приватный ключ RSA.
     */
    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey("RSA_KEY_ALIAS", null) as PrivateKey
    }

    /**
     * Получает публичный ключ RSA из AndroidKeyStore.
     *
     * @return Публичный ключ RSA.
     */
    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate("RSA_KEY_ALIAS").publicKey
    }
}