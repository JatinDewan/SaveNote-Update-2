package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.PersistentStorage.roomDatabase.CheckList
import note.notes.savenote.PersistentStorage.roomDatabase.Note
@Immutable
data class ChecklistUiState (
    val checklistChecked: MutableList<CheckList> = mutableListOf(),
    val checklistUnChecked: List<CheckList> = emptyList(),
    val navigateNewChecklist: Boolean = false,
    val showCompleted: Boolean = false,
    val isVisible: Boolean = false,
    val reArrange: Boolean = false,
    val fullChecklist: Note? = null,
    val checklistKey: Any? = null,
    val category: String? = null,
    val header: String = "",
    val uid: Int = 0
)