package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.Database.roomDatabase.Note

@Immutable
data class PrimaryUiState (
    val allEntries: List<Note> = emptyList(),
    val favoriteEntries: List<Note> = emptyList(),
    val searchEntries: List<Note> = emptyList(),
    val showSearchBar: Boolean = false,
    val newEntryButton: Boolean = false,
    val confirmDelete: Boolean = false,
    val currentPage: Boolean = true,
    val showBackup: Boolean = false,
    val dropDown: Boolean = false,
    val openNote: Boolean = false,
    val openChecklist: Boolean = false,
    val animateCloseBar: Boolean = true,
    val sortByView: Int = 0,
    val barOffsetY: Float = 0f,
    val searchQuery: String = ""
)