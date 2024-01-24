package note.notes.savenote.PersistentStorage.roomDatabase

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

data class CheckList(
    val note: String,
    var strike: Int,
    val key: Any?
)

class ArrayListConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromStringArrayList(value: List<CheckList>?): String = gson.toJson(value)

    @TypeConverter
    fun toStringArrayList(value: String): List<CheckList>? {
        return try {
            val type = object : TypeToken<List<CheckList>>() {}.type
            gson.fromJson<List<CheckList>>(value, type)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}

class NewArrayConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: Note): String = gson.toJson(value)

    @TypeConverter
    fun toString(value: String): Note? {
        return try {
            gson.fromJson(value, Note::class.java)
        } catch (e: JsonSyntaxException) {
            // Log the exception or handle it appropriately
            null // or throw it upwards, depending on your application's needs
        }
    }
}