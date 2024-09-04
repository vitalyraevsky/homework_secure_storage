package com.otus.securehomework.data.protection

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class TokenManager @Inject constructor () : ITokenManager {

    companion object {
        const val TRANSFORMATION_AES_GCM = "AES/GCM/NoPadding"
        const val TRANSFORMATION_AES_CBC = "AES/CBC/PKCS5Padding"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val AES = "AES"
        const val AES_KEY_SIZE = 256
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        // Проверяем, существует ли ключ
        val existingKey = keyStore.getKey(AES, null) as SecretKey?
        if (existingKey != null) {
            return existingKey
        }

        // Генерируем новый ключ с использованием KeyGenParameterSpec
        val keyGenerator = KeyGenerator.getInstance(AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = android.security.keystore.KeyGenParameterSpec.Builder(
            AES, // Алиас ключа
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    override fun encryptToken(token: String): String {
        return encryptTokenApi23(token)
    }

    override fun decryptToken(encryptedToken: String): String? {
        return decryptTokenApi23(encryptedToken)
    }

    private fun encryptTokenApi23(token: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(token.toByteArray(Charsets.UTF_8))

        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        return "$ivBase64:$encryptedBase64"
    }

    private fun decryptTokenApi23(encryptedToken: String): String {
        val (ivBase64, encryptedBase64) = encryptedToken.split(":")
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val encryptedData = Base64.decode(encryptedBase64, Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val originalData = cipher.doFinal(encryptedData)
        return String(originalData, Charsets.UTF_8)
    }
}