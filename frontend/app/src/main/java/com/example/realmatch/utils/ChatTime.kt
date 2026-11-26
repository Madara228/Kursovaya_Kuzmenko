package com.example.realmatch.utils

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")

private fun parseTime(iso: String?): OffsetDateTime? = iso?.let {
    runCatching { OffsetDateTime.parse(it) }.getOrNull()
}

fun formatMessageTime(iso: String?): String = parseTime(iso)?.format(timeFormatter).orEmpty()

fun formatChatTime(iso: String?): String {
    val time = parseTime(iso) ?: return ""
    val now = OffsetDateTime.now()
    val minutes = Duration.between(time, now).toMinutes()
    return when {
        minutes <= 0 -> "сейчас"
        minutes < 60 -> "${minutes}м"
        time.toLocalDate() == now.toLocalDate() -> time.format(timeFormatter)
        else -> time.format(dateFormatter)
    }
}
