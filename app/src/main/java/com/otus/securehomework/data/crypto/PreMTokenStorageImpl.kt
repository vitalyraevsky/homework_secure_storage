package com.otus.securehomework.data.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.constraintlayout.helper.widget.Flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class PreMTokenStorageImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
    ): SecuredTokenStorage {

    private val keyProvider = "AndroidKeyStore"
    private val keyAlias = "postMKeyAlias"

    private val preferences = context.getSharedPreferences("KeyPreferences", Context.MODE_PRIVATE)

    private val keyStore by lazy {
        KeyStore.getInstance(keyProvider).apply {
            load(null)
        }
    }

    private fun encryptAes(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivParams = IvParameterSpec(iv())
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivParams)
        val encodedBytes = cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    private fun decryptAes(encrypted: String?): String? {
        encrypted ?: return null
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivParams = IvParameterSpec(iv())
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParams)
        val base64Decoded = Base64.decode(encrypted, Base64.NO_WRAP)
        return String(cipher.doFinal(base64Decoded), Charset.forName("UTF-8"))
    }

    override suspend fun saveAccessToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let {
                preferences[stringPreferencesKey("key_access_token")] = encryptAes(it)
            }
        }
    }

    override suspend fun saveRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let {
                preferences[stringPreferencesKey("key_refresh_token")] = encryptAes(it)
            }
        }
    }

    override fun getAccessToken(): kotlinx.coroutines.flow.Flow<String?> {
        return dataStore.data.map { preferences ->
            decryptAes(preferences[stringPreferencesKey("key_access_token")])
        }
    }

    override fun getRefreshToken(): kotlinx.coroutines.flow.Flow<String?> {
        return dataStore.data.map { preferences ->
            decryptAes(preferences[stringPreferencesKey("key_refresh_token")])
        }
    }

    private fun getSecretKey(): SecretKey {
        val encryptedKey = getKeyFromPreferences() ?: return generateSecretKey()
        val decryptedKey = Base64.decode(encryptedKey, Base64.NO_WRAP)
        val key = rsaDecryptKey(decryptedKey)

        return SecretKeySpec(key, "AES")
    }

    private fun getKeyFromPreferences(): String? {
        return preferences.getString("AESKeyEncrypted", null)
    }

    private fun generateSecretKey(): SecretKey{
        val key = ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }

        val encryptedKeyBase64encoded = Base64.encodeToString(rsaEncryptKey(key), Base64.NO_WRAP)

        preferences.edit()
            .putString("AESKeyEncrypted", encryptedKeyBase64encoded)
            .apply()

        return SecretKeySpec(key, "AES")
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)

    }

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun getRsaPublicKey(): PublicKey {
        val publicKey = keyStore.getCertificate(keyAlias)?.publicKey
        return publicKey ?: generateRSAKeyPair().public
    }

    private fun getRsaPrivateKey(): PrivateKey {
        val privateKey = keyStore.getKey(keyAlias, null) as? PrivateKey
        return privateKey ?: generateRSAKeyPair().private
    }

    private fun generateRSAKeyPair(): KeyPair {

        val start: Calendar = Calendar.getInstance()
        val end = Calendar.getInstance().apply { add(Calendar.YEAR, 25) }

        val parameterSpec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(keyAlias)
            .setSubject(X500Principal("CN=RSASecret"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        keyPairGenerator.initialize(parameterSpec)

        return keyPairGenerator.generateKeyPair()
    }


    @SuppressLint("HardwareIds")
    private fun iv(): ByteArray {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return id.toByteArray(Charset.forName("UTF-8"))
    }

}