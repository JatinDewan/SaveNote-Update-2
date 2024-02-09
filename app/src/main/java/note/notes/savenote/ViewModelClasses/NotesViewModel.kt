package note.notes.savenote.ViewModelClasses

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import note.notes.savenote.PersistentStorage.roomDatabase.Note

class NotesViewModel(
    private val primaryViewModel: PrimaryViewModel,
    savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val stateSetter = MutableStateFlow(NotesUiState())
    val stateGetter: StateFlow<NotesUiState> = stateSetter.asStateFlow()

    fun shareNote(): Intent {
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${stateGetter.value.header}\n${noteEntry.text}")
                type = "text/plain"
            }, null
        )
    }

    fun editNote(note: Note) = viewModelScope.launch {
        stateSetter.update {
            updateState -> updateState.copy(
                fullNote = note,
                uid = note.uid!!,
                header = note.header ?: "",
                category = note.category,
                navigateNewNote = true
            )
        }
        noteEntry = TextFieldValue(note.note ?: "")
    }

    fun showDate(): String {
        fun thisString(dateAndTime: String) = primaryViewModel.dateUtils.formatDateAndTime(dateAndTime) {}
        return if(stateGetter.value.fullNote?.date.isNullOrEmpty()) {
            "Created - ${thisString(primaryViewModel.dateUtils.getCurrentDateAndTime())}"
        } else {
            "Last edited - ${thisString(stateGetter.value.fullNote?.date!!)}"
        }
    }
    fun header(header: String?) {
        stateSetter.update { currentState ->
            currentState.copy(header = header ?: "")
        }
    }

    fun navigateNewNote() = viewModelScope.launch {
        viewModelScope.launch{
            stateSetter.update { currentState -> currentState.copy(navigateNewNote = true) }
        }
        primaryViewModel.newEntryButton()
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    var noteEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    fun updateNoteEntry(newEntry: String) = viewModelScope.launch{ noteEntry = TextFieldValue(newEntry) }

    private fun saveNewOrEditExistingNote() = viewModelScope.launch {
        if(stateGetter.value.uid == 0) {
            if(stateGetter.value.header.isNotEmpty() || noteEntry.text.isNotEmpty()) {
                withContext(Dispatchers.IO){
                    primaryViewModel.insertNote(
                        Note(
                            header = stateGetter.value.header,
                            note = noteEntry.text
                        )
                    )
                }
            }
        } else {
            if(
                stateGetter.value.fullNote?.header != stateGetter.value.header ||
                stateGetter.value.fullNote?.note != noteEntry.text
            ){
                withContext(Dispatchers.IO){
                    primaryViewModel.editNote(
                        uid = stateGetter.value.uid,
                        header = stateGetter.value.header,
                        note = noteEntry.text,
                        category = stateGetter.value.category
                    )
                }
            }
        }
    }

    fun resetValues() = viewModelScope.launch {
        noteEntry = TextFieldValue("")
        stateSetter.update {
            updateState -> updateState.copy(
                fullNote = null,
                uid = 0,
                header = "",
                category = null,
                navigateNewNote = false
            )
        }
    }

    fun exitAndSave(focusManager: FocusManager) = viewModelScope.launch {
        focusManager.clearFocus()
        saveNewOrEditExistingNote()
    }
}