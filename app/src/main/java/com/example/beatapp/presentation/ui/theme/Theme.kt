package com.example.beatapp.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun BeatAppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = AppColors.Primary,
        secondary = AppColors.Secondary,
        background = AppColors.Background,
        surface = AppColors.Surface,
        error = AppColors.Error,
        onPrimary = AppColors.OnPrimary,
        onSecondary = AppColors.OnSecondary,
        onBackground = AppColors.OnBackground,
        onSurface = AppColors.OnSurface,
        onError = AppColors.OnError,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
