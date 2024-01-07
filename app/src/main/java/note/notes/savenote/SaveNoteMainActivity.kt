package note.notes.savenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import note.notes.savenote.Composables.MainComposable
import note.notes.savenote.ui.theme.SaveNoteTheme

class SaveNote : ComponentActivity() {
    private var loaded = true
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition{ loaded }
        super.onCreate(savedInstanceState)
        setContent {
            SaveNoteTheme {
                MainComposable(
                    loaded = { loaded = false }
                )
            }
        }
    }
}
