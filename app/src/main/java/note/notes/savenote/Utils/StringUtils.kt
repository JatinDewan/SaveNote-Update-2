package note.notes.savenote.Utils

class CheckStringUtil {
    fun checkString(string: String): String? = string.ifEmpty { null }
    fun replaceNull(string: String?): String = string ?: ""
}