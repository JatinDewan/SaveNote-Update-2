package note.notes.savenote.Utils

import android.os.Build
import android.os.Build.VERSION_CODES
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

interface OldDateConverter {
    fun convertOldDate(date: String): String
}

interface DateUtilities {
    fun getCurrentDateAndTime(): String

    fun  formatDateAndTime(dateAndTime: String, replaceNotesDate:(String) -> Unit): String
}

class DateUtils : DateUtilities, OldDateConverter {

    val testInstantDate = "2024-01-17T17:13:27.721281Z"
    val testSimpleDate = "2024-01-20, 15:37:37.000699"
    val testOldDate = "20 January 24, Saturday, 16:20"

    /**
     * Allows to convert V1.0 date save format in to newer format. Also upgrades strings from
     * Android Versions Older than Android 8 if Android version is upgraded
     * */
    override fun convertOldDate(date: String): String {
        val oldFormat = SimpleDateFormat("dd MMMM yy, EEEE, HH:mm", Locale.getDefault())
        val newFormat = SimpleDateFormat("yyyy-MM-dd, HH:mm:ss.ssssss", Locale.getDefault())
        val parsedDate = try {
            oldFormat.parse(date)
        } catch (e: Exception) {
            newFormat.parse(date) ?: ""
        }
        return newFormat.format(parsedDate).replace(", ", "T") + "Z"
    }

    /**
     * Saves date as a format depending on Android version
     * */
    override fun getCurrentDateAndTime(): String {
        val versionCheck = Build.VERSION.SDK_INT >= VERSION_CODES.O
        val simpleDate = SimpleDateFormat("yyyy-MM-dd, HH:mm:ss.ssssss", Locale.getDefault())

        return if (versionCheck) Instant.now().toString() else simpleDate.format(Date())
    }


    /**
     * Takes in a date [dateAndTime] and returns a string that displays date depending on
     * when the note was last edited or saved. If this is a a first time load from V1.0 OR is
     * and upgrade from Android versions older than Android 8.0, the dates will be converted to a
     * format that is compatible and replaced in the the database before being displayed on the cards
     * */

    override fun formatDateAndTime(dateAndTime: String, replaceNotesDate:(String) -> Unit): String {
        val versionCheck = Build.VERSION.SDK_INT >= VERSION_CODES.O

        return if (versionCheck) {
            var savedDateAndTime: ZonedDateTime
            try {
                savedDateAndTime = Instant.parse(dateAndTime).atZone(ZoneId.systemDefault())
            } catch (_: Exception) {
                val convertedDate = convertOldDate(dateAndTime)
                savedDateAndTime = Instant.parse(convertedDate).atZone(ZoneId.systemDefault())
                replaceNotesDate(convertedDate)
            }

            val current = Instant.parse(getCurrentDateAndTime()).atZone(ZoneId.systemDefault())
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM uu, EEEE, HH:mm").withZone(ZoneId.systemDefault())
            val parseTime = formatter.format(savedDateAndTime)
            val timeFormatted = parseTime.toString().split(", ")

            when (savedDateAndTime.toLocalDateTime().until(current, ChronoUnit.DAYS).toInt()) {
                0 -> "Today ${timeFormatted.last()}"
                1 -> "Yesterday ${timeFormatted.last()}"
                in 2..6 -> "${timeFormatted[1]} ${timeFormatted.last()}"
                else -> timeFormatted.first()
            }

        } else {
            val simpleDate = SimpleDateFormat("yyyy-MM-dd, HH:mm:ss.ssssss", Locale.getDefault())
            val displayDateFormat = SimpleDateFormat("dd MMMM yy", Locale.getDefault())
            val formatInput = simpleDate.parse(dateAndTime) as Date
            return displayDateFormat.format(formatInput)
        }
    }

}