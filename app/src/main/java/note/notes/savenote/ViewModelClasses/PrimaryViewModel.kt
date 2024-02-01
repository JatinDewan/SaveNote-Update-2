package note.notes.savenote.ViewModelClasses

import android.content.Context
import android.content.Intent
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

    private val stateSetter = MutableStateFlow(PrimaryUiState())
    val stateGetter: StateFlow<PrimaryUiState> = stateSetter.asStateFlow()
    private val backupAndRestore : BackupAndRestore = BackupAndRestoreNote()
    val temporaryEntryHold = mutableStateListOf<Note>()
    var isReady = false

    init {
        updateList()
    }

    private fun showSearchBar(showSearchBar: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(showSearchBar = showSearchBar) }
    }

    fun confirmDelete(confirmDelete: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(confirmDelete = confirmDelete) }
    }

    fun newEntryButton(collapseNewEntry: Boolean = false) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(newEntryButton = collapseNewEntry) }
    }

    fun dropDown(dropDown: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(dropDown = dropDown) }
    }

    private fun loadingScreen(showLoadingScreen: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(loadingScreen = showLoadingScreen) }
    }

    private fun showBackup(showBackup: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(showBackup = showBackup) }
    }

    private fun layoutInformation() = viewModelScope.launch(Dispatchers.IO) {
        sharedPref.getLayoutInformation.collect { layoutInformation ->
            stateSetter.update { updatedState -> updatedState.copy(layoutView = layoutInformation) }
        }
    }

    private fun sortByView() = viewModelScope.launch(Dispatchers.IO) {
        sharedPref.getSortByView.collect { sortByView ->
            stateSetter.update { updatedState -> updatedState.copy(sortByView = sortByView) }
        }
    }

    private fun currentDateAndTime(): String = dateUtils.getCurrentDateAndTime()

    fun deleteNote(uid: Int?) = viewModelScope.launch(Dispatchers.IO) { notesRepositoryImp.deleteNote(uid) }

    fun containerSize():String = if(temporaryEntryHold.size > 99) "99+" else temporaryEntryHold.size.toString()

    fun help(context: Context) = getHelp.getHelp(context)

    fun processSearchRequest(searchQuery: String){
        stateSetter.update { updateState -> updateState.copy(searchQuery = searchQuery) }
        updateSearchRequest()
    }

    fun backup(showBackup: Boolean){
        viewModelScope.launch {
            dropDown(false).join()
            showBackup(showBackup)
        }
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

    fun insertNote(navigateToNote:(Note) -> Unit) {
        val emptyNote = Note(date = currentDateAndTime())
        viewModelScope.launch(Dispatchers.IO){
            notesRepositoryImp.insertNote(emptyNote)
            notesRepositoryImp.getNote().collect {
                if(it.isNotEmpty()) {
                    if(
                        it.last().note.isNullOrEmpty() &&
                        it.last().header.isNullOrEmpty() &&
                        it.last().checkList.isNullOrEmpty()
                    ){
                        navigateToNote(it.last())
                        withContext(Dispatchers.Main){ newEntryButton() }

                    }
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
        viewModelScope.launch(Dispatchers.Main){ if (temporaryEntryHold.isEmpty()) navigateEntry() else deleteTally(note) }
    }

    fun selectLayout(page: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setLayoutInformation(!page)
            layoutInformation()
        }
    }

    private fun updateSearchRequest() {
        val searchNotes : NotesSearch = SearchNotes()
        viewModelScope.launch {
            stateSetter.update {
                updateParameter -> updateParameter.copy(
                    searchEntries = searchNotes.searchAllNotes(
                        searchNotes = stateGetter.value.allEntries + stateGetter.value.favoriteEntries,
                        searchQuery = stateGetter.value.searchQuery
                    )
                )
            }
        }
    }

    fun startSearch(focusRequester: FocusRequester) {
        viewModelScope.launch{
            dropDown(false)
            showSearchBar(true)
            delay(150)
            focusRequester.requestFocus()
        }
    }

    fun endSearch(focusManager: FocusManager) {
        showSearchBar(false)
        focusManager.clearFocus()
        processSearchRequest("")
    }

    fun selectAllNotes() {
        viewModelScope.launch {
            if(temporaryEntryHold.size < (stateGetter.value.allEntries + stateGetter.value.favoriteEntries).size) {
                temporaryEntryHold.addAll(
                    stateGetter.value.allEntries + stateGetter.value.favoriteEntries.filter { note ->
                        note !in temporaryEntryHold
                    }
                )
            } else {
                temporaryEntryHold.clear()
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
            withContext(Dispatchers.IO){
                notesRepositoryImp.insertAll(newList)
            }
            temporaryEntryHold.clear()
        }
    }


    fun deleteTally(note: Note) {
        viewModelScope.launch {
            if (temporaryEntryHold.contains(note)) {
                temporaryEntryHold.remove(note)
            } else {
                temporaryEntryHold.add(note)
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                notesRepositoryImp.deleteSelected(temporaryEntryHold)
            }
            temporaryEntryHold.clear()
        }
    }
    fun sortByString(): Int{
        return when(stateGetter.value.sortByView) {
            1 -> R.string.LastEdit
            2 -> R.string.Oldest
            else -> R.string.Newest
        }
    }

    fun updateSortByPreference(changeView: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setSortByView(if(changeView > 2) 1 else changeView + 1)
            sortByView()
        }
    }

    private fun isCategory(current: String?, category: String): String? {
        return when(current) {
            category -> null
            null -> category
            else  ->  current
        }
    }

    fun rateApp(context: Context){
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=note.notes.savenote")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun favouriteSelected(category: String) {
        val selectFavourites = viewModelScope.launch(Dispatchers.IO) {
            temporaryEntryHold.map { note ->
                notesRepositoryImp.updateCategory(
                    category = isCategory(note.category,category),
                    uid = note.uid
                )
            }
        }

        viewModelScope.launch {
            selectFavourites.join()
            temporaryEntryHold.clear()
        }
    }

    fun compareLastEdit(): Comparator<Note> {
        val sortByDate = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())
        return when (stateGetter.value.sortByView) {
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
        val fillList = viewModelScope.launch(Dispatchers.IO) {
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

        val sort = viewModelScope.launch(Dispatchers.IO) {
            sharedPref.getSortByView.collect { sortByView ->
                stateSetter.update { updatedState -> updatedState.copy(sortByView = sortByView) }
            }
        }

        val layout = viewModelScope.launch(Dispatchers.IO) {
            sharedPref.getLayoutInformation.collect { layoutInformation ->
                stateSetter.update { updatedState -> updatedState.copy(layoutView = layoutInformation) }
            }
        }

        viewModelScope.launch  {
            fillList.join()
            sort.join()
            layout.join()
        }
        isReady = true
    }

    fun backUpNotes(uri: Uri?, context: Context) {
        if (uri != null) {
            val backUp = viewModelScope.launch {
                backupAndRestore.backUp(
                    uri = uri,
                    context = context,
                    noteEntries = stateGetter.value.allEntries + stateGetter.value.favoriteEntries
                )
            }

            viewModelScope.launch{
                loadingScreen(true).join()
                showBackup(false).join()
                backUp.join()
                loadingScreen(false).join()
            }
        }
    }

    fun restoreNotes(uri: Uri?, context: Context) {
        if (uri != null) {
            val restore = viewModelScope.launch {
                backupAndRestore.restore(
                    uri = uri,
                    context = context,
                    noteEntries = stateGetter.value.allEntries + stateGetter.value.favoriteEntries
                )
            }

            viewModelScope.launch(Dispatchers.Main) {
                loadingScreen(true).join()
                showBackup(false).join()
                restore.join()
                withContext(Dispatchers.IO) {
                    notesRepositoryImp.insertAll(backupAndRestore.restoreNotes)
                }
                loadingScreen(false)
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