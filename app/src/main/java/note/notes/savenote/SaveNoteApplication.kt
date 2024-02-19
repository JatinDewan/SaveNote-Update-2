package note.notes.savenote

import android.app.Application
import note.notes.savenote.PersistentStorage.RoomDatabase.NoteDao_Impl
import note.notes.savenote.PersistentStorage.RoomDatabase.NoteDatabase
import note.notes.savenote.PersistentStorage.RoomDatabase.NotesRepositoryImp
import note.notes.savenote.PersistentStorage.SharedPreferences.ApplicationPreferences

class SaveNoteApplication: Application() {
    lateinit var notesRepositoryImp: NotesRepositoryImp
    lateinit var applicationPreferences: ApplicationPreferences
    override fun onCreate() {
        super.onCreate()
        notesRepositoryImp = NotesRepositoryImp(
            NoteDao_Impl(NoteDatabase.getDatabase(this))
        )
        applicationPreferences = ApplicationPreferences(this)
    }
}