package com.otus.securehomework.data.source.secure

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val AES_KEY_ALIAS = "AES_SECURE_STORAGE"

/**
 * Реализация безопасного хранилища для уровней API >= 23.
 * Использует AndroidKeyStore для безопасного хранения ключа AES.
 *
 * @param context Контекст приложения.
 */
@RequiresApi(Build.VERSION_CODES.M)
class TextCipherModern(
    private val context: Context
) : AbstractTextCipher() {

    /**
     * Получает секретный ключ AES из AndroidKeyStore. Если ключ не существует, генерирует новый ключ.
     *
     * @return Секретный ключ AES.
     */
    override fun getAesSecretKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
    }

    /**
     * Генерирует новый секретный ключ AES и сохраняет его в AndroidKeyStore.
     *
     * @return Сгенерированный секретный ключ AES.
     */
    private fun generateAesSecretKey() = KeyGenerator
        .getInstance("AES", KEY_PROVIDER)
        .apply {
            init(
                KeyGenParameterSpec.Builder(AES_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }
        .generateKey()

}