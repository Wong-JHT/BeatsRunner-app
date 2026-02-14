package com.beatrunner.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Primary Colors - Deep Blue/Purple gradient
val PrimaryBlue = Color(0xFF2196F3)
val PrimaryPurple = Color(0xFF9C27B0)
val PrimaryDark = Color(0xFF1565C0)

// Accent Colors
val AccentGreen = Color(0xFF00E676)
val AccentOrange = Color(0xFFFF9100)
val AccentRed = Color(0xFFFF1744)

// Background Colors
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceLight = Color(0xFF2C2C2C)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val TextDisabled = Color(0xFF6B6B6B)

// Status Colors
val SuccessColor = Color(0xFF4CAF50)
val ErrorColor = Color(0xFFF44336)
val WarningColor = Color(0xFFFFA726)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryPurple,
    tertiary = AccentGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorColor,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextPrimary
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryPurple,
    tertiary = AccentGreen,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = ErrorColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onError = Color.White
)
