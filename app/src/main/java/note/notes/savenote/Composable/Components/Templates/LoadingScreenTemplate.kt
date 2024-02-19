package note.notes.savenote.Composable.Components.Templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen(
    isLoading: Boolean,
    message: Int
) {
    if (isLoading){
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.background.copy(alpha = 0.9f))
                .fillMaxSize()
                .pointerInput(Unit) { /*Needed to not click anything*/ },
            contentAlignment = Alignment.Center

        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                CircularProgressIndicator()
                Text(
                    text = stringResource(id = message),
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}