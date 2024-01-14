package note.notes.savenote

import note.notes.savenote.Utils.DateFormatter
import note.notes.savenote.Utils.DateUtils
import org.junit.jupiter.api.Assertions

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val sample = DateUtils(DateFormatter())
    @org.junit.jupiter.api.Test
    fun addition_isCorrect() {
        Assertions.assertEquals(4, 2 + 2)
    }

    @org.junit.jupiter.api.Test
    fun formatDateAndTime() {
        Assertions.assertEquals(
            "Yesterday 22:40",
            sample.formatDateAndTime("11 January 24, Thursday, 22:40")
        )
    }

}