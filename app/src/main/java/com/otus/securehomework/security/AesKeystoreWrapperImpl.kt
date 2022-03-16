package com.otus.securehomework.security;

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


class AesKeystoreWrapperImpl(
    private val context: Context,
    private val rsaEncryptionService: RsaEncryptionService
) : KeystoreWrapper, BaseKeystoreWrapperImpl() {

    private val sharedPreferences by lazy { context.getSharedPreferences("AesKeystoreWrapper", Context.MODE_PRIVATE) }

    override fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, getSymmetricKey())
        return cipher.iv to cipher.doFinal(data)
    }

    override fun decryptData(encryptedData: ByteArray, ivBytes: ByteArray): ByteArray {
        val cipher = getCipher()
        getCipher().init(Cipher.DECRYPT_MODE, getSymmetricKey(), GCMParameterSpec(128, ivBytes))

        return cipher.doFinal(encryptedData)
    }

    fun getBiometricCryptoObject(): BiometricPrompt.CryptoObject {
        val cipher = getCipher()
        val secretKey = getSymmetricKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return BiometricPrompt.CryptoObject(cipher)
    }

    private fun getCipher(): Cipher = Cipher.getInstance(AES_NOPAD_TRANS)

    private fun getSymmetricKey(): SecretKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isKeyExists(keyStore)) {
                createSymmetricKey()
            }
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            getSymmetricKeyFromPrefs()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createSymmetricKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getSymmetricKeyFromPrefs(): SecretKey {
        val base64Key = sharedPreferences.getString(RSA_KEY_NAME, null)
        return base64Key?.let { base64 ->
            rsaEncryptionService.decrypt(base64)?.let {
                SecretKeySpec(it.toByteArray(Charsets.UTF_8), "AES")
            }
        } ?: createSymmetricKeyInPrefs()
    }

    private fun createSymmetricKeyInPrefs(): SecretKey {
        val key = ByteArray(32)
        SecureRandom().run {
            nextBytes(key)
        }
        val encryptedKey = rsaEncryptionService.encrypt(key)
        sharedPreferences.edit().apply {
            putString(RSA_KEY_NAME, encryptedKey)
            apply()
        }
        return SecretKeySpec(key, "AES")
    }

    companion object {
        const val AES_NOPAD_TRANS = "AES/GCM/NoPadding"
        const val RSA_KEY_NAME = "key_rsa_key_name"
    }
}