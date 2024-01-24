package note.notes.savenote.PersistentStorage.roomDatabase

import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    suspend fun getNote(): Flow<List<Note>>

    suspend fun insertNote(notes: Note)

    suspend fun deleteNote(uid: Int?)

    suspend fun editNote(notes: Note)

    suspend fun updateCategory(category: String?, uid: Int?)

    suspend fun deleteSelected(list: List<Note>)

    suspend fun insertAll(items: List<Note>)

}

class NotesRepositoryImp (private val notesDao: NoteDao) : NotesRepository {

    override suspend fun getNote(): Flow<List<Note>> {
        return notesDao.getAll()
    }

    override suspend fun insertNote(notes: Note) {
        return notesDao.insert(notes)
    }

    override suspend fun deleteNote(uid: Int?) {
        return notesDao.delete(uid)
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