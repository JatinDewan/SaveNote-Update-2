package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.Database.roomDatabase.Note

@Immutable
data class NotesUiState(
    val fullNote: Note? = null,
    val uid: Int = 0,
    val header: String = "",
    val confirmDelete: Boolean = false,
    val category: String? = null,
    val navigateNewNote: Boolean = false
)