package note.notes.savenote.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

fun legacyColors(
    primary: Color = Color(0xFFBB86FC),
    primaryVariant: Color = Color(0xFF3700B3),
    secondary: Color = Color(0xFF03DAC6),
    secondaryVariant: Color = secondary,
    background: Color = Color(0xFF121212),
    surface: Color = Color(0xFF121212),
    error: Color = Color(0xFFCF6679),
    onPrimary: Color = Color.Black,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.White,
    onSurface: Color = Color.White,
    onError: Color = Color.Black
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)




private val LegacyColorPalette = legacyColors(
    background = LegacyBackground,
    secondary = LegacySecondary,
    primary = LegacyPrimary,
    primaryVariant = LegacyPrimaryVariant,
    surface = LegacySurface,
    onSurface = LegacyOnSurface,
    onSecondary = LegacyOnSecondary,
    onError = LegacyOnError,
    onBackground = LegacyOnBackground,
    secondaryVariant = LegacySecondaryVariant
)

private val DarkColorPalette = darkColors(
    background = DarkBackground,
    secondary = DarkSecondary,
    primary = DarkPrimary,
    primaryVariant = DarkPrimaryVariant,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSecondary = DarkOnSecondary,
    onError = DarkOnError,
    onBackground = DarkOnBackground,
    secondaryVariant = DarkSecondaryVariant
)

private val LightColorPalette = lightColors(
    background = LightBackground,
    secondary = LightSecondary,
    primary = LightPrimary,
    primaryVariant = LightPrimaryVariant,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSecondary = LightOnSecondary,
    onError = LightonError,
    onBackground = LightOnBackground,
    secondaryVariant = LightSecondaryVariant
)




val BackgroundSample = Color(0xFF1a1a1a)
val SecondarySample = Color(0xFF212121)
val OnBackgroundSample = Color(0xFF373737)
val SecondaryVariantSample = Color(0xFF4d4d4d)
val OnSurfaceSample = Color(0xFF7a7a7a)
val OnSecondarySample = Color(0xFFa6a6a6)
val PrimaryVariantSample = Color(0XFFD3D3D3)
val TypeSample = Color(0XFFF5F3F4)
val PrimarySample = Color(0xFFda9642)
val onErrorSample = Color(0xFFc72a35)

@Composable
fun SaveNoteTheme(
    legacyColour: Int,
    content: @Composable () -> Unit,
) {
    val systemController = rememberSystemUiController()

    val colors = when (legacyColour) {
        1 -> LegacyColorPalette
        2 -> DarkColorPalette
        else -> LightColorPalette
    }

    systemController.setStatusBarColor(
        color = colors.background,
        darkIcons = legacyColour > 2
    )

    systemController.setNavigationBarColor(
        color =  Color.Transparent,
        darkIcons = legacyColour > 2
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}