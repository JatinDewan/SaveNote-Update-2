package note.notes.savenote.Utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class DateUtils {
    private val formatter = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())

    val current: String = formatter.format(Date())

    fun dateTimeDisplay(date: String): String {
        val currentDate = formatter.parse(current)
        val parsedDate = formatter.parse(date)

        val days = abs((currentDate!!.time - parsedDate!!.time) / (24 * 60 * 60 * 1000))

        val time = date.split(", ")

        return when (days) {
            0L -> "Today ${time.last()}"
            1L -> "Yesterday ${time.last()}"
            in 2..6 -> "${time[1]} ${time.last()}"
            else -> time.first()
        }
    }
}