package note.notes.savenote.Utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

interface Help {
    fun getHelp(context: Context)
}

class GetHelp : Help {

    override fun getHelp(context: Context) {
        val brand = Build.BRAND
        val device = Build.DEVICE
        val model = Build.MODEL
        try{
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.type = "vnd.android.cursor.item/email"
            sendEmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("savenoteapp@gmail.com"))
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, "SaveNote - Help / Feedback - ($brand $device $model)")
            ContextCompat.startActivity(context, sendEmail, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(context, "Please use a valid Email Client", Toast.LENGTH_SHORT).show()
        }
    }

}