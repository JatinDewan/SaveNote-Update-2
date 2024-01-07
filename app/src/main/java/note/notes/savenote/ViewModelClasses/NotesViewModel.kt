package note.notes.savenote.ViewModelClasses

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import note.notes.savenote.Database.Note
import note.notes.savenote.Utils.CheckStringUtil
import note.notes.savenote.Utils.DateUtils

class NotesViewModel(
    private val primaryViewModel: PrimaryViewModel,
    private val date: DateUtils = DateUtils(),
    savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    private val checkStringUtil = CheckStringUtil()

    fun shareNote(): Intent {
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${uiState.value.header}\n${noteEntry.text}")
                type = "text/plain"
            }, null
        )
    }

    fun navigateToNote(note: Note, navigateToNote: Boolean = false){
        viewModelScope.launch {
            noteChecker(note)
            uid(note.uid)
            header(checkStringUtil.replaceNull(note.header))
            update(checkStringUtil.replaceNull(note.note))
            category(note.category)
            if(navigateToNote) openNewNote()
        }
    }

    private fun category(category:String?) {
        _uiState.update { currentState ->
            currentState.copy(
                category = category
            )
        }
    }

    fun header(header: String?) {
        if(header != null){
            _uiState.update { currentState ->
                currentState.copy(header = header)
            }
        }
    }

    fun uid(uid: Int?) {
        if(uid != null) {
            _uiState.update { currentState ->
                currentState.copy( uid = uid )
            }
        }
    }

    fun returnAndSaveNote(closeScreen:() -> Unit) {
        viewModelScope.launch {
            editOrDeleteNote()
            delay(100)
            closeScreen()
        }
    }

    fun clearNote(){
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(uid = 0, header = "") }
            update("")
        }
    }

    fun noteChecker(note: Note) {
        _uiState.update { currentState -> currentState.copy(fullNote = note) }
    }

    fun openNewNote(openNote: Boolean = true){
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(navigateNewNote = openNote) }
        }
    }

    fun navigateNewNote(){
        viewModelScope.launch {
            openNewNote()
            clearNote()
            delay(400)
            createBlankNote()
            primaryViewModel.newEntryButton()
        }
    }

    fun createBlankNote() {
        viewModelScope.launch{
            primaryViewModel.notesRepositoryImp.insertNote(
                Note(
                    uid = null,
                    header =null,
                    note = null,
                    date = date.current,
                    checkList = null,
                    category = null
                )
            )

            primaryViewModel.notesRepositoryImp.getNote().collect { note ->
                if(note.isNotEmpty()) {
                    if(
                        note.last().note.isNullOrEmpty() &&
                        note.last().header.isNullOrEmpty() &&
                        note.last().checkList.isNullOrEmpty()
                    ){
                        navigateToNote(note.last())
                    }
                }
            }
        }
    }

    fun saveNoteEdit(){
        val headerCheck =
            uiState.value.fullNote?.header != uiState.value.header && uiState.value.header.isNotEmpty()
        val noteCheck =
            uiState.value.fullNote?.note != noteEntry.text && noteEntry.text.isNotEmpty()

        if(headerCheck || noteCheck) {
            viewModelScope.launch {
                primaryViewModel.notesRepositoryImp.editNote(
                    Note(
                        uiState.value.uid,
                        checkStringUtil.checkString(uiState.value.header),
                        checkStringUtil.checkString(noteEntry.text),
                        DateUtils().current,
                        null,
                        uiState.value.category
                    )
                )
            }
        }
    }
    @OptIn(SavedStateHandleSaveableApi::class)
    var noteEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    fun update(newEntry: String) = viewModelScope.launch{ noteEntry = TextFieldValue(newEntry) }

    private fun editOrDeleteNote() {
        val headerCheck = uiState.value.fullNote?.header != checkStringUtil.checkString(uiState.value.header)
        val noteCheck = uiState.value.fullNote?.note != checkStringUtil.checkString(noteEntry.text)
        viewModelScope.launch{
            when {
                uiState.value.header.isEmpty() && noteEntry.text.isEmpty() -> {
                    primaryViewModel.notesRepositoryImp.deleteNote(
                        Note(
                            uid = uiState.value.uid,
                            header = null,
                            note = null,
                            date = null,
                            checkList = null,
                            category = null

                        )
                    )
                }

                headerCheck || noteCheck -> {
                    primaryViewModel.notesRepositoryImp.editNote(
                        Note(
                            uiState.value.uid,
                            checkStringUtil.checkString(uiState.value.header),
                            checkStringUtil.checkString(noteEntry.text),
                            DateUtils().current,
                            null,
                            uiState.value.category
                        )
                    )
                }
            }
        }
    }
}