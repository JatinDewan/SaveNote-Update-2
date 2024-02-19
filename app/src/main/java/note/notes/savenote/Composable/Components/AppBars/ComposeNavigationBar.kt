package note.notes.savenote.Composable.Components.AppBars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun NotesAndChecklistNavigationBar(
    backButton: () -> Unit,
    buttonAction: () -> Unit,
    date: String = "",
    header: String = "",
    showHeader: Boolean = false,
    moreOptionsOpened: Boolean = false,
    isNote: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopAppBar(
            title = { SlideInHeader(visibility = showHeader, header = header, date = date) },
            backgroundColor = colors.background,
            elevation = if (showHeader) 10.dp else 0.dp,
            navigationIcon = {
                TopAppButtons(
                    onClick = backButton::invoke,
                    icon = R.drawable.arrow_narrow_left,
                    size = 30.dp
                )
            },
            actions = {
                Card(
                    modifier = Modifier
                        .width(50.dp)
                        .height(35.dp)
                        .padding(end = 10.dp),
                    elevation = 5.dp,
                    shape = RoundedCornerShape(20),
                    backgroundColor = colors.secondary
                ) {
                    if(isNote){
                        TopAppButtons(
                            onClick = buttonAction::invoke,
                            icon = R.drawable.share_03
                        )
                    } else {
                        TopAppButtons(
                            onClick = buttonAction::invoke,
                            icon = R.drawable.dots_vertical,
                            color = if(!moreOptionsOpened) colors.primary else colors.onSecondary
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SlideInHeader(
    visibility: Boolean,
    header: String,
    date: String
){
    AnimatedVisibility(
        visible = visibility,
        enter = slideInVertically(
            tween(150, easing = EaseIn),
            initialOffsetY = {fullHeight -> fullHeight}
        ) + fadeIn(tween(150, easing = EaseIn)),
        exit = slideOutVertically(
            tween(150, easing = EaseOut),
            targetOffsetY = {fullHeight -> fullHeight}
        ) + fadeOut(tween(150, easing = EaseOut))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp)
        ){
            Text(
                text = date,
                color = colors.secondaryVariant,
                fontFamily = UniversalFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = header.ifEmpty { "Un-named" },
                color = colors.onSurface,
                fontFamily = UniversalFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TopAppButtons(
    modifier: Modifier = Modifier,
    onClick:() -> Unit,
    icon: Int,
    contentDescription: Int = R.string.DeleteNote,
    size: Dp = 20.dp,
    color: Color = colors.primary
){
    IconButton(
        modifier = modifier,
        onClick = onClick::invoke
    ) {
        Icon(
            modifier = Modifier.size(size),
            painter = painterResource(id = icon),
            contentDescription = stringResource(contentDescription),
            tint = color
        )
    }
}