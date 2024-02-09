package note.notes.savenote.Utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import note.notes.savenote.PersistentStorage.roomDatabase.CheckList

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