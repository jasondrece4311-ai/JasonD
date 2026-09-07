package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PriceTagColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.Black,
  primaryContainer = EmeraldDark,
  onPrimaryContainer = EmeraldLight,
  secondary = AmberAccent,
  onSecondary = Color.Black,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = AmberAccent,
  tertiary = CyanHUD,
  onTertiary = Color.Black,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkSurfaceBorder,
  error = RedAccent
)

@Composable
fun PriceTagTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = PriceTagColorScheme,
    typography = Typography,
    content = content
  )
}

