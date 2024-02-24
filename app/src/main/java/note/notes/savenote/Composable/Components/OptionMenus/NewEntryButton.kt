package note.notes.savenote.Composable.Components.OptionMenus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import note.notes.savenote.Composable.Components.Templates.ButtonEntries
import note.notes.savenote.Composable.Components.Templates.OptionMenuContainer
import note.notes.savenote.R

@Composable
fun NewEntryButton (
    dismiss: Boolean,
    hideButton: Boolean,
    expand:() -> Unit,
    collapse:() -> Unit,
    navigateNewNote:() -> Unit,
    navigateNewChecklist:() -> Unit,
) {
    OptionMenuContainer(
        modifier = Modifier.padding(20.dp),
        contentAlignment = Alignment.BottomEnd,
        dismiss = dismiss,
        expandedIsFalse = collapse::invoke
    ) {
        if(hideButton){
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ButtonEntries(
                    buttonFunction = navigateNewChecklist::invoke,
                    entryLabel = R.string.ListOnly,
                    entryIcon = R.drawable.dotpoints_01,
                    dismiss = dismiss,
                    animationDelay = 200,
                    textColour = colors.background,
                    padding = 9.dp

                )

                ButtonEntries(
                    buttonFunction = navigateNewNote::invoke,
                    entryLabel = R.string.Note,
                    entryIcon = R.drawable.pencil_01,
                    dismiss = dismiss,
                    animationDelay = 100,
                    textColour = colors.background,
                    padding = 9.dp
                )

                RotatingNewEntryIcon(
                    modifier = Modifier
                        .fillMaxSize()
                        .size(60.dp)
                        .padding(10.dp),
                    icon = R.drawable.plus,
                    boolean = dismiss,
                    dismiss = dismiss,
                    expand = expand::invoke,
                    collapse = collapse::invoke
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RotatingNewEntryIcon(
    icon: Int,
    modifier: Modifier = Modifier,
    boolean: Boolean,
    dismiss: Boolean,
    expand: () -> Unit,
    collapse: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val backgroundColour: Color by animateColorAsState(
        targetValue = if(dismiss) colors.onSecondary else colors.primary,
        animationSpec = tween(500),
        label = ""
    )

    LaunchedEffect(boolean){
        when(boolean){
            true -> {
                rotation.animateTo(
                    targetValue = 45f,
                    animationSpec = tween(150)
                )
            }
            else -> {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(150)
                )
            }
        }
    }

    Card(
        shape = RoundedCornerShape(15.dp),
        backgroundColor = backgroundColour,
        modifier = Modifier.size(55.dp),
        onClick = { if (!dismiss) expand() else collapse() }
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = if(dismiss) colors.background else colors.secondary,
            contentDescription = null,
            modifier = modifier.rotate(rotation.value)
        )
    }
}