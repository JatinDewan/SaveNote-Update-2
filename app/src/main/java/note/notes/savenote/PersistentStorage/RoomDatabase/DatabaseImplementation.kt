package note.notes.savenote.PersistentStorage.RoomDatabase

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

    override suspend fun getNote(): Flow<List<Note>> = notesDao.getAll()

    override suspend fun insertNote(notes: Note) = notesDao.insert(notes)

    override suspend fun deleteNote(uid: Int?) = notesDao.delete(uid)

    override suspend fun editNote(notes: Note) = notesDao.editNote(notes)

    override suspend fun updateCategory(category: String?, uid: Int?) = notesDao.updateCategory(category, uid)

    override suspend fun deleteSelected(list: List<Note>) = notesDao.deleteItems(list)

    override suspend fun insertAll(items: List<Note>) = notesDao.insertGroup(items)

}