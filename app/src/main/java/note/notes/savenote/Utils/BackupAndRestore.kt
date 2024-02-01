package note.notes.savenote.Utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import note.notes.savenote.PersistentStorage.roomDatabase.NewArrayConverter
import note.notes.savenote.PersistentStorage.roomDatabase.Note


interface BackupAndRestore {
    var restoreNotes: List<Note>
    suspend fun backUp(uri: Uri, context: Context, noteEntries: List<Note>)

    suspend fun restore(uri: Uri, context: Context, noteEntries: List<Note>)

}

class BackupAndRestoreNote : BackupAndRestore {
    private val noteTypeConverter = NewArrayConverter()
    private var saveAllNotes = "7800728b-54ca-496e-93c1-7bb4caf97081"
    override var restoreNotes: List<Note> = emptyList()
    private fun toastMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override suspend fun backUp(uri: Uri, context: Context, noteEntries: List<Note>) {
        val writeTo = uri.let {
            uriStream -> context.contentResolver.openOutputStream(uriStream,"rw")
        }

        withContext(Dispatchers.Default){
            noteEntries.forEach { note ->
                saveAllNotes = "${saveAllNotes}\n${noteTypeConverter.fromString(note)}"
            }
        }

        try {
            withContext(Dispatchers.IO) {
                writeTo?.bufferedWriter()?.use { write -> write.write(saveAllNotes) }
            }
            restoreNotes = emptyList()
            saveAllNotes = "7800728b-54ca-496e-93c1-7bb4caf97081"
            toastMessage(context = context, message = "Backup Successful")
        } catch (e: Exception) {
            toastMessage(context = context, message = "Invalid Location")
        } finally {
            writeTo?.close()
        }
    }

    override suspend fun restore(uri: Uri, context: Context, noteEntries: List<Note>) {
        val createStream = uri.let { context.contentResolver.openInputStream(it) }
        val openReader = createStream?.bufferedReader().use { read -> read?.readText() }
        val textToList = openReader?.split("\n")

        try{
            if(textToList?.first() == saveAllNotes) {
                withContext(Dispatchers.IO){
                    restoreNotes = textToList.drop(1).map { notes ->
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
    }

}