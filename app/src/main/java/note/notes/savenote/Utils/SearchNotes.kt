package note.notes.savenote.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import note.notes.savenote.PersistentStorage.RoomDatabase.Note


interface NotesSearch {

    suspend fun searchAllNotes(searchNotes: List<Note>, searchQuery: String): List<Note>

}

class SearchNotes : NotesSearch {

    override suspend fun searchAllNotes(searchNotes: List<Note>, searchQuery: String): List<Note> {
        var filteredList: List<Note> = emptyList()

        if(searchQuery.isNotEmpty()) {
            withContext(Dispatchers.Default){
                filteredList = searchNotes.filter { note ->
                    note.note?.lowercase()?.contains(searchQuery.lowercase()) == true ||
                    note.header?.lowercase()?.contains(searchQuery.lowercase()) == true ||
                    note.checkList?.any { entry -> entry.note.lowercase().contains(searchQuery.lowercase()) } == true
                }
            }
        } else {
            filteredList = emptyList()
        }

        return filteredList
    }
}