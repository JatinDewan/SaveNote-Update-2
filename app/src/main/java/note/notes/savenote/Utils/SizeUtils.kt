package note.notes.savenote.Utils

import androidx.compose.ui.unit.Dp
import note.notes.savenote.Database.roomDatabase.CheckList

class SizeUtils {
    fun DPISelection(boolean: Boolean, dpFirst: Dp, dpSecond: Dp) = if (boolean) dpFirst else dpSecond
}

fun List<CheckList>.rangeFinder(maxRange: Int): List<CheckList> {
    val list = this.filter { it.strike == 0 }
    return list.subList(0,if (list.size > maxRange) maxRange else list.size)
}