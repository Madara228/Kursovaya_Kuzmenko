package com.example.realmatch.utils

import com.example.realmatch.BuildConfig
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

fun calculateAge(birthDate: String): Int? = runCatching {
    Period.between(LocalDate.parse(birthDate), LocalDate.now()).years
}.getOrNull()

fun formatBirthDate(birthDate: String): String? = runCatching {
    LocalDate.parse(birthDate).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")))
}.getOrNull()

fun genderLabel(gender: String): String = if (gender == "F") "Женщина" else "Мужчина"

/** Относительный путь из API (/media/...) превращаем в полный URL для Coil. */
fun mediaUrl(path: String?): String? = when {
    path.isNullOrBlank() -> null
    path.startsWith("http") -> path
    path.startsWith("/") -> BuildConfig.MEDIA_BASE_URL + path
    else -> BuildConfig.MEDIA_BASE_URL + "/" + path
}
