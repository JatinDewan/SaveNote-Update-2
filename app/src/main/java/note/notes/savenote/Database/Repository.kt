package note.notes.savenote.Database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface NotesRepository {

    suspend fun getNote(): Flow<List<Note>>

    suspend fun insertNote(notes: Note)

    suspend fun deleteNote(notes: Note)

    suspend fun editNote(notes: Note)

    suspend fun updateCategory(category: String?, uid: Int?)

    suspend fun deleteSelected(list: List<Note>)

    suspend fun insertAll(items: List<Note>)

}

class NotesRepositoryImp (private val notesDao: NoteDao) : NotesRepository {

    override suspend fun getNote(): Flow<List<Note>> {
        return withContext(Dispatchers.IO) { notesDao.getAll() }
    }

    override suspend fun insertNote(notes: Note) {
        return notesDao.insert(notes)
    }

    override suspend fun deleteNote(notes: Note) {
        return notesDao.delete(notes)
    }

    override suspend fun editNote(notes: Note) {
        return notesDao.editNote(notes)
    }

    override suspend fun updateCategory(category: String?, uid: Int?) {
        return notesDao.updateCategory(category, uid)
    }

    override suspend fun deleteSelected(list: List<Note>) {
        return notesDao.deleteItems(list)
    }

    override suspend fun insertAll(items: List<Note>) {
        return notesDao.insertGroup(items)
    }

}