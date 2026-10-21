package com.shohankhan.hisaabdesk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF087F5B)
private val LightScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F5E8),
    secondary = Color(0xFF477A66),
    background = Color(0xFFF7FAF8),
    surface = Color.White,
    error = Color(0xFFB3261E)
)

@Composable
fun HisaabDeskTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightScheme, content = content)
}
