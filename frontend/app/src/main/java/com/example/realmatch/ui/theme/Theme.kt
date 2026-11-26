package com.example.realmatch.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RealmatchColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleSoft,
    onPrimaryContainer = Purple,
    secondary = Purple,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = PurpleSoftAlt,
    onSurfaceVariant = TextSecondary,
    outline = PurpleBorder,
    outlineVariant = PurpleBorder,
    error = Error,
)

private val RealmatchShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Дизайн только светлый, тёмную тему не делаем
@Composable
fun RealmatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RealmatchColors,
        typography = RealmatchTypography,
        shapes = RealmatchShapes,
        content = content,
    )
}
