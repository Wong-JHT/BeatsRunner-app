package com.beatrunner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun BeatRunnerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            // Large speed display
            displayLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 96.sp,
                letterSpacing = (-1.5).sp
            ),
            displayMedium = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp
            ),
            // Headings
            headlineLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            headlineMedium = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            ),
            // Body text
            bodyLarge = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            // Labels
            labelLarge = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.1.sp
            )
        ),
        content = content
    )
}
