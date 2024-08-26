package com.otus.securehomework.data.biometrics

import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import kotlinx.coroutines.suspendCancellableCoroutine

// Функция `authenticate` для `Class3BiometricAuthPrompt` - выполняет биометрическую аутентификацию с поддержкой криптографии
suspend fun Class3BiometricAuthPrompt.authenticate(
    host: AuthPromptHost, // Контекст хоста для аутентификации (например, Activity или Fragment)
    crypto: BiometricPrompt.CryptoObject? // Криптографический объект, используемый для шифрования или дешифрования (может быть null)
): BiometricPrompt.AuthenticationResult { // Возвращает результат аутентификации (с использованием `BiometricPrompt.AuthenticationResult`)
    return suspendCancellableCoroutine { continuation -> // Приостанавливает выполнение корутины до завершения аутентификации
        val authPrompt = startAuthentication( // Запускает процесс аутентификации с использованием предоставленного хоста и криптообъекта
            host, // Передаем хост, который содержит контекст, необходимый для отображения биометрического промпта
            crypto, // Передаем криптографический объект, если он предоставлен
            Runnable::run, // Используем стандартный механизм выполнения в основном потоке (Runnable::run)
            CoroutineAuthPromptCallback(continuation) // Передаем коллбек, который свяжет результат аутентификации с корутиной
        )

        // Если корутина была отменена, то отменяем процесс аутентификации
        continuation.invokeOnCancellation {
            authPrompt.cancelAuthentication() // Отменяет аутентификацию при отмене корутины
        }
    }
}

// Функция `authenticate` для `Class2BiometricAuthPrompt` - выполняет биометрическую аутентификацию без поддержки криптографии
suspend fun Class2BiometricAuthPrompt.authenticate(
    host: AuthPromptHost, // Контекст хоста для аутентификации
): BiometricPrompt.AuthenticationResult { // Возвращает результат аутентификации (с использованием `BiometricPrompt.AuthenticationResult`)
    return suspendCancellableCoroutine { continuation -> // Приостанавливает выполнение корутины до завершения аутентификации
        val authPrompt = startAuthentication( // Запускает процесс аутентификации с использованием предоставленного хоста
            host, // Передаем хост, который содержит контекст, необходимый для отображения биометрического промпта
            Runnable::run, // Используем стандартный механизм выполнения в основном потоке (Runnable::run)
            CoroutineAuthPromptCallback(continuation) // Передаем коллбек, который свяжет результат аутентификации с корутиной
        )

        // Если корутина была отменена, то отменяем процесс аутентификации
        continuation.invokeOnCancellation {
            authPrompt.cancelAuthentication() // Отменяет аутентификацию при отмене корутины
        }
    }
}
