package note.notes.savenote.Composable.Components.Templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun OptionMenuContainer(
    modifier: Modifier = Modifier,
    dismiss: Boolean,
    showContent: Boolean = true,
    expandedIsFalse:() -> Unit,
    additionalDismissFunction:() -> Unit = { /*TODO()*/ },
    contentAlignment: Alignment = Alignment.TopEnd,
    menu: @Composable () -> Unit
){
    BoxWithConstraints {
        if (dismiss){
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background.copy(alpha = 0.8f))
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onPress = { expandedIsFalse(); additionalDismissFunction() }) }
            )
        }
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = contentAlignment,
            content = {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    menu()
                }
            }
        )
    }
}