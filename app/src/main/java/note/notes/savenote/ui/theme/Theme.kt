package note.notes.savenote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    background = Primary,
    secondary = Secondary,
    primary = Highlight,
    primaryVariant = Accent,
    surface = Type,
    onSurface = Faded,
    onSecondary = Divider,
    onError = Warning,
    onBackground = PrimaryFaded,
    secondaryVariant = SecondaryVariant

)

private val LightColorPalette = lightColors(

    background = Primary,
    secondary = Secondary,
    primary = Highlight,
    primaryVariant = Accent,
    surface = Type,
    onSurface = Faded,
    onSecondary = Divider,
    onError = Warning,
    onBackground = PrimaryFaded,
    secondaryVariant = SecondaryVariant

)

val Highlight1 = Color(0xFFda9642)
val Type1 = Color(0XFFF5F3F4)
val Accent1 = Color(0XFFD3D3D3)
val Secondary1 = Color(0xFF212529)
val Primary1 = Color(0xFF161A1D)
val Faded1 = Color(0xFF6C757D)
val Divider1 = Color(0xFFADB5BD)
val Warning1 = Color(0xFFc72a35)
val PrimaryFaded1 = Color(0xFF282E33)
val SecondaryVariant1 = Color(0xFF464D54)

@Composable
fun SaveNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}