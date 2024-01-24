package note.notes.savenote.ViewModelClasses

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
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
import note.notes.savenote.PersistentStorage.roomDatabase.NotesRepositoryImp
import note.notes.savenote.PersistentStorage.sharedPreferences.ISharedPreferences
import note.notes.savenote.R
import note.notes.savenote.SaveNoteApplication
import note.notes.savenote.Utils.BackupAndRestore
import note.notes.savenote.Utils.BackupAndRestoreNote
import note.notes.savenote.Utils.DateUtilities
import note.notes.savenote.Utils.DateUtils
import note.notes.savenote.Utils.GetHelp
import note.notes.savenote.Utils.Help
import note.notes.savenote.Utils.NotesSearch
import note.notes.savenote.Utils.SearchNotes
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class PrimaryViewModel(
    val notesRepositoryImp: NotesRepositoryImp,
    val sharedPref: ISharedPreferences,
    private val getHelp: Help,
    private val dateUtils: DateUtilities,
): ViewModel() {

    val stateSetter = MutableStateFlow(PrimaryUiState())
    val statGetter: StateFlow<PrimaryUiState> = stateSetter.asStateFlow()
    private val backupAndRestore : BackupAndRestore = BackupAndRestoreNote(viewModelScope)
    val temporaryEntryHold = mutableStateListOf<Note>()
    var isReady = false

    init {
        updateList()
    }

    private fun updateState(
        dispatcher:CoroutineDispatcher = Dispatchers.Default,
        primaryUiStates: (PrimaryUiState) -> PrimaryUiState
    ) {
        viewModelScope.launch(dispatcher) {
            stateSetter.update { updateParameter -> primaryUiStates(updateParameter) }
        }
    }
    private fun showSearchBar(show: Boolean) = updateState { it.copy(showSearchBar = show) }

    fun confirmDelete(confirmDelete: Boolean) = updateState { it.copy(confirmDelete = confirmDelete) }

    fun newEntryButton(collapse: Boolean = false) = updateState { it.copy(newEntryButton = collapse) }

    fun dropDown(dropDown: Boolean) = updateState { it.copy(dropDown = dropDown) }

    private fun currentDateAndTime(): String = dateUtils.getCurrentDateAndTime()

    fun deleteNote(uid: Int?) = viewModelScope.launch(Dispatchers.IO) { notesRepositoryImp.deleteNote(uid) }

    fun containerSize():String = if(temporaryEntryHold.size > 99) "99+" else temporaryEntryHold.size.toString()

    fun help(context: Context) = getHelp.getHelp(context)

    fun processSearchRequest(searchQuery: String){
        stateSetter.update { it.copy(searchQuery = searchQuery) }
        updateSearchRequest()
    }

    fun showBackup(showBackup: Boolean){
        dropDown(false)
        updateState { it.copy(showBackup = showBackup) }
    }

    fun dateAndTimeDisplay(dateAndTime: String, note: Note): String {
        return dateUtils.formatDateAndTime(
            dateAndTime = dateAndTime,
            replaceNotesDate = { date ->
                viewModelScope.launch(Dispatchers.IO) {
                    notesRepositoryImp.editNote(
                        Note(
                            uid = note.uid,
                            header = note.header,
                            note = note.note,
                            date = date,
                            checkList = note.checkList,
                            category = note.category
                        )
                    )
                }
            }
        )
    }

    fun insertNote(
        navigateToNote:(Note) -> Unit
    ){
        val emptyNote = Note(date = currentDateAndTime())
        viewModelScope.launch(Dispatchers.IO){
            notesRepositoryImp.insertNote(emptyNote)

            notesRepositoryImp.getNote().collect {
                if(it.isNotEmpty()) {
                    if(
                        it.last().note.isNullOrEmpty() &&
                        it.last().header.isNullOrEmpty() &&
                        it.last().checkList.isNullOrEmpty()
                    )   navigateToNote(it.last())
                }
            }
        }
    }

    fun editNote(
        uid: Int,
        header: String?,
        note: String? = null,
        checklist: ArrayList<CheckList>? = null,
        category: String?

    ) {
        viewModelScope.launch(Dispatchers.IO){
            notesRepositoryImp.editNote(
                Note(
                    uid = uid,
                    header = header,
                    note = note,
                    checkList = checklist,
                    date = currentDateAndTime(),
                    category = category
                )
            )
        }
    }

    fun cardFunctionSelection(navigateEntry:() -> Unit, note: Note) {
        viewModelScope.launch{ if (temporaryEntryHold.isEmpty()) navigateEntry() else deleteTally(note) }
    }

    fun selectLayout(page: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setLayoutInformation(!page)
            sharedPref.getLayoutInformation.collect { currentPage ->
                stateSetter.update { it.copy(currentPage = currentPage) }
            }
        }
    }

    fun updateSearchRequest() {
        val searchNotes : NotesSearch = SearchNotes()
        viewModelScope.launch {
            stateSetter.update {
                updateParameter -> updateParameter.copy(
                    searchEntries = searchNotes.searchAllNotes(
                        searchNotes = statGetter.value.allEntries + statGetter.value.favoriteEntries,
                        searchQuery = statGetter.value.searchQuery
                    )
                )
            }
        }
    }

    fun startSearch(focusRequester: FocusRequester) {
        viewModelScope.launch{
            dropDown(false)
            showSearchBar(true)
            delay(50)
            focusRequester.requestFocus()
        }
    }

    fun endSearch(focusManager: FocusManager) {
        showSearchBar(false)
        focusManager.clearFocus()
        processSearchRequest("")
    }

    fun selectAllNotes() {
        viewModelScope.launch{
            when {
                temporaryEntryHold.containsAll(
                    statGetter.value.allEntries + statGetter.value.favoriteEntries
                ) -> temporaryEntryHold.clear()
                else -> {
                    statGetter.value.allEntries.map {
                        if (it !in temporaryEntryHold) temporaryEntryHold.add(it)
                    }
                    statGetter.value.favoriteEntries.map {
                        if(it !in temporaryEntryHold) temporaryEntryHold.add(it)
                    }
                }
            }
        }
    }

    fun duplicateSelectedNotes() {
        viewModelScope.launch{
            val newList = temporaryEntryHold.map { note ->
                Note(
                    uid = null,
                    header = note.header,
                    note = note.note,
                    date = dateUtils.getCurrentDateAndTime(),
                    checkList = note.checkList,
                    category = note.category
                )
            }
            notesRepositoryImp.insertAll(newList)
            temporaryEntryHold.clear()
        }
    }


    fun deleteTally(note: Note) {
        viewModelScope.launch{
            if (temporaryEntryHold.contains(note)) {
                temporaryEntryHold.remove(note)
            } else {
                temporaryEntryHold.add(note)
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            notesRepositoryImp.deleteSelected(temporaryEntryHold)
            temporaryEntryHold.clear()
        }
    }
    fun sortByString(): Int{
        return when(statGetter.value.sortByView) {
            1 -> R.string.LastEdit
            2 -> R.string.Oldest
            else -> R.string.Newest
        }
    }

    fun updateSortByPreference(changeView: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setSortByView(if(changeView > 2) 1 else changeView + 1)
            sharedPref.getSortByView.collect { sortByView ->
                stateSetter.update { view -> view.copy(sortByView = sortByView) }
            }
        }
    }

    private fun isCategory(current: String?, category: String): String? {
        return when(current) {
            category -> null
            null -> category
            else  ->  current
        }
    }

    fun favouriteSelected(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            temporaryEntryHold.map { note ->
                notesRepositoryImp.updateCategory(
                    category = isCategory(note.category,category),
                    uid = note.uid
                )
            }
            delay(10)
            temporaryEntryHold.clear()
        }
    }

    fun compareLastEdit(): Comparator<Note> {
        val sortByDate = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())
        return when (statGetter.value.sortByView) {
            1 -> compareBy<Note> { note ->
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.parse(note.date).atZone(ZoneId.systemDefault())
                } else {
                    sortByDate.parse(note.date!!)
                }
            }.reversed()
            2 -> compareBy { it.uid }
            else -> compareBy<Note> { it.uid }.reversed()
        }
    }

    private fun updateList() {

        val fillList = viewModelScope.launch {
            withContext(Dispatchers.IO){
                notesRepositoryImp.getNote().collect { note ->
                    withContext(Dispatchers.Default) {
                        stateSetter.update { filterList ->
                            filterList.copy(
                                allEntries = note.filter { entry -> entry.category == null },
                                favoriteEntries = note.filter { entry -> entry.category != null }
                            )
                        }
                    }
                }
            }
        }

        val setMainPageLayout = viewModelScope.launch(Dispatchers.IO) {
            sharedPref.getLayoutInformation.collect { currentPage ->
                updateState { it.copy(currentPage = currentPage) }
            }
        }

        val setSortByOption = viewModelScope.launch {
            sharedPref.getSortByView.collect { view ->
                updateState { it.copy(sortByView = view) }
            }
        }

        viewModelScope.launch {
            fillList.join()
            setMainPageLayout.join()
            setSortByOption.join()
            isReady = true
        }
    }

    fun backUpNotes(uri: Uri?, context: Context){
        if (uri != null) {
            backupAndRestore.backUp(
                uri = uri,
                context = context,
                noteEntries = statGetter.value.allEntries + statGetter.value.favoriteEntries
            )
        }
    }

    fun restoreNotes(uri: Uri?, context: Context) {
        if (uri != null) {
            val restore = backupAndRestore.restore(
                uri = uri,
                context = context,
                noteEntries = statGetter.value.allEntries + statGetter.value.favoriteEntries
            )
            viewModelScope.launch(Dispatchers.IO){
                notesRepositoryImp.insertAll(restore)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val notesRepositoryImp = (this[APPLICATION_KEY] as SaveNoteApplication).notesRepositoryImp
                val sharedPref = (this[APPLICATION_KEY] as SaveNoteApplication).sharedPref
                PrimaryViewModel(
                    notesRepositoryImp = notesRepositoryImp,
                    sharedPref = sharedPref,
                    dateUtils = DateUtils(),
                    getHelp = GetHelp()
                )
            }
        }
    }
}