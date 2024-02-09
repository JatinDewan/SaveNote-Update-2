package note.notes.savenote.ViewModelClasses

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
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
import kotlinx.coroutines.flow.flow
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
        updateList()
        moreOptionsMenu()
    }

    fun unCheckCompleted() = viewModelScope.launch{
        temporaryChecklist.forEach { if (it.strike == 1) it.strike = 0 }
        updateList()
        moreOptionsMenu()
    }

    fun updateChecklistEntry(newEntry: String) = viewModelScope.launch{
        checklistEntry = TextFieldValue(newEntry)
    }

    fun moreOptionsMenu(moreOptionsMenu: Boolean = false) = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(showMoreOptionsMenu = moreOptionsMenu) }
    }

    fun dragRestriction(fromIndex: Int): Boolean {
        return fromIndex <= stateGetter.value.checklistCompleted.lastIndex + 3 && fromIndex >= 3
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

    fun isTextFocused(isTextFocused: Boolean) = viewModelScope.launch(Dispatchers.Main) {
        stateSetter.update { currentState -> currentState.copy(isTextFocused = isTextFocused) }
    }

    fun clearChecklistEdit() = viewModelScope.launch {
        stateSetter.update { currentState -> currentState.copy(checklistKey = null) }
    }

    fun showDate(): String {
        fun thisString(dateAndTime: String) = primaryViewModel.dateUtils.formatDateAndTime(dateAndTime) {}
        return if(stateGetter.value.fullChecklist?.date.isNullOrEmpty()) {
            "Created - ${thisString(primaryViewModel.dateUtils.getCurrentDateAndTime())}"
        } else {
            "Last edited - ${thisString(stateGetter.value.fullChecklist?.date!!)}"
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    fun bringInToViewRequester(bringIntoViewRequester: BringIntoViewRequester) = viewModelScope.launch {
        bringIntoViewRequester.bringIntoView()
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
                checklistToDo = withContext(Dispatchers.Default) {
                    temporaryChecklist.filter { note -> note.strike == 0 }
                },
                checklistCompleted = withContext(Dispatchers.Default) {
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
            updateList()
        }
        if (fromIndex >= 0 && toIndex >= 0) {
            temporaryChecklist.add(toIndex, temporaryChecklist.removeAt(fromIndex)).apply {  }
        }
    }

    fun shareChecklist(): Intent {
        val listEntries = stateSetter.value.checklistToDo
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

    fun saveNewOrEditExistingChecklist() = viewModelScope.launch {
        if(stateGetter.value.uid == 0) {
            if(stateGetter.value.header.isNotEmpty() || temporaryChecklist.isNotEmpty()) {
                withContext(Dispatchers.IO){
                    primaryViewModel.insertNote(
                        Note(
                            header = stateGetter.value.header,
                            checkList = ArrayList(temporaryChecklist)
                        )
                    )
                }
            }
        } else {
            if( stateGetter.value.fullChecklist?.header != stateGetter.value.header ||
                stateGetter.value.fullChecklist?.checkList != temporaryChecklist
            ) {
                withContext(Dispatchers.IO){
                    primaryViewModel.editNote(
                        uid = stateGetter.value.uid,
                        header = stateGetter.value.header,
                        checklist = ArrayList(temporaryChecklist),
                        category = stateGetter.value.category
                    )
                }
            }
        }
    }

    /**
     * [addEntryToChecklist] creates a checklist entry to add to [temporaryChecklist] but only
     * does so if the entry is not empty (currently does allow if blank as may be used in future
     * to add a spacer). After adding entry updates observable list, clears [checklistEntry] field
     * and brings new entry field in UI back in to view.
     * */
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
        val updateObservableChecklist = viewModelScope.launch {
            stateSetter.update { updateState ->
                updateState.copy(checklistToDo = temporaryChecklist.filter { note -> note.strike == 0 })
            }
        }

        viewModelScope.launch {
            addEntry.join()
            updateObservableChecklist.join()
            checklistEntry = TextFieldValue("")
            delay(50)
            bringIntoViewRequester.bringIntoView()
        }
    }

    /**
     * [navigateNewChecklist] updates checklist activity to checklist view and closes the
     * new entry selection in the main view.
     * */
    fun navigateNewChecklist() = viewModelScope.launch {
        stateSetter.update { updateState -> updateState.copy(navigateNewChecklist = true) }
        primaryViewModel.newEntryButton()
    }

    /**
     * [editChecklist] takes in [Note] as a parameter and passes those values to state
     * to view and edit.
     * */
    fun editChecklist(note: Note) = viewModelScope.launch {
        val checkListNewKeys = note.checkList?.map {
            CheckList(
                note = it.note,
                strike = it.strike,
                key = UUID.randomUUID()
            )
        }
        val notes = note.copy(checkList = checkListNewKeys)

        temporaryChecklist.swapAll(checklist = checkListNewKeys)
        stateSetter.update {
            currentState -> currentState.copy(
                modifiableChecklist = flow{checkListNewKeys?.toMutableList()},
                fullChecklist = notes,
                uid = note.uid!!,
                checklistToDo = checkListNewKeys?.filter { it.strike == 0 } ?: emptyList(),
                checklistCompleted = checkListNewKeys?.filter { it.strike == 1 } ?: emptyList(),
                header = note.header ?: "",
                category = note.category,
                navigateNewChecklist = true
            )
        }
    }

    /**
     * [resetValues] back to default when exiting a checklist.
     * */
     fun resetValues() = viewModelScope.launch {
        updateChecklistEntry("")
        temporaryChecklist.clear()
        stateSetter.update { updateStates ->
            updateStates.copy(
                checklistToDo = emptyList(),
                checklistCompleted = emptyList(),
                navigateNewChecklist = false,
                showMoreOptionsMenu = false,
                reArrange = false,
                fullChecklist = null,
                checklistKey = null,
                category = null,
                header = "",
                uid = 0
            )
        }
    }

    /**
     * [exitAndSave] clears focus to any textfields that might have it, which also closes keyboard
     * if open. [saveNewOrEditExistingChecklist] and then [resetValues] back to default. Allows navigating between
     * existing checklists and new checklists.
     * */
    fun exitAndSave(focusManager: FocusManager)  = viewModelScope.launch {
        focusManager.clearFocus()
        saveNewOrEditExistingChecklist()
    }

}