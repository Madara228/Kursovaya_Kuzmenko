package com.example.realmatch.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun parseOfferDateInput(input: String): LocalDate? =
    runCatching { LocalDate.parse(input.trim(), inputFormatter) }.getOrNull()

fun offerDateInputToIso(input: String): String? = parseOfferDateInput(input)?.toString()

fun offerDateIsoToInput(iso: String): String =
    runCatching { LocalDate.parse(iso).format(inputFormatter) }.getOrDefault(iso)

fun formatOfferDate(iso: String): String = offerDateIsoToInput(iso)
