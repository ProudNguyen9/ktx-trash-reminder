package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AppPrimaryContainer,
    onPrimary = AppOnPrimaryContainer,
    primaryContainer = AppPrimary,
    onPrimaryContainer = AppOnPrimary,
    secondary = AppSecondaryContainer,
    onSecondary = AppOnSecondaryContainer,
    secondaryContainer = AppSecondary,
    onSecondaryContainer = AppOnSecondary,
    tertiary = AppTertiaryContainer,
    onTertiary = AppOnTertiaryContainer,
    tertiaryContainer = AppTertiary,
    onTertiaryContainer = AppOnTertiary,
    background = AppOnBackground,
    onBackground = AppBackground,
    surface = AppOnSurface,
    onSurface = AppSurface,
    surfaceVariant = AppOnSurfaceVariant,
    onSurfaceVariant = AppSurfaceVariant,
    outline = AppOutlineVariant,
    outlineVariant = AppOutline,
    error = AppErrorContainer,
    onError = AppOnErrorContainer,
    errorContainer = AppError,
    onErrorContainer = AppOnError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    primaryContainer = AppPrimaryContainer,
    onPrimaryContainer = AppOnPrimaryContainer,
    secondary = AppSecondary,
    onSecondary = AppOnSecondary,
    secondaryContainer = AppSecondaryContainer,
    onSecondaryContainer = AppOnSecondaryContainer,
    tertiary = AppTertiary,
    onTertiary = AppOnTertiary,
    tertiaryContainer = AppTertiaryContainer,
    onTertiaryContainer = AppOnTertiaryContainer,
    background = AppBackground,
    onBackground = AppOnBackground,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    outline = AppOutline,
    outlineVariant = AppOutlineVariant,
    error = AppError,
    onError = AppOnError,
    errorContainer = AppErrorContainer,
    onErrorContainer = AppOnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
}
