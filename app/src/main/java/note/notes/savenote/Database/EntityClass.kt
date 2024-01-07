package note.notes.savenote.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "header") val header: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "checkList") val checkList: ArrayList<CheckList>?,
    @ColumnInfo(name = "category") val category: String?,
)