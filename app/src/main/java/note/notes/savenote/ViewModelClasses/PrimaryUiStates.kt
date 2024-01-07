package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.mutableStateListOf
import note.notes.savenote.Database.Note

data class PrimaryUiState (
    val allEntries: List<Note> = mutableStateListOf(),
    val favoriteEntries: List<Note> = mutableStateListOf(),
    val searchEntries: List<Note> = mutableStateListOf(),
    val showSearchBar: Boolean = false,
    val newEntryButton: Boolean = false,
    val confirmDelete: Boolean = false,
    val currentPage: Boolean = true,
    val showBackup: Boolean = false,
    val dropDown: Boolean = false,
    val showSortBy: Boolean = false,
    val openNote: Boolean = false,
    val openChecklist: Boolean = false,
    val animateCloseBar: Boolean = true,
    val sortByView: Int = 0,
    val barOffsetY: Float = 0f,
    val searchQuery: String = ""
)