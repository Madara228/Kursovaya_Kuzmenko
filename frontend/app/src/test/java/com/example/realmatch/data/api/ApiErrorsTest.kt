package com.example.realmatch.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException

class ApiErrorsTest {

    private fun httpError(code: Int, body: String) =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    @Test
    fun `network failure is reported as no connection`() {
        assertEquals(
            "Нет соединения с сервером. Проверьте, что бэкенд запущен.",
            UnknownHostException("10.0.2.2").toUserMessage(),
        )
    }

    @Test
    fun `invalid esia credentials are translated`() {
        val message = httpError(
            401,
            """{"error":"INVALID_ESIA_CREDENTIALS","message":"Invalid ESIA login or password","code":401}""",
        ).toUserMessage()

        assertEquals("Неверный логин или пароль", message)
    }

    @Test
    fun `underage user error is translated`() {
        val message = httpError(
            403,
            """{"error":"USER_UNDER_18","message":"User must be at least 18 years old","code":403}""",
        ).toUserMessage()

        assertEquals("Сервис доступен только с 18 лет", message)
    }

    @Test
    fun `unknown http error includes status code`() {
        val message = httpError(500, """{"error":"Internal","code":500}""").toUserMessage()

        assertEquals("Ошибка сервера (500)", message)
    }

    @Test
    fun `invite errors are translated`() {
        val expired = httpError(400, """{"error":"INVITE_EXPIRED","code":400}""").toUserMessage()
        val accepted = httpError(409, """{"error":"INVITE_ALREADY_ACCEPTED","code":409}""").toUserMessage()
        val wrong = httpError(403, """{"error":"WRONG_RESPONDER","code":403}""").toUserMessage()

        assertEquals("Срок действия приглашения истёк", expired)
        assertEquals("Промокод по этому приглашению уже получен", accepted)
        assertEquals("Это приглашение адресовано другому пользователю", wrong)
    }
}
