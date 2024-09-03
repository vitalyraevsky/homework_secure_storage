package com.otus.securehomework.data.encryptor.secretkeymanager

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class SecretKeyManagerImpl : SecretKeyManager {

    companion object {
        private const val KEY_PROVIDER = "AndroidKeyStore"

        private const val AES_ALGORITHM = "AES"
        private const val AES_KEY_LENGTH = 256
    }

    /** Инициализация хранилища ключей */
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply { load(null) }
    }

    override suspend fun getSecretKey(keyName: String): SecretKey = withContext(Dispatchers.IO) {
        keyStore.getKey(keyName, null) as? SecretKey ?: generateAndStoreAesSecretKey(keyName)
    }

    /** Генерация и сохранение AES секретного ключа в хранилище */
    private suspend fun generateAndStoreAesSecretKey(keyName: String): SecretKey =
        withContext(Dispatchers.IO) {
            val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
                init(getKeyGenSpec(keyName))
            }
            keyGenerator.generateKey()
        }

    /** Установка спецификации для генерируемого ключа */
    private fun getKeyGenSpec(keyName: String): KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        keyName,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setUserAuthenticationRequired(false)
        .setRandomizedEncryptionRequired(false)
        .setKeySize(AES_KEY_LENGTH)
        .build()
}