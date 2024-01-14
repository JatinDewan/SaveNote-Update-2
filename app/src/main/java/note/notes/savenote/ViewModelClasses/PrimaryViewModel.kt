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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import note.notes.savenote.Database.roomDatabase.CheckList
import note.notes.savenote.Database.roomDatabase.NewArrayConverter
import note.notes.savenote.Database.roomDatabase.Note
import note.notes.savenote.Database.roomDatabase.NotesRepositoryImp
import note.notes.savenote.Database.sharedPreferences.ISharedPreferences
import note.notes.savenote.R
import note.notes.savenote.SaveNoteApplication
import note.notes.savenote.Utils.DateFormatter
import note.notes.savenote.Utils.DateUtilities
import note.notes.savenote.Utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale
class PrimaryViewModel(
    val notesRepositoryImp: NotesRepositoryImp,
    val sharedPref: ISharedPreferences,
    private val dateUtils: DateUtilities
): ViewModel() {

    val _uiState = MutableStateFlow(PrimaryUiState())
    val uiState: StateFlow<PrimaryUiState> = _uiState.asStateFlow()
    val temporaryEntryHold = mutableStateListOf<Note>()
    private var showSearch = mutableStateListOf<Note>()
    var isReady = false

    init {
        updateList()
    }

    private fun updateState(primaryUiStates: (PrimaryUiState) -> PrimaryUiState) {
        viewModelScope.launch{ _uiState.update { updateParameter -> primaryUiStates(updateParameter) } }
    }
    private fun showSearchBar(show: Boolean) = updateState { it.copy(showSearchBar = show) }

    fun confirmDelete(confirmDelete: Boolean) = updateState { it.copy(confirmDelete = confirmDelete) }

    fun newEntryButton(collapse: Boolean = false) = updateState { it.copy(newEntryButton = collapse) }

    fun dropDown(dropDown: Boolean) = updateState { it.copy(dropDown = dropDown) }

    fun animateTopBarVisibility(animate: Boolean) = updateState { it.copy(animateCloseBar = animate) }

    fun nestedScrollOffset(offSet: Float) = updateState { it.copy(barOffsetY = offSet) }

    fun dateAndTimeDisplay(dateAndTime: String): String = dateUtils.formatDateAndTime(dateAndTime)

    private fun currentDateAndTime(): String = dateUtils.getCurrentDateAndTime()

    fun showBackup(showBackup: Boolean){
        dropDown(false)
        updateState { it.copy(showBackup = showBackup) }
    }

    fun insertNote(
        navigateToNote:(Note) -> Unit
    ){
        val emptyNote = Note(
            uid = null,
            header = null,
            note = null,
            date = dateUtils.getCurrentDateAndTime(),
            checkList = null,
            category = null
        )
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

//        viewModelScope.launch {
//            notesRepositoryImp.getNote().collect {
//                if(it.isNotEmpty()) {
//                    if(
//                        it.last().note.isNullOrEmpty() &&
//                        it.last().header.isNullOrEmpty() &&
//                        it.last().checkList.isNullOrEmpty()
//                    )   navigateToNote(it.last())
//                }
//            }
//        }
    }

    fun getNote(note:(List<Note>) -> Unit){
        viewModelScope.launch{
            notesRepositoryImp.getNote().collect { currentNote -> note(currentNote) }
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

    fun deleteNote(uid: Int?){
        viewModelScope.launch(Dispatchers.IO){ notesRepositoryImp.deleteNote(uid) }
    }

    fun containerSize():String = if(temporaryEntryHold.size > 99) "99+" else temporaryEntryHold.size.toString()

    fun cardFunctionSelection(returnOperationOne:() -> Unit, returnOperationTwo: () -> Unit) {
        viewModelScope.launch{ if (temporaryEntryHold.isEmpty()) returnOperationOne() else returnOperationTwo() }
    }

    fun searchQuery(query:String) {
        updateState { it.copy(searchQuery = query) }
        searchNotes()
    }

    fun updateCurrentPageView1(page: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setLayoutInformation(!page)
            sharedPref.getLayoutInformation.collect { currentPage ->
                updateState { it.copy(currentPage = currentPage) }
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
    fun duplicateSelectedNotes() {
        viewModelScope.launch{
            temporaryEntryHold.forEach { note ->
                notesRepositoryImp.insertNote(
                    notes = Note(
                        uid = null,
                        header = note.header,
                        note = note.note,
                        date = dateUtils.getCurrentDateAndTime(),
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
        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.setSortByView(if(changeView > 2) 1 else changeView + 1)
            sharedPref.getSortByView.collect{ sortByView ->
                updateState { view -> view.copy(sortByView = sortByView) }
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
        viewModelScope.launch(Dispatchers.IO) {
            notesRepositoryImp.getNote().collect { notes ->
                when(uiState.value.searchQuery.isNotEmpty()){
                    true -> {
                        updateState {
                            it.copy(
                                searchEntries = notes.filter { note ->
                                    note.note?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                    note.header?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                    finderChecklist(uiState.value.searchQuery.lowercase(), note)
                                }
                            )
                        }
                    }
                    else -> updateState { it.copy(searchEntries = emptyList()) }
                }
            }
        }
    }

    private fun updateList() {
        viewModelScope.launch(Dispatchers.IO) {
            notesRepositoryImp.getNote().collect { note ->
                notesRepositoryImp.deleteSelected(
                    note.filter { entry ->
                        entry.note.isNullOrEmpty() &&
                        entry.checkList.isNullOrEmpty() &&
                        entry.header.isNullOrEmpty()
                    }
                )
                updateState {
                    it.copy(
                        allEntries = note.filter { entry -> entry.category == null },
                        favoriteEntries = note.filter { entry -> entry.category != null }
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            sharedPref.getLayoutInformation.collect{
                currentPage -> updateState { it.copy(currentPage = currentPage) }
            }
            sharedPref.getSortByView.collect{
                view -> updateState { it.copy(sortByView = view) }
            }
        }

        isReady = true
    }

//    fun backupNotes(context: Context){
//        val appDatabase = NoteDatabase.getDatabase(context = context)
//        appDatabase.
//
//    }

//    fun backupDatabase(context: Context) {
//        val appDatabase: NoteDatabase = NoteDatabase.getDatabase(context)
//        appDatabase.close()
//        val dbfile = context.getDatabasePath("DATABASE_NAME")
//        val sddir = File()
//        val sdir: File = File(getFilePath(context, 0), "backup")
//        val fileName: String = FILE_NAME + getDateFromMillisForBackup(System.currentTimeMillis())
//        val sfpath = sdir.path + File.separator + fileName
//        if (!sdir.exists()) {
//            sdir.mkdirs()
//        } else {
//            //Directory Exists. Delete a file if count is 5 already. Because we will be creating a new.
//            //This will create a conflict if the last backup file was also on the same date. In that case,
//            //we will reduce it to 4 with the function call but the below code will again delete one more file.
//            checkAndDeleteBackupFile(sdir, sfpath)
//        }
//        val savefile = File(sfpath)
//        if (savefile.exists()) {
//            Log.d(LOGGER, "File exists. Deleting it and then creating new file.")
//            savefile.delete()
//        }
//        try {
//            if (savefile.createNewFile()) {
//                val buffersize = 8 * 1024
//                val buffer = ByteArray(buffersize)
//                var bytes_read = buffersize
//                val savedb: OutputStream = FileOutputStream(sfpath)
//                val indb: InputStream = FileInputStream(dbfile)
//                while (indb.read(buffer, 0, buffersize).also { bytes_read = it } > 0) {
//                    savedb.write(buffer, 0, bytes_read)
//                }
//                savedb.flush()
//                indb.close()
//                savedb.close()
//                val sharedPreferences = context.getSharedPreferences(SHAREDPREF, MODE_PRIVATE)
//                sharedPreferences.edit().putString("backupFileName", fileName).apply()
//                updateLastBackupTime(sharedPreferences)
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//            Log.d(LOGGER, "ex: $e")
//        }
//    }


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
                            if (!note?.note.isNullOrEmpty() || !note?.checkList.isNullOrEmpty()) {
                                temporaryList.add(
                                    Note(
                                        uid = null,
                                        header = note?.header,
                                        note = note?.note,
                                        date = note?.date,
                                        checkList = note?.checkList,
                                        category = note?.category
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
                    sharedPref = sharedPref,
                    dateUtils = DateUtils(DateFormatter())
                )
            }
        }
    }
}