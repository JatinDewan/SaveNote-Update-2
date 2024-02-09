package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.PersistentStorage.roomDatabase.Note

@Immutable
data class NotesUiState(
    val navigateNewNote: Boolean = false,
    val fullNote: Note? = null,
    val category: String? = null,
    val header: String = "",
    val uid: Int = 0
)