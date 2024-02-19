package note.notes.savenote.Composable.Components.Templates

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
    menu: @Composable () -> Unit,
    expandedIsFalse:() -> Unit,
    additionalDismissFunction:() -> Unit = { /*TODO()*/ },
    contentAlignment: Alignment = Alignment.TopEnd
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
            content = { menu() }
        )
    }
}