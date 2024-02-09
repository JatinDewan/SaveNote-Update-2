package note.notes.savenote.ViewModelClasses

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import note.notes.savenote.PersistentStorage.roomDatabase.CheckList
import note.notes.savenote.PersistentStorage.roomDatabase.Note
@Immutable
data class ChecklistUiState (
    val modifiableChecklist: Flow<List<CheckList>> = flow{ mutableListOf<CheckList>() },
    val checklistToDo: List<CheckList> = emptyList(),
    val checklistCompleted: List<CheckList> = emptyList(),
    val navigateNewChecklist: Boolean = false,
    val showCompleted: Boolean = false,
    val showMoreOptionsMenu: Boolean = false,
    val reArrange: Boolean = false,
    val fullChecklist: Note? = null,
    val checklistKey: Any? = null,
    val category: String? = null,
    val header: String = "",
    val uid: Int = 0,
    val isTextFocused: Boolean = false
)