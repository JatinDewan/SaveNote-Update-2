package note.notes.savenote.ViewModelClasses

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.withContext
import note.notes.savenote.PersistentStorage.roomDatabase.CheckList
import note.notes.savenote.PersistentStorage.roomDatabase.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.swapAll
import java.util.UUID


class ChecklistViewModel (
    private val primaryViewModel: PrimaryViewModel,
    savedStateHandle: SavedStateHandle = SavedStateHandle()
): ViewModel() {

    private val stateSetter = MutableStateFlow(ChecklistUiState())
    val stateGetter: StateFlow<ChecklistUiState> = stateSetter.asStateFlow()
    private val temporaryChecklist = mutableListOf<CheckList>()

    init {
        updateList()
        updateState()
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    var checklistEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    fun header(header: String?) = viewModelScope.launch {
        if(header != null) stateSetter.update { currentState -> currentState.copy(header = header) }
    }

    fun confirmDeleteAllChecked() = viewModelScope.launch {
        temporaryChecklist.removeIf { it.strike == 1 }
        moreOptionsMenu()
    }

    fun unCheckCompleted() = viewModelScope.launch{
        temporaryChecklist.forEach { if (it.strike == 1) it.strike = 0 }
        moreOptionsMenu()
    }

    fun updateChecklistEntry(newEntry: String) = viewModelScope.launch{
        checklistEntry = TextFieldValue(newEntry)
    }

    fun moreOptionsMenu(isVisible: Boolean = false) = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(isVisible = isVisible) }
    }

    fun dragRestriction(fromIndex: Int): Boolean {
        return fromIndex <= stateGetter.value.checklistUnChecked.lastIndex + 3 && fromIndex >= 3
    }

    fun updateShowCompleted(boolean: Boolean) = viewModelScope.launch {
        primaryViewModel.sharedPref.setCompletedChecklistLayout(boolean)
    }

    private fun updateState() = viewModelScope.launch {
        primaryViewModel.sharedPref.getCompletedChecklistEntries.collect {
            stateSetter.update { currentState -> currentState.copy( showCompleted = it ) }
        }
    }

    fun editChecklistEntry(entryKey: Any?) = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(checklistKey = entryKey) }
    }

    fun reArrange() = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(reArrange = !stateGetter.value.reArrange) }
    }

    fun clearChecklistEdit() = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(checklistKey = null) }
    }


    @OptIn(ExperimentalFoundationApi::class)
    fun bringInToViewRequester(bringIntoViewRequester: BringIntoViewRequester) {
        viewModelScope.launch{ bringIntoViewRequester.bringIntoView() }
    }

    fun iconSelection(iconSelectionOne: Boolean, iconSelectionTwo: Boolean): Int {
        return when {
            iconSelectionOne -> { R.drawable.switch_vertical_01 }
            iconSelectionTwo -> { R.drawable.x_close }
            else -> { R.drawable.circle }
        }
    }

    fun focusChange(
        focusState: FocusState,
        checkList: CheckList,
        isEntryEmpty: Boolean,
        entry: String
    ) {
        when {
            focusState.isFocused -> editChecklistEntry(checkList.key)
            !focusState.isFocused -> if(isEntryEmpty) entryEditOrAdd(checkList = checkList, entry = entry)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun bringInToViewRequest(checkList: CheckList, bringIntoViewRequester: BringIntoViewRequester) {
        if(stateGetter.value.checklistKey == checkList.key) viewModelScope.launch {
            delay(200)
            bringIntoViewRequester.bringIntoView()
        }
    }

    fun checklistCompletedTask(checkList: CheckList) {
        viewModelScope.launch{
            temporaryChecklist[temporaryChecklist.indexOf(checkList)] = CheckList(checkList.note, 0, checkList.key)
            updateList()
        }
    }

    private fun updateList() = viewModelScope.launch(Dispatchers.Main) {
        stateSetter.update { updateState ->
            updateState.copy(
                checklistUnChecked = withContext(Dispatchers.Default) {
                    temporaryChecklist.filter { note -> note.strike == 0 }
                },
                checklistChecked = withContext(Dispatchers.Default) {
                    temporaryChecklist.filter { note -> note.strike == 1 }
                }
            )
        }
    }

    fun entryEditOrAdd(
        strike: Int = 0,
        entry: String,
        deletable:Boolean = true,
        checkList: CheckList
    ) = viewModelScope.launch{
        val index = temporaryChecklist.indexOf(checkList)
        try {
            when {
                entry.isEmpty() && deletable -> temporaryChecklist.remove(checkList)
                else -> temporaryChecklist[index] = CheckList(entry, strike, checkList.key)
            }
        } catch (_:IndexOutOfBoundsException) {
            Log.ERROR
        }
        updateList()
    }

    fun deleteOrComplete(checkList: CheckList) = viewModelScope.launch {
        if(stateGetter.value.checklistKey == checkList.key) {
            temporaryChecklist.remove(checkList)
        } else {
            entryEditOrAdd(
                entry = checkList.note,
                strike = 1,
                checkList = checkList
            )
        }
        updateList()
    }

    fun onMoveIndexer(fromKey: Any?, toKey: Any?){

        temporaryChecklist.indexOfFirst { it.key == fromKey }.takeIf { it >= 0 }?.let { fromIndex ->
            temporaryChecklist.indexOfFirst { it.key == toKey }.takeIf { it >= 0 }?.let { toIndex ->
                temporaryChecklist.add(toIndex, temporaryChecklist.removeAt(fromIndex)).also {
                    updateList()
                    // Adjust for the case when moving forward, as removeAt shifts elements to the left.
                    if (fromIndex < toIndex) {
                        temporaryChecklist.removeAt(toIndex)
                    }
                }
            }
        }
//        var fromIndex = -1
//        var toIndex = -1
//        for (i in 0 until temporaryChecklist.size) {
//            val item = temporaryChecklist[i]
//            if (item.key == fromKey) {
//                fromIndex = i
////                if (toIndex >= 0) break
//            } else if (item.key == toKey) {
//                toIndex = i
////                if (fromIndex >= 0) break
//            }
//            updateList()
//        }
//        if (fromIndex >= 0 && toIndex >= 0) {
//            temporaryChecklist.add(toIndex, temporaryChecklist.removeAt(fromIndex)).apply {  }
//        }
    }

    fun shareChecklist(): Intent {
        val listEntries = stateSetter.value.checklistChecked
            .joinToString { "\n○ ${it.note}" }
            .replace(",","")
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${stateGetter.value.header}\n$listEntries")
                type = "text/plain"
            },
            null
        )
    }

    fun saveChangesOnLifeCycleChange(){
        val headerCheck = stateGetter.value.fullChecklist?.header != stateGetter.value.header
        val checklistCheck = stateGetter.value.fullChecklist?.checkList != temporaryChecklist
        if(headerCheck || checklistCheck) {
            primaryViewModel.editNote(
                uid = stateGetter.value.uid,
                header = stateGetter.value.header,
                checklist = ArrayList(temporaryChecklist),
                category = stateGetter.value.category
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun addEntryToChecklist(bringIntoViewRequester: BringIntoViewRequester) {
        val addEntry = viewModelScope.launch {
            if(checklistEntry.text.isNotEmpty())  {
                temporaryChecklist.add(
                    CheckList(
                        note = checklistEntry.text,
                        strike = 0,
                        key = UUID.randomUUID()
                    )
                )
            }
        }

        viewModelScope.launch {
            addEntry.join()
            updateList().join()
            checklistEntry = TextFieldValue("")
            delay(50)
            bringInToViewRequester(bringIntoViewRequester)
        }
    }

    fun newChecklist() = viewModelScope.launch {
        temporaryChecklist.clear()
        updateChecklistEntry("")
        primaryViewModel.insertNote {
            stateSetter.update {
                currentState -> currentState.copy(
                    uid = it.uid!!,
                    navigateNewChecklist = true,
                    checklistChecked = emptyList(),
                    checklistUnChecked = emptyList(),
                    header = ""
                )
            }
        }
    }

    fun editChecklist(note: Note, navigateToNote: Boolean = false) = viewModelScope.launch {
        updateChecklistEntry("")
        stateSetter.update {
            currentState -> currentState.copy(
                fullChecklist = note,
                uid = note.uid!!,
                checklistUnChecked = note.checkList?.filter { it.strike == 0 } ?: emptyList(),
                checklistChecked = note.checkList?.filter { it.strike == 1 } ?: emptyList(),
                header = note.header ?: "",
                category = note.category,
                navigateNewChecklist = navigateToNote
            )
        }
        temporaryChecklist.swapAll(note.checkList)
    }

    fun exitAndSave()  = viewModelScope.launch {
        val headerCheck = stateGetter.value.fullChecklist?.header != stateGetter.value.header
        val checklistCheck = stateGetter.value.fullChecklist?.checkList != temporaryChecklist
        when {
            stateGetter.value.header.isEmpty() && temporaryChecklist.isEmpty() -> {
                primaryViewModel.deleteNote(stateGetter.value.uid)
            }
            headerCheck || checklistCheck -> {
                primaryViewModel.editNote(
                    uid = stateGetter.value.uid,
                    header = stateGetter.value.header,
                    checklist = ArrayList(temporaryChecklist),
                    category = stateGetter.value.category
                )
            }
        }
        stateSetter.update {
            currentState -> currentState.copy(
                reArrange = false,
                navigateNewChecklist = false
            )
        }
    }
}