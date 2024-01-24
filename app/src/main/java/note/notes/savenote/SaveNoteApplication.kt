package note.notes.savenote

import android.app.Application
import note.notes.savenote.PersistentStorage.roomDatabase.NoteDao_Impl
import note.notes.savenote.PersistentStorage.roomDatabase.NoteDatabase
import note.notes.savenote.PersistentStorage.roomDatabase.NotesRepositoryImp
import note.notes.savenote.PersistentStorage.sharedPreferences.SharedPref

class SaveNoteApplication: Application() {
    lateinit var notesRepositoryImp: NotesRepositoryImp
    lateinit var sharedPref: SharedPref
    override fun onCreate() {
        super.onCreate()
        notesRepositoryImp = NotesRepositoryImp(
            NoteDao_Impl(NoteDatabase.getDatabase(this))
        )
        sharedPref = SharedPref(this)
    }
}