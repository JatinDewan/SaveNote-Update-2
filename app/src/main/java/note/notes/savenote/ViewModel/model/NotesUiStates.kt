package note.notes.savenote.ViewModel.model

import androidx.compose.runtime.Immutable
import note.notes.savenote.PersistentStorage.RoomDatabase.Note

@Immutable
data class NotesUiState(
    val navigateNewNote: Boolean = false,
    val fullNote: Note = Note(),
    val category: String? = null,
    val header: String = "",
    val uid: Int = 0
)