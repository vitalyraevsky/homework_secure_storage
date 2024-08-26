package com.otus.securehomework.data.biometrics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE_PROVIDER = "AndroidKeyStore" // Константа, определяющая провайдера для AndroidKeyStore
private const val AUTH_TAG_SIZE = 128 // Размер аутентификационного тега для шифрования GCM
private const val KEY_SIZE = 256 // Размер ключа в битах (256 бит)

@RequiresApi(Build.VERSION_CODES.M)
private const val TRANSFORMATION = "$KEY_ALGORITHM_AES/" +
        "$BLOCK_MODE_GCM/" +
        ENCRYPTION_PADDING_NONE // Строка, которая определяет алгоритм шифрования (AES в режиме GCM без заполнения)

class BiometricCipher( // Класс для шифрования и дешифрования с использованием биометрии
    private val applicationContext: Context // Контекст приложения, используемый для получения ресурсов, ключей и др.
) {
    private val keyAlias by lazy { "${applicationContext.packageName}.biometricKey" } // Создание алиаса ключа на основе имени пакета приложения

    // Метод для получения объекта BiometricPrompt.CryptoObject, который будет использоваться для шифрования
    @RequiresApi(Build.VERSION_CODES.M)
    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance(TRANSFORMATION).apply { // Создание объекта шифрования
            init(Cipher.ENCRYPT_MODE, getOrCreateKey()) // Инициализация шифра в режиме шифрования с использованием ключа
        }

        return BiometricPrompt.CryptoObject(encryptor) // Возврат объекта шифрования, обернутого в BiometricPrompt.CryptoObject
    }

    // Метод для получения объекта BiometricPrompt.CryptoObject, который будет использоваться для дешифрования
    @RequiresApi(Build.VERSION_CODES.M)
    fun getDecryptor(iv: ByteArray): BiometricPrompt.CryptoObject {
        val decryptor = Cipher.getInstance(TRANSFORMATION).apply { // Создание объекта шифрования
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(AUTH_TAG_SIZE, iv)) // Инициализация шифра в режиме дешифрования с использованием ключа и вектора инициализации (IV)
        }
        return BiometricPrompt.CryptoObject(decryptor) // Возврат объекта дешифрования, обернутого в BiometricPrompt.CryptoObject
    }

    // Метод для шифрования текста с использованием предоставленного объекта шифрования (Cipher)
    fun encrypt(plaintext: String, encryptor: Cipher): EncryptedEntity {
        require(plaintext.isNotEmpty()) { "Plaintext cannot be empty" } // Проверка, что текст не пустой
        val ciphertext = encryptor.doFinal(plaintext.toByteArray()) // Шифрование текста
        return EncryptedEntity(
            ciphertext, // Зашифрованные данные
            encryptor.iv // Вектор инициализации (IV), который необходим для дешифрования
        )
    }

    // Метод для дешифрования зашифрованных данных с использованием предоставленного объекта дешифрования (Cipher)
    fun decrypt(ciphertext: ByteArray, decryptor: Cipher): String {
        val plaintext = decryptor.doFinal(ciphertext) // Дешифрование данных
        return String(plaintext, Charsets.UTF_8) // Преобразование дешифрованных байтов в строку
    }

    // Метод для получения или создания секретного ключа (SecretKey) в AndroidKeyStore
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKey(): SecretKey {
        val keystore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { // Получение экземпляра KeyStore для AndroidKeyStore
            load(null) // Загрузка KeyStore
        }

        // Если ключ уже существует, он возвращается
        keystore.getKey(keyAlias, null)?.let { key ->
            return key as SecretKey // Приведение и возврат ключа в виде SecretKey
        }

        // Если ключ не существует, создаем новый
        val keySpec = KeyGenParameterSpec.Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE_GCM) // Устанавливаем режим блоков GCM
            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE) // Без заполнения
            .setKeySize(KEY_SIZE) // Устанавливаем размер ключа
            .setUserAuthenticationRequired(true) // Требуется аутентификация пользователя для использования ключа
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true) // Ключ можно использовать только если устройство разблокировано

                    val hasStringBox = applicationContext
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE) // Проверка наличия StrongBox для безопасного хранения ключей

                    setIsStrongBoxBacked(hasStringBox) // Если StrongBox доступен, ключ будет в нем храниться
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG) // Требование аутентификации с использованием биометрии
                }
            }.build()

        // Создаем генератор ключей и инициализируем его с использованием созданного спецификатора ключа (keySpec)
        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
            init(keySpec)
        }

        return keyGenerator.generateKey() // Генерация и возврат нового ключа
    }
}
