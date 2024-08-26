package com.otus.securehomework.data.biometrics

import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.AuthPromptErrorException
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resumeWithException

// Класс CoroutineAuthPromptCallback реализует обработку событий, возникающих при биометрической аутентификации.
// Он принимает continuation, которая позволяет возобновить выполнение сопрограммы (coroutine) с результатом аутентификации.
internal class CoroutineAuthPromptCallback(
    private val continuation: CancellableContinuation<BiometricPrompt.AuthenticationResult>
) : AuthPromptCallback() { // Наследуется от AuthPromptCallback для реализации его методов

    // Этот метод вызывается, когда происходит ошибка аутентификации.
    // Параметры включают:
    // - activity: Активность, связанная с этим событием (может быть null).
    // - errorCode: Код ошибки, указывающий на тип произошедшей ошибки.
    // - errString: Сообщение об ошибке.
    override fun onAuthenticationError(
        activity: FragmentActivity?,
        errorCode: Int,
        errString: CharSequence
    ) {
        // Сообщаем continuation, что аутентификация завершилась с ошибкой,
        // передавая исключение AuthPromptErrorException с кодом ошибки и сообщением.
        continuation.resumeWithException(AuthPromptErrorException(errorCode, errString))
    }

    // Этот метод вызывается при успешной аутентификации.
    // Параметры включают:
    // - activity: Активность, связанная с этим событием (может быть null).
    // - result: Результат успешной аутентификации, включающий данные об аутентифицированном пользователе.
    override fun onAuthenticationSucceeded(
        activity: FragmentActivity?,
        result: BiometricPrompt.AuthenticationResult
    ) {
        // Сообщаем continuation, что аутентификация завершилась успешно,
        // передавая результат в continuation как успешный результат (Result.success).
        continuation.resumeWith(Result.success(result))
    }

    // Этот метод вызывается, если аутентификация не удалась, но не является ошибкой (например, отпечаток не распознан).
    // В этом примере он не делает ничего (Stub).
    override fun onAuthenticationFailed(activity: FragmentActivity?) {
        // Никаких действий не предпринимается, так как этот метод не влияет на продолжение сопрограммы.
        // Метод может быть использован для обновления UI или логирования.
    }
}