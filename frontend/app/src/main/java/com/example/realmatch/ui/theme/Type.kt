package com.example.realmatch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.realmatch.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

private fun poppins(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.SemiBold) =
    TextStyle(fontFamily = Poppins, fontWeight = weight, fontSize = size.sp, lineHeight = lineHeight.sp)

private fun inter(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = Inter, fontWeight = weight, fontSize = size.sp, lineHeight = lineHeight.sp)

val RealmatchTypography = Typography(
    displayLarge = poppins(34, 41),
    displayMedium = poppins(30, 37),
    headlineLarge = poppins(30, 37),
    headlineMedium = poppins(24, 31),
    headlineSmall = poppins(22, 29),
    titleLarge = poppins(20, 27),
    titleMedium = poppins(16, 23),
    titleSmall = poppins(14, 20),
    bodyLarge = inter(16, 24),
    bodyMedium = inter(14, 20),
    bodySmall = inter(12, 16),
    labelLarge = inter(16, 22, FontWeight.Medium),
    labelMedium = inter(13, 18, FontWeight.Medium),
    labelSmall = inter(11, 15, FontWeight.Medium),
)
