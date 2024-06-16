package com.otus.securehomework.data.source.secure

/**
 * Интерфейс для операций с безопасным хранилищем.
 */
interface TextCipher {

    /**
     * Шифрует переданный текст.
     *
     * @param text Открытый текст для шифрования.
     * @return Зашифрованный текст, закодированный в строку Base64.
     */
    fun encrypt(text: String): String

    /**
     * Расшифровывает переданный зашифрованный текст.
     *
     * @param encryptedText Зашифрованный текст, закодированный в строку Base64.
     * @return Расшифрованный открытый текст.
     */
    fun decrypt(encryptedText: String): String
}
