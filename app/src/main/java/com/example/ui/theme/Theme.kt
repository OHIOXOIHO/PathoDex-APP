package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * تم روشن بر پایه‌ی accent انتخاب‌شده و پالت ساختاری «میدنایت رز / بنفش».
 *
 * surfaceTint عمداً Transparent است تا سطوح دقیقاً همان رنگ خواسته‌شده بمانند
 * و پله‌ی عمق با رنگ کارت و حاشیه ساخته شود، نه با یک پوشش بنفش روی همه‌چیز.
 */
private fun lightSchemeFor(accent: ThemeAccent, p: PdPalette) = lightColorScheme(
    primary = accent.light.primary,
    onPrimary = accent.light.onPrimary,
    primaryContainer = accent.light.container,
    onPrimaryContainer = accent.light.onContainer,
    secondary = accent.light.primary,
    onSecondary = accent.light.onPrimary,
    secondaryContainer = accent.light.container,
    onSecondaryContainer = accent.light.onContainer,
    tertiary = p.success,
    onTertiary = p.onSuccess,
    tertiaryContainer = p.successContainer,
    onTertiaryContainer = p.onSuccessContainer,
    background = p.background,
    onBackground = p.onBackground,
    surface = p.surface,
    onSurface = p.onSurface,
    surfaceVariant = p.surfaceVariant,
    onSurfaceVariant = p.onSurfaceVariant,
    surfaceTint = Color.Transparent,
    inverseSurface = p.textPrimary,
    inverseOnSurface = p.background,
    error = p.error,
    onError = p.onError,
    errorContainer = p.errorContainer,
    onErrorContainer = p.onErrorContainer,
    outline = p.border,
    outlineVariant = p.border.copy(alpha = 0.6f),
    scrim = p.scrim
)

/** تم تیره «میدنایت رز / بنفش» با سطوح سه‌پله و متن نرم. */
private fun darkSchemeFor(accent: ThemeAccent, p: PdPalette) = darkColorScheme(
    primary = accent.dark.primary,
    onPrimary = accent.dark.onPrimary,
    primaryContainer = accent.dark.container,
    onPrimaryContainer = accent.dark.onContainer,
    secondary = accent.dark.primary,
    onSecondary = accent.dark.onPrimary,
    secondaryContainer = accent.dark.container,
    onSecondaryContainer = accent.dark.onContainer,
    tertiary = p.success,
    onTertiary = p.onSuccess,
    tertiaryContainer = p.successContainer,
    onTertiaryContainer = p.onSuccessContainer,
    background = p.background,
    onBackground = p.onBackground,
    surface = p.surface,
    onSurface = p.onSurface,
    surfaceVariant = p.surfaceVariant,
    onSurfaceVariant = p.onSurfaceVariant,
    surfaceTint = Color.Transparent,
    inverseSurface = p.textPrimary,
    inverseOnSurface = p.background,
    error = p.error,
    onError = p.onError,
    errorContainer = p.errorContainer,
    onErrorContainer = p.onErrorContainer,
    outline = p.border,
    outlineVariant = p.border.copy(alpha = 0.6f),
    scrim = p.scrim
)

private val PdTypographyShapes = Shapes(
    extraSmall = PdShapes.xs,
    small = PdShapes.sm,
    medium = PdShapes.md,
    large = PdShapes.lg,
    extraLarge = PdShapes.xl
)

@Composable
fun PathodexTheme(
    themeMode: String = "system", // "system", "dark", "light"
    accentId: String = DefaultAccentId,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    val accent = accentById(accentId)
    val palette = if (isDark) darkPdPalette() else lightPdPalette()
    val colorScheme = if (isDark) darkSchemeFor(accent, palette) else lightSchemeFor(accent, palette)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // لبه‌به‌لبه: نوارهای سیستم شفاف تا محتوای برنامه زیر آن‌ها بگذرد
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalPdPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = PdTypographyShapes,
            content = content
        )
    }
}
