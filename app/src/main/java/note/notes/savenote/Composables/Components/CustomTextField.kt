package note.notes.savenote.Composables.Components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    searchIsActive: Boolean = true,
    onValueChange: (String) -> Unit,
    decorationBox: @Composable () -> Unit,
    keyboardAction: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    onTextLayout:(TextLayoutResult) -> Unit = { /*TODO()*/ },
    singleLine: Boolean = false,
    maxLines: Int = 1000,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textColour: Color = colors.primaryVariant
){
    BasicTextField(
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        keyboardActions = keyboardAction,
        cursorBrush = SolidColor(colors.primary),
        textStyle = TextStyle(
            color = textColour,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = UniversalFamily
        ),
        onTextLayout = onTextLayout,
        value = value,
        onValueChange = { onValueChange(it) },
        decorationBox = { innerTextField ->
            decorationBox()
            innerTextField()
        },
        maxLines = maxLines,
        enabled = searchIsActive
    )
}


@Composable
fun TextFieldPlaceHolder(
    showPlaceHolder: Boolean,
    text: Int,
    colour: Color = colors.onSurface,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal

){
    if (showPlaceHolder) {
        Text(
            text = stringResource(text),
            color = colour,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = UniversalFamily
        )
    }
}