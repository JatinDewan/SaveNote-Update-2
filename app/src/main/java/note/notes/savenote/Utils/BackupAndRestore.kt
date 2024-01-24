package note.notes.savenote.Utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import note.notes.savenote.PersistentStorage.roomDatabase.NewArrayConverter
import note.notes.savenote.PersistentStorage.roomDatabase.Note


interface BackupAndRestore {

    fun backUp(uri: Uri, context: Context, noteEntries: List<Note>)

    fun restore(uri: Uri, context: Context, noteEntries: List<Note>): List<Note>

}

class BackupAndRestoreNote(val viewModelScope: CoroutineScope) : BackupAndRestore {
    private val noteTypeConverter = NewArrayConverter()
    private var saveAllNotes = "7800728b-54ca-496e-93c1-7bb4caf97081"

    private fun toastMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun backUp(uri: Uri, context: Context, noteEntries: List<Note>) {
        val writeTo = uri.let {
            uriStream -> context.contentResolver.openOutputStream(uriStream,"rw")
        }

        noteEntries.forEach { note ->
            saveAllNotes = "${saveAllNotes}\n${noteTypeConverter.fromString(note)}"
        }

        try{
            writeTo?.bufferedWriter()?.use { write -> write.write(saveAllNotes) }
            toastMessage(context = context, message = "Backup Successful")
        } catch (e: Exception){
            toastMessage(context = context, message = "Invalid Location")
        } finally {
            writeTo?.close()
        }
    }

    override fun restore(uri: Uri, context: Context, noteEntries: List<Note>): List<Note> {
        val createStream = uri.let { context.contentResolver.openInputStream(it) }
        val openReader = createStream?.bufferedReader().use { read -> read?.readText() }
        val textToList = openReader?.split("\n")
        var restoredNotes: List<Note> = emptyList()

        try{
            if(textToList?.first() == saveAllNotes) {
                restoredNotes = textToList.drop(1).map { notes ->
                    val note = NewArrayConverter().toString(notes)
                    Note(
                        uid = null,
                        header = note?.header,
                        note = note?.note,
                        date = note?.date,
                        checkList = note?.checkList,
                        category = note?.category
                    )
                }
                toastMessage(context = context, message = "Transfer Successful")
            } else {
                toastMessage(context = context, message = "Incompatible File Type")
            }

        } catch(e: Exception){
            toastMessage(context = context, message = "Next One")
        } finally {
            createStream?.close()
        }
        return restoredNotes
    }

}