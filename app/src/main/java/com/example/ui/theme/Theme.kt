package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekRed,
    onPrimary = Color.White,
    primaryContainer = SleekRedDark,
    onPrimaryContainer = Color.White,
    secondary = SleekBlueButton,
    onSecondary = Color.White,
    secondaryContainer = BeaconCyanContainer,
    onSecondaryContainer = SleekNavy,
    tertiary = WarningAmber,
    background = DeepSlate900,
    onBackground = Color.White,
    surface = DeepSlate800,
    onSurface = Color.White,
    surfaceVariant = DeepSlate700,
    onSurfaceVariant = Slate100
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekRed,
    onPrimary = Color.White,
    primaryContainer = SleekCardBlue,
    onPrimaryContainer = SleekCardBlueText,
    secondary = SleekBlueButton,
    onSecondary = Color.White,
    secondaryContainer = SleekCardBlue,
    onSecondaryContainer = SleekCardBlueText,
    tertiary = WarningAmber,
    background = SleekBg,
    onBackground = SleekOnBg,
    surface = SleekGrayCard,
    onSurface = SleekOnBg,
    surfaceVariant = SleekPillBg,
    onSurfaceVariant = SleekMutedText
  )

@Composable
fun SilentSignalTheme(
  darkTheme: Boolean = false, // Default to Sleek Interface light theme
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = SilentSignalTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

