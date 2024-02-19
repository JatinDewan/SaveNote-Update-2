package note.notes.savenote.Composable.Components.Templates

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun ButtonEntries(
    modifier: Modifier = Modifier,
    textBoxModifier: Modifier = Modifier,
    buttonFunction:() -> Unit,
    entryLabel: Int,
    entryIcon: Int,
    dismiss: Boolean,
    animationDelay: Int,
    additionalText: Int? = null,
    textChangeCondition: Int? = null,
    iconBackgroundColour: Color = MaterialTheme.colors.primary,
    textBackgroundColour: Color = MaterialTheme.colors.onSecondary,
    iconColour: Color = MaterialTheme.colors.background,
    textColour: Color = MaterialTheme.colors.onSecondary,
    padding: Dp = 10.dp,
    additionalTextSpacing: Dp = 7.dp
){

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { buttonFunction() }
                )
            }
            .padding(horizontal = padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = dismiss,
            enter = slideInHorizontally(tween(animationDelay + 200)) + fadeIn(tween(animationDelay + 200)),
            exit = fadeOut(tween(100))
        ){
            Card(
                backgroundColor = textBackgroundColour,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = textBoxModifier.padding(
                            top = 5.dp, bottom = 5.dp, start = 7.dp, end = additionalTextSpacing
                        ),
                        text = stringResource(id = entryLabel),
                        color = textColour,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = UniversalFamily,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    if (additionalText != null && textChangeCondition != null) {
                        AdditionalText(
                            textChangeCondition = textChangeCondition,
                            entryLabel = additionalText,
                            textColour = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = dismiss,
            enter = scaleIn(tween(animationDelay)) + fadeIn(tween(animationDelay)),
            exit = scaleOut(tween(100)) + fadeOut(tween(100))
        ){
            Card(
                backgroundColor = iconBackgroundColour,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    tint = iconColour,
                    painter = painterResource(id = entryIcon),
                    contentDescription = stringResource(R.string.Check),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }
    }

}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun AdditionalText(
    textChangeCondition: Int,
    entryLabel: Int,
    textColour: Color
){
    AnimatedContent(
        targetState = textChangeCondition,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) + fadeIn(tween(100)) togetherWith
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) + fadeOut(tween(100))
        },
        label = ""
    ){ _ ->
        Text(
            modifier = Modifier.padding(end = 7.dp),
            text = stringResource(id = entryLabel),
            color = textColour,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = UniversalFamily,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}