package note.notes.savenote.ViewModel.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import note.notes.savenote.PersistentStorage.RoomDatabase.CheckList
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
@Immutable
data class ChecklistUiState (
    val temporaryChecklist: SnapshotStateList<CheckList> = mutableStateListOf(),
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