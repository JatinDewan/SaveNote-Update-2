package note.notes.savenote.Composables.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun Int.scaledSp(): TextUnit {
    val value: Int = this
    return with(LocalDensity.current) {
        val fontScale = this.fontScale
        val textSize = value / fontScale
        textSize.sp
    }
}

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    searchIsActive: Boolean = true,
    onValueChange: (String) -> Unit,
    decorationBox: @Composable () -> Unit = { /*TODO()*/ },
    keyboardAction: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    onTextLayout:(TextLayoutResult) -> Unit = { /*TODO()*/ },
    singleLine: Boolean = false,
    maxLines: Int = 1000,
    fontSize: TextUnit = 15.scaledSp(),
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
    showPlaceHolderIcon: Boolean = false,
    text: Int,
    colour: Color = colors.secondaryVariant,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal

){
    if (showPlaceHolder) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ){
            Text(
                text = stringResource(text),
                color = colour,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = UniversalFamily
            )
            if(showPlaceHolderIcon){
                Icon(
                    tint = colour,
                    painter = painterResource(id = R.drawable.pencil_01),
                    contentDescription = stringResource(R.string.Check),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}