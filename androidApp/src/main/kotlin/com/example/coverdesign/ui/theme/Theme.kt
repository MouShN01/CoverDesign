package com.example.coverdesign.ui.theme

// androidApp/ui/theme/Theme.kt
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// ── Light color scheme ──────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary           = PrimaryMid,
    onPrimary         = NeutralWhite,
    primaryContainer  = PrimaryLight,
    onPrimaryContainer = PrimaryDark,

    secondary         = SecondaryMid,
    onSecondary       = NeutralWhite,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,

    tertiary          = AccentAmber,
    onTertiary        = NeutralWhite,

    background        = NeutralOffWhite,
    onBackground      = NeutralDark,

    surface           = NeutralWhite,
    onSurface         = NeutralDark,
    surfaceVariant    = NeutralLight,
    onSurfaceVariant  = NeutralMid,

    outline           = NeutralLight,
    outlineVariant    = NeutralLight,

    error             = ErrorRed,
    onError           = NeutralWhite,
    errorContainer    = ErrorRedLight,
    onErrorContainer  = ErrorRed
)

// ── Dark color scheme ───────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary           = PrimaryLight,
    onPrimary         = PrimaryDark,
    primaryContainer  = PrimaryMid,
    onPrimaryContainer = PrimaryLight,

    secondary         = SecondaryLight,
    onSecondary       = SecondaryDark,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryLight,

    tertiary          = AccentAmber,
    onTertiary        = NeutralDark,

    background        = NeutralDark,
    onBackground      = NeutralOffWhite,

    surface           = SurfaceDark,
    onSurface         = OnSurfaceDark,
    surfaceVariant    = SurfaceVariantDark,
    onSurfaceVariant  = NeutralMid,

    outline           = SurfaceVariantDark,
    outlineVariant    = SurfaceVariantDark,

    error             = ErrorRed,
    onError           = NeutralWhite,
    errorContainer    = Color(0xFF501313),
    onErrorContainer  = ErrorRed
)

// ── AppTheme ────────────────────────────────────────────────────────────────
@Composable
fun AppTheme(
    darkTheme        : Boolean = isSystemInDarkTheme(),
    dynamicColor     : Boolean = false,   // disabled — keeps brand identity
    content          : @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You dynamic color — Android 12+ only, opt-in
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // sync status bar color with theme TODO enableEdgeToEdge() in MainActivity
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color         = colorScheme.background,
            darkIcons     = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}