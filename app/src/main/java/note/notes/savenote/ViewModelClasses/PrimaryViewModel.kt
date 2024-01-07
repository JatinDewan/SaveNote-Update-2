package note.notes.savenote.ViewModelClasses

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import note.notes.savenote.Database.NewArrayConverter
import note.notes.savenote.Database.Note
import note.notes.savenote.Database.NotesRepositoryImp
import note.notes.savenote.Database.SharedPref
import note.notes.savenote.R
import note.notes.savenote.SaveNoteApplication
import note.notes.savenote.Utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale

class PrimaryViewModel(
    val notesRepositoryImp: NotesRepositoryImp,
    val sharedPref: SharedPref,
    val date: DateUtils = DateUtils()
): ViewModel() {

    val _uiState = MutableStateFlow(PrimaryUiState())
    val uiState: StateFlow<PrimaryUiState> = _uiState.asStateFlow()
    val temporaryEntryHold = mutableStateListOf<Note>()
    private var showSearch = mutableStateListOf<Note>()
    var isReady = false

    init {
//        notesRepositoryImp
        initialiseRepository()
        updateList()
        updateSortByView()
        updatePage()
        deleteAllEmpty()
        isReady = true
    }

    private fun showSearchBar(show: Boolean) = _uiState.update { currentState -> currentState.copy(showSearchBar = show) }

    fun containerSize():String = if(temporaryEntryHold.size > 99) "99+" else temporaryEntryHold.size.toString()

    private fun initialiseRepository() {
        viewModelScope.launch(Dispatchers.IO){ notesRepositoryImp }
    }

    fun showBackup(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(showBackup = boolean) }
    }

    fun dropDown(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(dropDown = boolean) }
    }

    fun showSortBy(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(showSortBy = boolean) }
    }

    fun cardFunctionSelection(returnOperationOne:() -> Unit, returnOperationTwo: () -> Unit) {
        viewModelScope.launch{ if (temporaryEntryHold.isEmpty()) returnOperationOne() else returnOperationTwo() }
    }

    fun confirmDelete(confirmDelete: Boolean) {
        _uiState.update { currentState -> currentState.copy(confirmDelete = confirmDelete) }
    }

    fun searchQuery(query:String) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(searchQuery = query) }
            searchNotes()
        }
    }

    fun newEntryButton(collapse: Boolean = false) {
        _uiState.update { currentState -> currentState.copy(newEntryButton = collapse) }
    }

    fun nestedScrollOffset(offSet: Float) {
        _uiState.update { currentState -> currentState.copy(barOffsetY = offSet) }
    }

    fun animateTopBarVisibility(animate: Boolean) {
        _uiState.update { currentState -> currentState.copy(animateCloseBar = animate) }
    }

    private fun updatePage(){
        viewModelScope.launch(Dispatchers.IO){
            sharedPref.getLayout.collect { currentPage ->
                _uiState.update { currentState -> currentState.copy(currentPage = currentPage) }
            }
        }
    }

    fun updateCurrentPageView(page: Boolean){
        viewModelScope.launch {
            sharedPref.saveLayout(page)
            updatePage()
        }
    }

    private fun updateSortByView(){
        viewModelScope.launch(Dispatchers.IO){
            sharedPref.getView.collect {
                _uiState.update { currentState -> currentState.copy(sortByView = it) }
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
        searchQuery("")
        showSearch.clear()
    }

    fun selectAllNotes() {
        viewModelScope.launch{
            when {
                temporaryEntryHold.containsAll(uiState.value.allEntries) &&
                temporaryEntryHold.containsAll(uiState.value.favoriteEntries) -> temporaryEntryHold.clear()
                else -> {
                    uiState.value.allEntries.forEach { if (it !in temporaryEntryHold) temporaryEntryHold.add(it) }
                    uiState.value.favoriteEntries.forEach { if(it !in temporaryEntryHold) temporaryEntryHold.add(it) }
                }
            }
        }
    }

//    This will need a loading screen as it may take time and will crash app if not
    
    fun duplicateSelectedNotes(){
        viewModelScope.launch{
            temporaryEntryHold.forEach {
                note -> notesRepositoryImp.insertNote(
                    notes = Note(
                        uid = null,
                        header = note.header,
                        note = note.note,
                        date = date.current,
                        checkList = note.checkList,
                        category = note.category
                    )
                )
            }
            temporaryEntryHold.clear()
        }
    }

    fun help(context: Context){
        val brand = Build.BRAND
        val device = Build.DEVICE
        val model = Build.MODEL
        try{
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.type = "vnd.android.cursor.item/email"
            sendEmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("savenoteapp@gmail.com"))
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, "SaveNote - Help / Feedback - ($brand $device $model)")
            ContextCompat.startActivity(context, sendEmail, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(context, "Please use a valid Email Client", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAllEmpty() {
        viewModelScope.launch {
            notesRepositoryImp.getNote().onEach { notes ->
                notes.forEach { entry ->
                    if (entry.note.isNullOrEmpty() && entry.checkList.isNullOrEmpty() && entry.header.isNullOrEmpty()) {
                        notesRepositoryImp.deleteNote(entry)
                    }
                }
            }
        }
    }

    fun deleteTally(note: Note) {
        viewModelScope.launch{
            if (temporaryEntryHold.contains(note)) temporaryEntryHold.remove(note) else temporaryEntryHold.add(note)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            notesRepositoryImp.deleteSelected(temporaryEntryHold)
            temporaryEntryHold.clear()
        }
    }
    fun sortByString(): Int{
        return when(uiState.value.sortByView) {
            1 -> R.string.LastEdit
            2 -> R.string.Oldest
            else -> R.string.Newest
        }
    }

    fun updateCurrentPageView(changeView: Int) {
        val returnNumber: Int = if(changeView > 2) 1 else changeView + 1
        viewModelScope.launch {
            sharedPref.saveSortView(returnNumber)
            updatePage()
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
        viewModelScope.launch {
            temporaryEntryHold.forEach { note ->
                notesRepositoryImp.updateCategory(
                    isCategory(note.category,category),
                    note.uid
                )
            }
            delay(10)
            temporaryEntryHold.clear()
        }
    }

    fun compareLastEdit(): Comparator<Note> {
        val sortByDate = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())
        return when(uiState.value.sortByView){
            1 -> compareBy<Note> { note -> note.date?.let { date -> sortByDate.parse(date) } }.reversed()
            2 -> compareBy { it.uid }
            else -> compareBy<Note> { it.uid }.reversed()
        }
    }

    private fun searchNotes() {
        viewModelScope.launch {
            notesRepositoryImp.getNote().collect {
                when(uiState.value.searchQuery.isNotEmpty()){
                    true -> {
                        _uiState.update { update ->
                            update.copy( searchEntries = it.filter { note ->
                                    note.note?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                    note.header?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                    finderChecklist(uiState.value.searchQuery.lowercase(), note)
                                }
                            )
                        }
                    }
                    else -> _uiState.update { update -> update.copy(searchEntries = emptyList()) }
                }

            }
        }
    }

    private fun updateList() {
        viewModelScope.launch {
            notesRepositoryImp.getNote().collect { note ->
                _uiState.update { update ->
                    update.copy(allEntries = note.filter { entry -> entry.category == null })
                }
                _uiState.update { update ->
                    update.copy(favoriteEntries = note.filter { entry -> entry.category != null })
                }
            }
        }
    }

    fun backUpNotes(uri: Uri?, context: Context){
        val collectList = (uiState.value.allEntries + uiState.value.favoriteEntries)
        val writeTo = uri?.let { context.contentResolver.openOutputStream(it,"rw") }

        try{
            viewModelScope.launch(Dispatchers.IO) {
                writeTo?.bufferedWriter()?.use {
                    it.write("****FileVerificationTag****")
                    it.write("****CustomListSplitMethod****")
                    collectList.forEach { note ->
                        it.write(NewArrayConverter().fromString(note))
                        it.write("****CustomListSplitMethod****")
                    }
                }
            }
        } catch (e: Exception){
            Toast.makeText(context, "Invalid Location", Toast.LENGTH_SHORT).show()
        } finally {
            writeTo?.close()
        }
    }

    fun restoreNotes(uri: Uri?, context: Context) {
        val temporaryList = mutableListOf<Note>()
        val createStream = uri?.let { context.contentResolver.openInputStream(it) }
        try{
            val openReader = createStream?.bufferedReader().use { it?.readText() }
            if (openReader?.split("****CustomListSplitMethod****")?.first() == "****FileVerificationTag****") {
                viewModelScope.launch(Dispatchers.IO) {
                    openReader.split("****CustomListSplitMethod****").drop(1).forEach { entries ->
                        val note = NewArrayConverter().toString(entries)
                        if (note !in uiState.value.allEntries + uiState.value.favoriteEntries) {
                            if (!note.note.isNullOrEmpty() || !note.checkList.isNullOrEmpty()) {
                                temporaryList.add(
                                    Note(
                                        uid = null,
                                        header = note.header,
                                        note = note.note,
                                        date = note.date,
                                        checkList = note.checkList,
                                        category = note.category
                                    )
                                )
                            }
                        }
                    }
                    notesRepositoryImp.insertAll(temporaryList)
                }
                Toast.makeText(context, "Transfer Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Incompatible File Type", Toast.LENGTH_SHORT).show()
            }
        }catch(e: Exception){
            Toast.makeText(context, "Incompatible File Type", Toast.LENGTH_SHORT).show()
        } finally {
            createStream?.close()
        }
    }

    private fun finderChecklist (string: String, note: Note):Boolean {
        var checklistContainsEntry = false
        if (note.checkList != null) {
            note.checkList.forEach { if (it.note.lowercase().contains(string.lowercase())) checklistContainsEntry = true }
        }
        return checklistContainsEntry
    }

    fun collapsingBarConnection(canBarScroll: Boolean): NestedScrollConnection {
        val offSet = 162f
        return object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                viewModelScope.launch{
                    val deltaY = available.y / offSet
                    val clampedOffset = (uiState.value.barOffsetY / offSet - deltaY).coerceIn(0f, 1f)
                    if(canBarScroll){ nestedScrollOffset(clampedOffset * offSet) }
                    animateTopBarVisibility(false)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                viewModelScope.launch{
                    nestedScrollOffset(if (canBarScroll && uiState.value.barOffsetY >= offSet/2 ) offSet else 0f)
                    animateTopBarVisibility(true)
                }
                return super.onPostFling(consumed, available)
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
                    sharedPref = sharedPref
                )
            }
        }
    }
}