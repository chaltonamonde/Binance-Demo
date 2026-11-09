package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BinanceYellow,
    onPrimary = BinanceDarkBg,
    primaryContainer = BinanceDarkCard,
    onPrimaryContainer = BinanceYellow,
    secondary = BinanceGold,
    onSecondary = BinanceDarkBg,
    secondaryContainer = BinanceDarkElevated,
    onSecondaryContainer = BinanceTextPrimary,
    tertiary = BinanceGreen,
    onTertiary = BinanceDarkBg,
    background = BinanceDarkBg,
    onBackground = BinanceTextPrimary,
    surface = BinanceDarkBg,
    onSurface = BinanceTextPrimary,
    surfaceVariant = BinanceDarkCard,
    onSurfaceVariant = BinanceTextSecondary,
    outline = BinanceDarkBorder,
    error = BinanceRed,
    onError = BinanceDarkBg
)

private val LightColorScheme = DarkColorScheme // Binance app defaults to pro dark theme for trading

@Composable
fun BinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
