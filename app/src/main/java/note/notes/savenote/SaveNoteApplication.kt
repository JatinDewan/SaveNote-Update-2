package note.notes.savenote

import android.app.Application

import note.notes.savenote.Database.roomDatabase.NoteDao_Impl
import note.notes.savenote.Database.roomDatabase.NoteDatabase
import note.notes.savenote.Database.roomDatabase.NotesRepositoryImp
import note.notes.savenote.Database.sharedPreferences.SharedPref

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