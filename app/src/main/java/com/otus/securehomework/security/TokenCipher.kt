package com.otus.securehomework.security

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface TokenCipher {

    suspend fun decryptToken(token: String): String
    suspend fun encryptTokens(
        tokens: Map<Preferences.Key<String>, String?>
    ): Flow<Preferences.Pair<String>>


}