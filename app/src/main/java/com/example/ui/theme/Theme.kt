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

private val LightColorScheme = lightColorScheme(
  primary = VyaparTealPrimary,
  onPrimary = VyaparOnPrimary,
  primaryContainer = VyaparTealPrimaryContainer,
  onPrimaryContainer = VyaparOnPrimaryContainer,
  secondary = VyaparSecondary,
  onSecondary = VyaparOnSecondary,
  secondaryContainer = VyaparSecondaryContainer,
  onSecondaryContainer = VyaparOnSecondaryContainer,
  tertiary = VyaparTertiary,
  onTertiary = VyaparOnTertiary,
  tertiaryContainer = VyaparTertiaryContainer,
  onTertiaryContainer = VyaparOnTertiaryContainer,
  background = VyaparBackground,
  onBackground = VyaparOnBackground,
  surface = VyaparSurface,
  onSurface = VyaparOnSurface,
  surfaceVariant = VyaparSurfaceVariant,
  onSurfaceVariant = VyaparOnSurfaceVariant,
  outline = VyaparOutline
)

private val DarkColorScheme = darkColorScheme(
  primary = VyaparDarkPrimary,
  onPrimary = VyaparDarkOnPrimary,
  primaryContainer = VyaparDarkPrimaryContainer,
  onPrimaryContainer = VyaparDarkOnPrimaryContainer,
  secondary = VyaparDarkSecondary,
  onSecondary = VyaparDarkOnSecondary,
  secondaryContainer = VyaparDarkSecondaryContainer,
  onSecondaryContainer = VyaparDarkOnSecondaryContainer,
  tertiary = VyaparDarkTertiary,
  onTertiary = VyaparDarkOnTertiary,
  tertiaryContainer = VyaparDarkTertiaryContainer,
  onTertiaryContainer = VyaparDarkOnTertiaryContainer,
  background = VyaparDarkBackground,
  onBackground = VyaparDarkOnBackground,
  surface = VyaparDarkSurface,
  onSurface = VyaparDarkOnSurface,
  surfaceVariant = VyaparDarkSurfaceVariant,
  onSurfaceVariant = VyaparDarkOnSurfaceVariant,
  outline = VyaparDarkOutline
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our branded Indian business palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
