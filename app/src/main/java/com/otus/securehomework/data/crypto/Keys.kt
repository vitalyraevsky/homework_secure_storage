package com.otus.securehomework.data.crypto

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.security.crypto.MasterKey
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.security.auth.x500.X500Principal


// Определяем константы, используемые в коде.
private const val KEY_PROVIDER = "AndroidKeyStore" // Провайдер ключей - AndroidKeyStore, встроенный в Android механизм для безопасного хранения ключей.
private const val KEY_LENGTH = 256 // Длина ключа для AES.

private const val RSA_ALGORITHM = "RSA" // Алгоритм шифрования RSA.
private const val RSA_KEY_ALIAS = "RSA_OTUS_DEMO" // Алиас для RSA-ключа в KeyStore.
private const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding" // Режим и паддинг для RSA (для версий Android ниже M).
private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences" // Имя для SharedPreferences, где будут храниться ключи, зашифрованные RSA.
private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName" // Имя для хранения зашифрованного ключа AES в SharedPreferences.

private const val AES_ALGORITHM = "AES" // Алгоритм шифрования AES.
private const val AES_KEY_ALIAS = "AES_OTUS_DEMO" // Алиас для AES-ключа в KeyStore.

class Keys@Inject constructor(
    private val applicationContext: Context
) {

    // Ленивая инициализация SharedPreferences для хранения зашифрованных ключей AES.
    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    // Ленивая инициализация хранилища ключей KeyStore.
    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    // Метод для удаления ключей из KeyStore.
    fun removeKeys(keyAlias: String) {
        keyStore.deleteEntry(keyAlias);
    }

    // Получение (или генерация) AES-ключа.
    fun getAesSecretKey(): SecretKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Если версия Android >= M, то пытаемся получить ключ из KeyStore или генерируем новый.
            keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
        } else {
            // Если версия Android < M, получаем ключ из SharedPreferences или генерируем новый.
            getAesSecretKeyLessThanM() ?: generateAesSecretKey()
        }
    }

    // Получение AES-ключа для версий Android ниже M из SharedPreferences.
    private fun getAesSecretKeyLessThanM(): SecretKey? {
        val encryptedKeyBase64Encoded = getSecretKeyFromSharedPreferences() // Получаем зашифрованный ключ в виде строки Base64.
        return encryptedKeyBase64Encoded?.let {
            val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT) // Декодируем Base64.
            val key = rsaDecryptKey(encryptedKey) // Расшифровываем ключ с использованием RSA.
            SecretKeySpec(key, AES_ALGORITHM) // Создаем и возвращаем SecretKey на основе расшифрованного ключа.
        }
    }

    // Расшифровка ключа с использованием RSA.
    private fun rsaDecryptKey(encryptedKey: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M) // Инициализируем шифр с заданным режимом для RSA.
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey()) // Инициализируем шифр для расшифровки с приватным ключом.
        return cipher.doFinal(encryptedKey) // Возвращаем расшифрованные данные.
    }

    // Получение зашифрованного ключа из SharedPreferences.
    private fun getSecretKeyFromSharedPreferences(): String? {
        return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
    }


    // Метод генерации нового AES-ключа. Если версия Android >= M, используется KeyGenerator.
    private fun generateAesSecretKey(): SecretKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getKeyGenerator().generateKey() // Генерация нового AES-ключа с использованием KeyGenerator.
        } else {
            generateAndSaveAesSecretKeyLessThanM() // Если версия ниже M, генерируем ключ и сохраняем его.
        }
    }

    // Генерация и сохранение AES-ключа для версий Android ниже M.
    private fun generateAndSaveAesSecretKeyLessThanM(): SecretKey {
        val key = ByteArray(16) // Создание байтового массива длиной 16 байт для ключа.
        SecureRandom().run {
            nextBytes(key) // Генерация случайных байтов для ключа.
        }
        val encryptedKeyBase64encoded = Base64.encodeToString(
            rsaEncryptKey(key), // Шифруем ключ с использованием RSA.
            Base64.DEFAULT
        )
        // Сохраняем зашифрованный ключ в SharedPreferences.
        sharedPreferences.edit().apply {
            putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            apply() // Применяем изменения.
        }
        return SecretKeySpec(key, AES_ALGORITHM) // Возвращаем AES-ключ, созданный из массива байт.
    }

    // Метод шифрования ключа с использованием RSA.
    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M) // Инициализация шифра с режимом RSA/ECB/PKCS1Padding.
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey()) // Инициализация шифра для шифрования с публичным ключом.
        return cipher.doFinal(secret) // Возвращаем зашифрованный ключ.
    }

    // Метод получения KeyGenerator для AES на Android >= M.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenerator() = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
        init(getKeyGenSpec()) // Инициализация генератора ключей с параметрами.
    }

    // Метод получения спецификации генерации ключа (KeyGenParameterSpec) для Android >= M.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS, // Устанавливаем алиас ключа.
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT // Указываем, что ключ используется для шифрования и дешифрования.
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM) // Устанавливаем режим блочного шифрования GCM.
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // Устанавливаем отсутствие паддинга.
            .setUserAuthenticationRequired(false) // Отключаем требование аутентификации пользователя.
            .setRandomizedEncryptionRequired(false) // Отключаем требование случайной генерации.
            .setKeySize(KEY_LENGTH) // Устанавливаем размер ключа в битах.
            .build() // Создаем и возвращаем спецификацию.
    }



    // Метод получения приватного RSA-ключа.
    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey ?: generateRsaSecretKey().private
        // Возвращаем приватный ключ, если он существует, иначе генерируем новый.
    }

    // Метод получения публичного RSA-ключа.
    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public
        // Возвращаем публичный ключ, если он существует, иначе генерируем новый.
    }

    // Метод генерации новой пары RSA-ключей.
    private fun generateRsaSecretKey(): KeyPair {
        val spec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Для Android >= M создаем KeyGenParameterSpec.
            KeyGenParameterSpec.Builder(
                RSA_KEY_ALIAS, // Устанавливаем алиас ключа.
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT // Указываем, что ключ используется для шифрования и дешифрования.
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB) // Устанавливаем режим блочного шифрования ECB.
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1) // Устанавливаем паддинг PKCS1.
                .setUserAuthenticationRequired(true) // Включаем требование аутентификации пользователя.
                .setRandomizedEncryptionRequired(false) // Отключаем требование случайной генерации.
                .build()
        } else {
            // Для Android < M создаем KeyPairGeneratorSpec.
            val start: Calendar = Calendar.getInstance() // Начальная дата.
            val end: Calendar = Calendar.getInstance() // Конечная дата.
            end.add(Calendar.YEAR, 30) // Устанавливаем срок действия ключа на 30 лет.
            KeyPairGeneratorSpec.Builder(applicationContext)
                .setAlias(RSA_KEY_ALIAS) // Устанавливаем алиас ключа.
                .setSubject(X500Principal("CN=$RSA_KEY_ALIAS")) // Устанавливаем X500Principal для сертификата.
                .setSerialNumber(BigInteger.TEN) // Устанавливаем серийный номер.
                .setStartDate(start.getTime()) // Устанавливаем начальную дату.
                .setEndDate(end.getTime()) // Устанавливаем конечную дату.
                .build()
        }
        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(spec) // Инициализируем генератор ключей с созданной спецификацией.
            generateKeyPair() // Генерируем и возвращаем пару ключей.
        }
    }
}
