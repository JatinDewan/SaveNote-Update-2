package note.notes.savenote.ViewModel.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import note.notes.savenote.PersistentStorage.RoomDatabase.Note

@Immutable
data class PrimaryUiState (
    val allEntries: List<Note> = emptyList(),
    val favoriteEntries: List<Note> = emptyList(),
    val searchEntries: List<Note> = emptyList(),
    val temporaryEntryHold: SnapshotStateList<Note> = mutableStateListOf(),
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
    val searchQuery: String = "",
    val deleteSelectedCount: String = ""
)