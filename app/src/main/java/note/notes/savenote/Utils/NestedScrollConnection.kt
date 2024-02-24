package note.notes.savenote.Utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun animateOnScroll(animateScroll: State<Boolean>, offset: State<Float>): IntOffset {
    val animationSpec = animateFloatAsState(targetValue = offset.value, label = "")
    val animateOrState = if(animateScroll.value) offset else animationSpec

    return IntOffset(x = 0, y = animateOrState.value.roundToInt())
}

@Composable
fun customNestedScrollConnection(
    toolbarOffsetHeightPx: MutableFloatState,
    allNotesObserver: State<Boolean>,
    allowAnimation: MutableState<Boolean>
):NestedScrollConnection {
    val toolbarHeightPx = with(LocalDensity.current) { 60.dp.roundToPx().toFloat() }
    val derivedObserver = remember{ derivedStateOf { allNotesObserver.value } }
    val toolBarOffsetObserver = remember { derivedStateOf { toolbarOffsetHeightPx.floatValue } }
    return  remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.floatValue + delta
                if(derivedObserver.value) toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                allowAnimation.value = true
                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                toolbarOffsetHeightPx.floatValue = if(toolBarOffsetObserver.value  >= -82.5 || !derivedObserver.value) 0f else -165f
                allowAnimation.value = false
                return super.onPostFling(consumed, available)
            }
        }
    }
}
