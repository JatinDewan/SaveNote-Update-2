package note.notes.savenote.Utils


import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
class DateUtilsTest {

    private val sample = DateUtils(DateFormatter())


    @Test
    fun formatDateAndTime() {
        assertEquals("Yesterday 22:40", sample.formatDateAndTime("11 January 24, Thursday, 22:40"))
    }
}