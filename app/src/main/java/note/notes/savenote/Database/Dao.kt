package note.notes.savenote.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAll(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Insert
    suspend fun insertGroup(items: List<Note>)
    @Delete
    suspend fun delete(note: Note)

    @Update
    suspend fun editNote (note: Note)

    @Query("UPDATE note SET category = :category WHERE uid =:uid")
    suspend fun updateCategory(category: String?, uid: Int?)

    @Delete
    suspend fun deleteItems(items: List<Note>)

}