package note.notes.savenote.Utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


interface DateUtilities {
    fun formatDateAndTime(date: String): String

    fun getCurrentDateAndTime(): String
}

interface FormatType {
    fun dateFormat(): SimpleDateFormat
}

class DateFormatter: FormatType {
    override fun dateFormat(): SimpleDateFormat {
        return SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}

class DateUtils(private val formatter: FormatType): DateUtilities {

    override fun getCurrentDateAndTime(): String = formatter.dateFormat().format(Date())
    override fun formatDateAndTime(date: String): String {
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        formatter.dateFormat().timeZone = utcTimeZone

        val currentDate = formatter.dateFormat().parse(getCurrentDateAndTime())
        val inputDate = formatter.dateFormat().parse(date)

        val calendarCurrent = Calendar.getInstance(utcTimeZone).apply { time = currentDate!! }
        val calendarInput = Calendar.getInstance(utcTimeZone).apply { time = inputDate!! }

        val daysDifference = (calendarCurrent.timeInMillis - calendarInput.timeInMillis) / (24 * 60 * 60 * 1000)
        val timeParts = date.split(", ")

        return when (daysDifference.toInt()) {
            0 -> "Today ${timeParts.last()}"
            1 -> "Yesterday ${timeParts.last()}"
            in 2..6 -> "${timeParts[1]} ${timeParts.last()}"
            else -> timeParts.first()
        }
    }

}