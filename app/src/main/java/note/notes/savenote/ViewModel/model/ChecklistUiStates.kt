package note.notes.savenote.ViewModel.model

import androidx.compose.runtime.Immutable
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
@Immutable
data class ChecklistUiState (
    val navigateNewChecklist: Boolean = false,
    val showCompleted: Boolean = false,
    val showMoreOptionsMenu: Boolean = false,
    val reArrange: Boolean = false,
    val fullChecklist: Note = Note(),
    val checklistKey: Any? = null,
    val category: String? = null,
    val header: String = "",
    val uid: Int = 0,
    val isTextFocused: Boolean = false
)