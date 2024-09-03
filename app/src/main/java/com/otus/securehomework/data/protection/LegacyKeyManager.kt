package com.otus.securehomework.data.protection

import android.content.Context
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class LegacyKeyManager @Inject constructor (
    private val context: Context
) {
    companion object {
        const val PREFS_NAME = "key_prefs"
        const val SECRET_KEY_ALIAS = "encrypted_secret_key"
        const val SALT = "random_salt_value"
        const val ITERATION_COUNT = 1000
        const val KEY_LENGTH = 256
        const val TRANSFORMATION_AES_CBC = "AES/CBC/PKCS5Padding"
        const val TRANSFORMATION_AES = "AES"
        const val AES_KEY_SIZE = 256
    }

    private fun generateMasterKey(password: String): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(password.toCharArray(), SALT.toByteArray(), ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, TRANSFORMATION_AES)
    }

    private fun encryptSecretKey(secretKey: SecretKey, password: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES_CBC)
        val masterKey = generateMasterKey(password)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val encryptedKey = cipher.doFinal(secretKey.encoded)

        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedKeyBase64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
        return "$ivBase64:$encryptedKeyBase64"
    }

    private fun decryptSecretKey(encryptedData: String, password: String): SecretKey {
        val (ivBase64, encryptedKeyBase64) = encryptedData.split(":")
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION_AES_CBC)
        val masterKey = generateMasterKey(password)
        val spec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

        val decodedKey = cipher.doFinal(encryptedKey)
        return SecretKeySpec(decodedKey, TRANSFORMATION_AES)
    }

    private fun saveSecretKey(
        secretKey: SecretKey,
        password: String
    ) {
        val encryptedKey = encryptSecretKey(secretKey, password)
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(SECRET_KEY_ALIAS, encryptedKey)
            apply()
        }
    }

    private fun getSecretKey(password: String): SecretKey? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedKey = sharedPreferences.getString(SECRET_KEY_ALIAS, null)
        return encryptedKey?.let { decryptSecretKey(it, password) }
    }

    fun getOrGenerateLegacyKey(): SecretKey {
        //Думаю надо сюда передавать какие то данные пользователя
        val password = "user_defined_password_or_pin"
        return getSecretKey(password) ?: generateLegacyKey().also {
            saveSecretKey(it, password)
        }
    }

    private fun generateLegacyKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(TRANSFORMATION_AES)
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }

    fun encryptTokenLegacy(
        token: String,
        secretKey: SecretKey
    ): String {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES_CBC)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(token.toByteArray(Charsets.UTF_8))

        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        return "$ivBase64:$encryptedBase64"
    }

    fun decryptTokenLegacy(
        encryptedToken: String,
        secretKey: SecretKey
    ): String {
        val (ivBase64, encryptedBase64) = encryptedToken.split(":")
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val encryptedData = Base64.decode(encryptedBase64, Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION_AES_CBC)
        val spec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val originalData = cipher.doFinal(encryptedData)
        return String(originalData, Charsets.UTF_8)
    }
}