package com.otus.securehomework.data.crypto

import android.util.Base64
import java.math.BigInteger
import java.security.Key
import java.security.MessageDigest
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

private const val RSA_RANSFORMATION = "RSA/ECB/PKCS1Padding"
// Константа, определяющая режим шифрования и заполнения для алгоритма RSA.

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
// Константа, определяющая режим шифрования и заполнения для алгоритма AES.

private const val IV = "3134003223491201"
// Жестко заданный инициализационный вектор (IV), который используется для AES-шифрования.

private const val GCM_IV_LENGTH = 12
// Длина инициализационного вектора (IV) для режима GCM (12 байт), который используется в AES-шифровании.

class Security @Inject constructor() {

    private val FIXED_IV = IV.toByteArray()
    // Преобразование строки IV в байтовый массив, который используется как фиксированный инициализационный вектор.

    /* Хеширование
    https://developer.android.com/reference/kotlin/java/security/MessageDigest
    */
    fun md5(plaintext: CharSequence): String {
        return createHash(plaintext.toString(), "MD5")
    }
    // Функция для вычисления хеша MD5 из входной строки. Использует вспомогательную функцию createHash.

    fun sha256(plaintext: CharSequence): String {
        return createHash(plaintext.toString(), "SHA-256")
    }
    // Функция для вычисления хеша SHA-256 из входной строки. Использует вспомогательную функцию createHash.

    private fun createHash(plaintext: String, type: String): String {
        val md = MessageDigest.getInstance(type)
        // Получение экземпляра MessageDigest для указанного типа (например, "MD5" или "SHA-256").

        val bigInt = BigInteger(1, md.digest(plaintext.toByteArray(Charsets.UTF_8)))
        // Преобразование байтового массива (результата хеширования) в BigInteger.

        return String.format("%032x", bigInt)
        // Возврат хеша в виде шестнадцатеричной строки.
    }

    /* Хеширование одноключевое
    https://developer.android.com/reference/javax/crypto/Mac
    */

    fun hmacMD5(plaintext: CharSequence, key: Key): String {
        return createHash(plaintext.toString(), "HmacMD5", key)
    }
    // Функция для вычисления HMAC с использованием алгоритма MD5. Использует вспомогательную функцию createHash.

    fun hmacSHA256(plaintext: CharSequence, key: Key): String {
        return createHash(plaintext.toString(), "HmacSHA256", key)
    }
    // Функция для вычисления HMAC с использованием алгоритма SHA-256. Использует вспомогательную функцию createHash.


    private fun createHash(plaintext: String, algorithm: String, key: Key): String {
        val mac = Mac.getInstance(algorithm)
        // Получаем экземпляр Mac для указанного алгоритма (например, "HmacMD5" или "HmacSHA256").

        mac.init(key)
        // Инициализируем Mac с использованием переданного ключа.

        val dataByteArray = mac.doFinal(plaintext.toByteArray())
        // Преобразуем входной текст в байтовый массив и выполняем вычисление HMAC.

        val hash = BigInteger(1, dataByteArray)
        // Преобразуем результат вычисления HMAC (байтовый массив) в объект BigInteger.

        return hash.toString()
        // Возвращаем строковое представление полученного хэша.
    }

    fun encryptAes(plainText: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        // Получаем экземпляр Cipher для алгоритма AES с указанным режимом и заполнением (AES/GCM/NoPadding).

        cipher.init(Cipher.ENCRYPT_MODE, key, getInitializationVector())
        // Инициализируем шифрование с использованием переданного ключа и вектора инициализации (IV).

        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        // Выполняем шифрование и получаем зашифрованные данные в виде байтового массива.

        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
        // Кодируем зашифрованные данные в строку Base64 и возвращаем результат.
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        // Создаем байтовый массив для инициализационного вектора с длиной, заданной GCM_IV_LENGTH.

        FIXED_IV.copyInto(iv, 0, GCM_IV_LENGTH)
        // Копируем фиксированный инициализационный вектор (IV) в созданный массив.

        return GCMParameterSpec(128, iv)
        // Возвращаем спецификацию параметра для режима GCM с длиной тега аутентификации 128 бит и инициализационным вектором.
    }

    fun decryptAes(encrypted: String, key: Key): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        // Получаем экземпляр Cipher для алгоритма AES с указанным режимом и заполнением (AES/GCM/NoPadding).

        cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector())
        // Инициализируем дешифрование с использованием переданного ключа и вектора инициализации (IV).

        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        // Декодируем зашифрованные данные из строки Base64 в байтовый массив.

        val decoded = cipher.doFinal(decodedBytes)
        // Выполняем дешифрование и получаем исходные данные в виде байтового массива.

        return String(decoded, Charsets.UTF_8)
        // Преобразуем дешифрованные данные в строку и возвращаем результат.
    }

}