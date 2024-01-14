package note.notes.savenote.ViewModelClasses

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import note.notes.savenote.Database.roomDatabase.CheckList
import note.notes.savenote.Database.roomDatabase.Note
import note.notes.savenote.Utils.CheckStringUtil
import java.util.UUID


class ChecklistViewModel (
    private val primaryViewModel: PrimaryViewModel,
    savedStateHandle: SavedStateHandle = SavedStateHandle()
): ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    private val checkStringUtil = CheckStringUtil()
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()
    private var temporaryChecklist = mutableStateListOf<CheckList>()

    @OptIn(SavedStateHandleSaveableApi::class)
    var checklistEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    init { updateState() }

    fun checklistUncheckedUpdater():List<CheckList> {
        return temporaryChecklist.filter { entry -> entry.strike == 0 }.toMutableStateList()
    }

    fun checklistCheckedUpdater():List<CheckList> {
        return temporaryChecklist.filter { entry -> entry.strike == 1}.toMutableStateList()
    }

    private fun toDoList(note: List<CheckList>?) {
        if (note != null) temporaryChecklist = note.toMutableStateList()
    }

    fun header(header: String?) {
        if(header != null) _uiState.update { currentState -> currentState.copy(header = header) }
    }

    fun confirmDeleteAllChecked() {
        viewModelScope.launch{
            temporaryChecklist.removeIf { it.strike == 1 }
            pullUp()
        }
    }

    fun completedNote(completedNote: Boolean) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(completedNotes = completedNote) }
        }
    }

    fun unCheckCompleted() {
        viewModelScope.launch{
            temporaryChecklist.forEach { if (it.strike == 1) it.strike = 0 }
            pullUp()
        }
    }

    fun update(newEntry: String) = viewModelScope.launch{ checklistEntry = TextFieldValue(newEntry) }

    fun pullUp(isVisible: Boolean = false) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(isVisible = isVisible) }
        }
    }

    private fun uid(uid: Int?) {
        if (uid != null) _uiState.update { currentState -> currentState.copy(uid = uid) }
    }

    fun dragRestriction(fromIndex: Int): Boolean {
        return fromIndex <= checklistUncheckedUpdater().lastIndex + 3 && fromIndex >= 3
    }

    fun updateShowCompleted(boolean: Boolean) {
        viewModelScope.launch { primaryViewModel.sharedPref.setCompletedChecklistLayout(boolean) }
    }

    private fun updateState() {
        viewModelScope.launch {
            primaryViewModel.sharedPref.getCompletedChecklistEntries.collect {
                _uiState.update {  currentState -> currentState.copy( showCompleted = it ) }
            }
        }
    }

    fun editChecklistEntry(entryKey: Any?) {
        _uiState.update { currentState -> currentState.copy(checklistKey = entryKey) }
    }

    private fun checklistChecker(note: Note) {
        _uiState.update { currentState -> currentState.copy(fullChecklist = note) }
    }

    fun reArrange(reArrange: Boolean) {
        viewModelScope.launch(Dispatchers.IO){
            _uiState.update { currentState -> currentState.copy(reArrange = reArrange) }
        }
    }

    fun clearChecklistEdit() {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(checklistKey = null) }
        }
    }

    private fun deleteEntry(location: Int) {
        viewModelScope.launch { temporaryChecklist.removeAt(location) }
    }

    fun focusChange(focusState: FocusState, checkList: CheckList, isEntryEmpty: Boolean, entry: String) {
        when {
            focusState.isFocused -> editChecklistEntry(checkList.key)
            !focusState.isFocused -> if(isEntryEmpty) entryEditOrAdd(checkList = checkList, entry = entry)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun bringInToViewRequest(checkList: CheckList, bringIntoViewRequester: BringIntoViewRequester) {
        if(uiState.value.checklistKey == checkList.key) viewModelScope.launch {
            delay(200)
            bringIntoViewRequester.bringIntoView()
        }
    }

    fun checklistCompletedTask(checkList: CheckList) {
        viewModelScope.launch{
            temporaryChecklist[temporaryChecklist.indexOf(checkList)] = CheckList(checkList.note, 0, checkList.key)
            if(checklistCheckedUpdater().isEmpty()) completedNote(false)
        }
    }

    fun entryEditOrAdd(strike: Int = 0, entry: String, deletable:Boolean = true, checkList: CheckList) {
        viewModelScope.launch{
            try {
                when(entry.isEmpty()) {
                    true -> if (deletable) temporaryChecklist.indexOf(checkList)
                    else -> temporaryChecklist[temporaryChecklist.indexOf(checkList)] = CheckList(entry, strike, checkList.key)
                }
            } catch (_:IndexOutOfBoundsException) {
                Log.ERROR
            }
        }
    }

    fun deleteOrComplete(checkList: CheckList){
        if(uiState.value.checklistKey == checkList.key){
            deleteEntry(temporaryChecklist.indexOf(checkList))
        } else {
            entryEditOrAdd(
                entry = checkList.note,
                strike = 1,
                checkList = checkList
            )
        }
    }

    fun onBackPress(closeScreen:()-> Unit){
        if(uiState.value.isVisible) pullUp() else
            if(uiState.value.reArrange) reArrange(false) else returnAndSaveChecklist(closeScreen)
    }



    fun onMoveIndexer(fromKey: Any?, toKey: Any?){
        viewModelScope.launch {
            var fromIndex = -1
            var toIndex = -1
            for (i in 0 until temporaryChecklist.size) {
                val item = temporaryChecklist[i]
                if (item.key == fromKey) {
                    fromIndex = i
                    if (toIndex >= 0) break
                } else if (item.key == toKey) {
                    toIndex = i
                    if (fromIndex >= 0) break
                }
            }
            if (fromIndex >= 0 && toIndex >= 0) {
                temporaryChecklist.add(toIndex, temporaryChecklist.removeAt(fromIndex))
            }
        }
    }

    fun iconSelection(iconSelectionOne: Boolean, iconSelectionTwo: Boolean): Int {
        return when {
            iconSelectionOne -> { note.notes.savenote.R.drawable.switch_vertical_01 }

            iconSelectionTwo -> { note.notes.savenote.R.drawable.x_close }

            else -> { note.notes.savenote.R.drawable.circle }
        }
    }

    private fun clearValuesChecklist() {
        viewModelScope.launch{
            temporaryChecklist.clear()
            _uiState.update { currentState ->
                currentState.copy(
                    uid = 0,
                    header = ""
                )
            }
            update("")
        }
    }

    fun createBlankChecklist() {
        viewModelScope.launch{
            primaryViewModel.insertNote{
            }

//            primaryViewModel.notesRepositoryImp.getNote().collect {
//                if (it.isNotEmpty()) {
//                    if (
//                        it.last().note.isNullOrEmpty() &&
//                        it.last().header.isNullOrEmpty() &&
//                        it.last().checkList.isNullOrEmpty()
//                    ) primaryViewModel.getNote { note -> note.last() }
//                }
//            }
        }
    }

    private fun editOrDeleteChecklist(){
        val headerCheck = uiState.value.fullChecklist?.header != checkStringUtil.checkString(uiState.value.header)
        val checklistCheck = uiState.value.fullChecklist?.checkList != temporaryChecklist
        when {
            uiState.value.header.isEmpty() && temporaryChecklist.isEmpty() -> {
                primaryViewModel.deleteNote(uiState.value.uid)
            }
            headerCheck || checklistCheck -> {
                primaryViewModel.editNote(
                    uid = uiState.value.uid,
                    header = checkStringUtil.checkString(uiState.value.header),
                    checklist = ArrayList(temporaryChecklist),
                    category = uiState.value.category
                )
            }
        }
    }

    fun saveChecklistEdit(){
        val headerCheck = uiState.value.fullChecklist?.header != checkStringUtil.checkString(uiState.value.header)
        val checklistCheck = uiState.value.fullChecklist?.checkList != temporaryChecklist
        if(headerCheck || checklistCheck) {
            primaryViewModel.editNote(
                uid = uiState.value.uid,
                header = checkStringUtil.checkString(uiState.value.header),
                checklist = ArrayList(temporaryChecklist),
                category = uiState.value.category
            )
        }
    }

    fun openNewChecklist(openChecklist: Boolean = true){
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(navigateNewChecklist = openChecklist) }
        }
    }

    fun navigateNewChecklist(){
        viewModelScope.launch {
            openNewChecklist()
            clearValuesChecklist()
            delay(400)
            println("HI")
            createBlankChecklist()
            println("HI2")
            primaryViewModel.newEntryButton()
        }
    }

    fun navigateToChecklist(note: Note, navigateToNote: Boolean = false) {
        viewModelScope.launch {
            checklistChecker(note)
            toDoList(note.checkList)
            header(checkStringUtil.replaceNull(note.header))
            uid(note.uid)
            category(note.category)
            if(navigateToNote) openNewChecklist()
        }
    }

    private fun category(category:String?) {
        _uiState.update { currentState -> currentState.copy(category = category) }
    }

    fun addChecklistEntry() {
        viewModelScope.launch{
            if(checklistEntry.text.isNotEmpty())  {
                temporaryChecklist.add(
                    CheckList(
                        note = checklistEntry.text,
                        strike = 0,
                        key = UUID.randomUUID()
                    )
                )
                checklistEntry = TextFieldValue("")
            }
        }
    }

    fun shareChecklist(): Intent {
        val listEntries = checklistUncheckedUpdater().joinToString { "\n○ ${it.note}" }.replace(",","")
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${uiState.value.header}\n$listEntries")
                type = "text/plain"
            },
            null
        )
    }



    fun returnAndSaveChecklist(closeScreen:()-> Unit){
        completedNote(false)
        reArrange(false)
        viewModelScope.launch {
            editOrDeleteChecklist()
            update("")
            delay(100)
            closeScreen()
        }
    }
}