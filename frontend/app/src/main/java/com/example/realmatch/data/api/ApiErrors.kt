package com.example.realmatch.data.api

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private data class ApiErrorBody(
    val error: String? = null,
    val message: String? = null,
    val code: Int? = null,
)

private val gson = Gson()

private fun HttpException.errorBody(): ApiErrorBody? = try {
    response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
        ?.let { gson.fromJson(it, ApiErrorBody::class.java) }
} catch (_: Exception) {
    null
}

private fun HttpException.userMessage(): String {
    val body = errorBody()
    return when (body?.error) {
        "INVALID_ESIA_CREDENTIALS", "Unauthorized" -> "Неверный логин или пароль"
        "USER_UNDER_18" -> "Сервис доступен только с 18 лет"
        "USER_BLOCKED" -> "Аккаунт заблокирован"
        "PHOTO_LIMIT_REACHED" -> "Можно загрузить не больше 6 фото"
        "INVITE_EXPIRED" -> "Срок действия приглашения истёк"
        "INVITE_ALREADY_ACCEPTED" -> "Промокод по этому приглашению уже получен"
        "WRONG_RESPONDER" -> "Это приглашение адресовано другому пользователю"
        "PARTNER_ACCESS_REQUIRED" -> "Действие доступно только партнёрам"
        "Unprocessable Entity" -> "Проверьте введённые данные"
        else -> "Ошибка сервера (${code()})"
    }
}

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException, is ConnectException, is SocketTimeoutException ->
        "Нет соединения с сервером. Проверьте, что бэкенд запущен."

    is IOException -> "Проблема с сетью. Попробуйте ещё раз."
    is HttpException -> userMessage()
    else -> "Что-то пошло не так. Попробуйте ещё раз."
}
