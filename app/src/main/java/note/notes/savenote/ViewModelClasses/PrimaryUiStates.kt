package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import note.notes.savenote.PersistentStorage.roomDatabase.Note

@Immutable
data class PrimaryUiState (
    val allEntries: List<Note> = emptyList(),
    val favoriteEntries: List<Note> = emptyList(),
    val searchEntries: List<Note> = emptyList(),
    val showSearchBar: Boolean = false,
    val newEntryButton: Boolean = false,
    val confirmDelete: Boolean = false,
    val layoutView: Boolean = true,
    val showBackup: Boolean = false,
    val dropDown: Boolean = false,
    val loadingScreen: Boolean = false,
    val sortByView: Int = 0,
    val setTheme: Int = 1,
    val barOffsetY: Float = 0f,
    val searchQuery: String = ""
)