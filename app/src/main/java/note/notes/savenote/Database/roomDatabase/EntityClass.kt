package note.notes.savenote.Database.roomDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "header") val header: String? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "date") val date: String? = null,
    @ColumnInfo(name = "checkList") val checkList: List<CheckList>? = null,
    @ColumnInfo(name = "category") val category: String? = null,
)