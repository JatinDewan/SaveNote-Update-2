package note.notes.savenote.Database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CheckList(
    val note: String,
    var strike: Int,
    val key: Any?
)

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class ArrayListConverter {
    @TypeConverter
    fun fromStringArrayList(value: ArrayList<CheckList>?): String {

        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringArrayList(value: String): ArrayList<CheckList>? {
        return try {
            Gson().fromJson<ArrayList<CheckList>>(value) //using extension function
        } catch (e: Exception ) {
            arrayListOf()
        }
    }
}

class NewArrayConverter{
    @TypeConverter
    fun fromString(value: Note):String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toString(value:String): Note {
        return try {
            Gson().fromJson<Note>(value) //using extension function
        } catch (_: Exception) {
            Note(
                uid = null,
                header = null,
                note = null,
                date = null,
                checkList = null,
                category = null
            )
        }
    }
}