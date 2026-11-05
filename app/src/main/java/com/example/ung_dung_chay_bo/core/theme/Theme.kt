package com.example.ung_dung_chay_bo.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SportOrange,
    onPrimary = PureWhite,
    primaryContainer = SportOrangeSoft,
    onPrimaryContainer = SportOrangeDark,
    secondary = TextSecondary,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = PureWhite,
    outline = LightBorder
)

@Composable
fun UngDungChayBoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
