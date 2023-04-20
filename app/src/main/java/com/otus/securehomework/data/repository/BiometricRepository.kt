package com.otus.securehomework.data.repository

import android.content.Context

class BiometricRepository(
    context: Context
) {


    private val preferences = context.getSharedPreferences("KeyPreferences", Context.MODE_PRIVATE)

    fun setBiometricState(state: BiometricState){
        preferences.edit().putInt("biometricState", state.ordinal).apply()
    }

    fun getBiometricState(): BiometricState {
        return BiometricState.values()[
            preferences.getInt("biometricState", 0)
        ]
    }

}

enum class BiometricState{
    OFF,
    WEAK,
    STRONG
}