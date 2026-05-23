package com.phonemonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
  primary = AccentBlue,
  onPrimary = White,
  secondary = TextSecondary,
  onSecondary = White,
  background = Background,
  onBackground = TextPrimary,
  surface = CardBg,
  onSurface = TextPrimary,
  error = AccentRed,
  onError = White,
  outline = CardBorder
)

@Composable
fun NotiMonTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = LightScheme, typography = Typography, content = content)
}
