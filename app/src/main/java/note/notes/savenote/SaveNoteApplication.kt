package note.notes.savenote

import android.app.Application
import note.notes.savenote.Database.NoteDao_Impl
import note.notes.savenote.Database.NoteDatabase
import note.notes.savenote.Database.NotesRepositoryImp
import note.notes.savenote.Database.SharedPref

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