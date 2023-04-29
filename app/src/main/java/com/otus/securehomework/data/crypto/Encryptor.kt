package com.otus.securehomework.data.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.otus.securehomework.data.crypto.key_manager.KeyManager
import com.otus.securehomework.data.enums.KeyManagerType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Encryptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: KeyManager
) {

    private val ivSpec by lazy { getInitializationVector() }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivSpec)
        val encodedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encrypted: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivSpec)
        val encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decodedBytes = cipher.doFinal(encodedBytes)
        return String(decodedBytes, Charsets.UTF_8)
    }

    private fun getSecretKey(): SecretKey {
        val keyName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ABOVE_M_KEY
        else
            BELOW_M_KEY
        return keyManager.getSecretKey(keyName, KeyManagerType.ENCRYPTION)
    }

    @SuppressLint("HardwareIds")
    private fun getInitializationVector(): AlgorithmParameterSpec {
        val deviceID = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return IvParameterSpec(deviceID.toByteArray())
    }

    companion object {
        private const val AES_MODE = "AES/CBC/PKCS7Padding"
        private const val ABOVE_M_KEY = "AES_KEY"
        private const val BELOW_M_KEY = "RSA_KEY"
    }

}