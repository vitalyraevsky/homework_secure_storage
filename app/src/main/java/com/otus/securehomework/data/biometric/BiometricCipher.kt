package com.otus.securehomework.data.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

class BiometricCipher @Inject constructor() {

    companion object {
        private const val KEY_PROVIDER = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "biometric_key"

        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_ALGORITHM = "AES"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply { load(null) }
    }

    fun getCipher(): Cipher = Cipher.getInstance(AES_TRANSFORMATION)

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun getSecretKey(): SecretKey =
        (keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey?) ?: generateAndStoreAesSecretKey()

    /** Генерация и сохранение AES секретного ключа в хранилище */
    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun generateAndStoreAesSecretKey(): SecretKey =
        withContext(Dispatchers.IO) {
            val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
                init(getKeyGenSpec())
            }
            keyGenerator.generateKey()
        }

    /** Установка спецификации для генерируемого ключа */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenSpec(): KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEYSTORE_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setUserAuthenticationRequired(true)
        .setUserAuthenticationValidityDurationSeconds(-1)
        .build()
}