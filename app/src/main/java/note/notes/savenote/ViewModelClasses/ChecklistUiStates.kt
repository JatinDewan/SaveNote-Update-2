package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.Database.roomDatabase.Note
@Immutable
data class ChecklistUiState (
    val fullChecklist: Note? = null,
    val uid: Int = 0,
    val header: String = "",
    val checklistEntryNumber: Int? = null,
    val checklistKey: Any? = null,
    val completedNotes: Boolean = false,
    val reArrange: Boolean = false,
    val showCompleted: Boolean = false,
    val isVisible: Boolean = false,
    val category: String? = null,
    val navigateNewChecklist: Boolean = false
)