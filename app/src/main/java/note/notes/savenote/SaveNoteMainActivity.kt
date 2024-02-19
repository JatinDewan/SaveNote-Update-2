package note.notes.savenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import note.notes.savenote.Composable.MainComposable

class SaveNote : ComponentActivity() {
    private var loaded = true
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition{ loaded }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MainComposable(
                loaded = { loaded = false }
            )
        }
    }
}
