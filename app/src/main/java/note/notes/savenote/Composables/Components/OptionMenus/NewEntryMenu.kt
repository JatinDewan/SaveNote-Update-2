package note.notes.savenote.Composables.Components.OptionMenus

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
import androidx.compose.material.Colors
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
import note.notes.savenote.R

@OptIn(ExperimentalMaterialApi::class)
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
        expandedIsFalse = collapse::invoke,
        menu = {
            if(hideButton){
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ButtonEntries(
                        buttonFunction = navigateNewChecklist::invoke,
                        entryLabel = R.string.ListOnly,
                        entryIcon = R.drawable.dotpoints_01,
                        dismiss = dismiss,
                        animationDelay = 200,
                        textColour = colors.background
                    )

                    ButtonEntries(
                        buttonFunction = navigateNewNote::invoke,
                        entryLabel = R.string.Note,
                        entryIcon = R.drawable.pencil_line,
                        dismiss = dismiss,
                        animationDelay = 100,
                        textColour = colors.background
                    )

                    Card(
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.size(60.dp),
                        onClick = { if (!dismiss) expand() else collapse() }
                    ) {
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
    )
}

@Composable
fun colourSelectionSimplifier(condition: Boolean, c1: Color, c2: Color): Colors {
    return if(condition) colors.apply { c1 } else colors.apply { c2 }
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
        targetValue = if(!dismiss) colors.primary else colors.onSecondary,
        animationSpec = tween(100),
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
        modifier = Modifier.size(60.dp),
        onClick = { if (!dismiss) expand() else collapse() }
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = colors.background,
            contentDescription = null,
            modifier = modifier.rotate(rotation.value)
        )
    }
}