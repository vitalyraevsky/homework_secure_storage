package com.otus.securehomework.data.crypto

import android.content.Context
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.inject.Inject


// Определяем константы, используемые в коде.

const val RSA_KEY_ALIAS = "RSA_OTUS_DEMO" // Алиас для RSA-ключа в KeyStore.
const val KEY_PROVIDER = "AndroidKeyStore" // Провайдер ключей - AndroidKeyStore, встроенный в Android механизм для безопасного хранения ключей.

const val KEY_LENGTH = 256 // Длина ключа для AES.

const val RSA_ALGORITHM = "RSA" // Алгоритм шифрования RSA.
const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding" // Режим и паддинг для RSA (для версий Android ниже M).
const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences" // Имя для SharedPreferences, где будут храниться ключи, зашифрованные RSA.
const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName" // Имя для хранения зашифрованного ключа AES в SharedPreferences.

const val AES_ALGORITHM = "AES"
const val AES_KEY_ALIAS = "AES_OTUS_DEMO"

class Keys@Inject constructor(
    private val applicationContext: Context,
    private val keyProvider: KeyProvider
) {

    protected val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }


    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }


    fun removeKeys(keyAlias: String) {
        keyStore.deleteEntry(keyAlias);
    }

    fun getAesSecretKey(): SecretKey {
        return keyProvider.getAesSecretKey()
    }

}
