package com.bluetalk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluetalkBlue,
    secondary = BluetalkTeal,
    background = BluetalkSurface,
    surface = BluetalkSurface,
    onBackground = BluetalkInk,
    onSurface = BluetalkInk,
)

@Composable
fun BluetalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
