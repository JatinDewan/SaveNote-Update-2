package note.notes.savenote.Utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import note.notes.savenote.PersistentStorage.RoomDatabase.CheckList

fun List<CheckList>.rangeFinder(maxRange: Int): List<CheckList> {
    val list = this.filter { it.strike == 0 }
    return list.subList(0,if (list.size > maxRange) maxRange else list.size)
}

fun MutableList<CheckList>.swapAll(checklist: List<CheckList>?) {
    this.clear()
    this.addAll(checklist ?: emptyList())
}

fun Modifier.conditional(allowModifier: Boolean, modifier: (Modifier) -> Modifier): Modifier {
    return if(allowModifier) modifier(this) else Modifier
}

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }

    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }

    return state
}