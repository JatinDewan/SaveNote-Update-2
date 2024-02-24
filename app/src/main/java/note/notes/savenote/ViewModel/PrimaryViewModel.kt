package note.notes.savenote.ViewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import note.notes.savenote.PersistentStorage.RoomDatabase.CheckList
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
import note.notes.savenote.PersistentStorage.RoomDatabase.NotesRepositoryImp
import note.notes.savenote.PersistentStorage.SharedPreferences.IApplicationPreferences
import note.notes.savenote.SaveNoteApplication
import note.notes.savenote.Utils.BackupAndRestore
import note.notes.savenote.Utils.BackupAndRestoreNote
import note.notes.savenote.Utils.DateUtilities
import note.notes.savenote.Utils.DateUtils
import note.notes.savenote.Utils.GetHelp
import note.notes.savenote.Utils.Help
import note.notes.savenote.Utils.NotesSearch
import note.notes.savenote.Utils.SearchNotes
import note.notes.savenote.ViewModel.model.PrimaryUiState
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale

class PrimaryViewModel(
    private val notesRepositoryImp: NotesRepositoryImp,
    private val backupAndRestore: BackupAndRestore,
    private val getHelp: Help,
    val applicationPreferences: IApplicationPreferences,
    val dateUtils: DateUtilities,
): ViewModel() {

    private val stateSetter = MutableStateFlow(PrimaryUiState())
    val stateGetter: StateFlow<PrimaryUiState> = stateSetter.asStateFlow()
    var isReady = false

    init { applicationStartProcess() }

    private fun currentDateAndTime(): String = dateUtils.getCurrentDateAndTime()

    fun help(context: Context) = getHelp.getHelp(context)

    fun backup(showBackup: Boolean) = dropDown(false).invokeOnCompletion { showBackup(showBackup) }

    fun deleteNote(uid:Int) = viewModelScope.launch(Dispatchers.IO){ notesRepositoryImp.deleteNote(uid) }

    fun noteSelector(note: Note) = viewModelScope.launch {
        if (note !in stateSetter.value.temporaryEntryHold) stateSetter.value.temporaryEntryHold.add(note) else stateSetter.value.temporaryEntryHold.remove(note)
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

    fun dropDown(dropDown: Boolean = !stateGetter.value.dropDown) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(dropDown = dropDown) }
    }

    private fun loadingScreen(showLoadingScreen: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(loadingScreen = showLoadingScreen) }
    }

    private fun showBackup(showBackup: Boolean) = viewModelScope.launch {
        stateSetter.update { updatedState -> updatedState.copy(showBackup = showBackup) }
    }

    fun processSearchRequest(searchQuery: String) {
        stateSetter.update { updateState -> updateState.copy(searchQuery = searchQuery) }
        updateSearchRequest()
    }

    fun cardFunctionSelection(
        navigateEntry:() -> Unit,
        note: Note
    ) {
        if (stateSetter.value.temporaryEntryHold.isEmpty()) navigateEntry() else noteSelector(note)
    }

    fun deleteSelected() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){ notesRepositoryImp.deleteSelected(stateSetter.value.temporaryEntryHold) }
            stateSetter.value.temporaryEntryHold.clear()
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

    suspend fun insertNote(note: Note): Flow<Int?> {
        notesRepositoryImp.insertNote(
            Note(
                header = note.header,
                note = note.note,
                date = dateUtils.getCurrentDateAndTime(),
                checkList = note.checkList,
            )
        )
        return notesRepositoryImp.getNote().map { thisNote ->
            thisNote.last().uid
        }
    }

    suspend fun editNote(
        uid: Int,
        header: String?,
        note: String? = null,
        checklist: ArrayList<CheckList>? = null,
        category: String?
    ) {
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
        dropDown(false)
        showSearchBar(true)
        viewModelScope.launch{
            delay(150)
        }.invokeOnCompletion { focusRequester.requestFocus() }
    }

    fun endSearch(focusManager: FocusManager) {
        showSearchBar(false)
        focusManager.clearFocus()
        processSearchRequest("")
    }

    fun selectAllNotes() {
        val allNotes = stateGetter.value.allEntries + stateGetter.value.favoriteEntries
        viewModelScope.launch {
            if(stateSetter.value.temporaryEntryHold.size < allNotes.size) {
                stateSetter.value.temporaryEntryHold.addAll(
                    allNotes.filter { note -> note !in stateSetter.value.temporaryEntryHold }
                )
            } else {
                stateSetter.value.temporaryEntryHold.clear()
            }
        }
    }

    fun duplicateSelectedNotes() {
        viewModelScope.launch{
            val newList = stateSetter.value.temporaryEntryHold.map { note ->
                Note(
                    header = note.header,
                    note = note.note,
                    date = dateUtils.getCurrentDateAndTime(),
                    checkList = note.checkList,
                    category = note.category
                )
            }
            withContext(Dispatchers.IO){ notesRepositoryImp.insertAll(newList) }
            stateSetter.value.temporaryEntryHold.clear()
        }
    }

    fun selectLayout(){
        viewModelScope.launch(Dispatchers.IO) {
            applicationPreferences.setLayoutInformation(!stateGetter.value.layoutView)
            applicationPreferences.getLayoutInformation.collect { layoutPage ->
                withContext(Dispatchers.Main){
                    stateSetter.update { updatedState ->
                        updatedState.copy(layoutView = layoutPage)
                    }
                }
            }
        }
    }

    fun updateTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            applicationPreferences.setTheme(
                withContext(Dispatchers.Default){
                    if (stateGetter.value.setTheme > 2) 1 else stateGetter.value.setTheme + 1
                }
            )
            applicationPreferences.getTheme.collect { theme ->
                withContext(Dispatchers.Default){
                    stateSetter.update { updatedState -> updatedState.copy(setTheme = theme) }
                }
            }
        }
    }

    fun updateSortByPreference() {
        viewModelScope.launch(Dispatchers.IO) {
            applicationPreferences.setSortByView(
                withContext(Dispatchers.Default){
                    if (stateGetter.value.sortByView > 2) 1 else stateGetter.value.sortByView + 1
                }
            )
            applicationPreferences.getSortByView.collect { getSortBy ->
                withContext(Dispatchers.Default) {
                    stateSetter.update { updateState ->
                        updateState.copy(
                            sortByView = getSortBy,
                            allEntries = updateState.allEntries
                                .sortedWith(listComparatorArrangement(getSortBy)),
                            favoriteEntries = updateState.favoriteEntries
                                .sortedWith(listComparatorArrangement(getSortBy))
                        )
                    }
                }
            }
        }
    }

    fun rateApp(context: Context){
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=note.notes.savenote")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun favouriteSelected() {
        val category = "favourite"
        viewModelScope.launch {
            stateSetter.value.temporaryEntryHold.map { note ->
                val updatedNotes = note.copy(category = if (note.category == category) null else category)

                withContext(Dispatchers.IO) { notesRepositoryImp.editNote(updatedNotes) }
            }
        }.invokeOnCompletion { stateSetter.value.temporaryEntryHold .clear() }
    }

    private fun listComparatorArrangement(comparator: Int): Comparator<Note> {
        val sortByDate = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())
        return when (comparator) {
            1 -> compareBy<Note> { note ->
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.parse(note.date)
                } else {
                    note.date?.let { date -> sortByDate.parse(date) }
                }
            }.reversed()
            2 -> compareBy { it.uid }
            else -> compareBy<Note> { it.uid }.reversed()
        }
    }

    private fun applicationStartProcess() {
        viewModelScope.launch(Dispatchers.IO) {
            val sortBy = applicationPreferences.getSortByView.first()
            val getTheme = applicationPreferences.getTheme.first()
            val getLayout = applicationPreferences.getLayoutInformation.first()

            withContext(Dispatchers.Default) {
                stateSetter.update { applyStuff ->
                    applyStuff.copy(
                        sortByView = sortBy,
                        layoutView = getLayout,
                        setTheme = getTheme
                    )
                }
            }
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.IO) {
                notesRepositoryImp.getNote().collect { note ->
                    withContext(Dispatchers.Default) {
                        stateSetter.update { filterList ->
                            filterList.copy(
                                allEntries = note
                                    .filter { currentNote -> currentNote.category == null }
                                    .sortedWith(listComparatorArrangement(filterList.sortByView)),
                                favoriteEntries = note
                                    .filter { currentNote -> currentNote.category != null }
                                    .sortedWith(listComparatorArrangement(filterList.sortByView))
                            )
                        }
                    }
                }
            }.invokeOnCompletion { isReady = true }
        }
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

            viewModelScope.launch {
                loadingScreen(true).join()
                showBackup(false).join()
                restore.join()
                withContext(Dispatchers.IO) { notesRepositoryImp.insertAll(backupAndRestore.restoreNotes) }
                loadingScreen(false)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val notesRepositoryImp = (this[APPLICATION_KEY] as SaveNoteApplication).notesRepositoryImp
                val appPreferences = (this[APPLICATION_KEY] as SaveNoteApplication).applicationPreferences
                PrimaryViewModel(
                    notesRepositoryImp = notesRepositoryImp,
                    applicationPreferences = appPreferences,
                    dateUtils = DateUtils(),
                    getHelp = GetHelp(),
                    backupAndRestore = BackupAndRestoreNote()
                )
            }
        }
    }
}