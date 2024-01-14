package note.notes.savenote.Utils

import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun HighlightedText(
    text: String,
    selectedString: String,
    maxLines: Int,
    fontSize: TextUnit,
    colour: Color = colors.primaryVariant
) {

    val color = colors.primary.copy(0.4f)

    val annotatedText = remember(text, selectedString) {
        AnnotatedString.Builder(text).apply {
            if (selectedString.isNotEmpty()){
                val regex = selectedString.lowercase().toRegex()
                val matches = regex.findAll(text.lowercase())
                matches.forEach { match ->
                    addStyle(
                        style = SpanStyle(background = color),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
        }.toAnnotatedString()
    }

    Text(
        text = annotatedText,
        fontSize = fontSize,
        color = colour,
        fontFamily = UniversalFamily,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}