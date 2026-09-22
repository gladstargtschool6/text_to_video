package com.gladstar.texttovideo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

@Composable
fun TextToVideoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = Typography, content = content)
}
