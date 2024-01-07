package note.notes.savenote.Composables.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun ConfirmDelete(
    popUp: Boolean,
    cancel:() -> Unit,
    confirmDelete:() -> Unit,
    confirmMessage: String,
){
    if(popUp) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onPress = { cancel() }) }
                .fillMaxSize()
                .background(MaterialTheme.colors.background.copy(0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .height(160.dp),
                shape = RoundedCornerShape(15.dp),
                backgroundColor = MaterialTheme.colors.secondary,
                border = BorderStroke(1.dp, color = MaterialTheme.colors.onBackground)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ){
                    ConfirmDeleteHeader(confirmMessage = confirmMessage)

                    DeleteConfirmationSelection(
                        cancel = cancel::invoke,
                        confirmDelete = confirmDelete::invoke
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteHeader(
    confirmMessage: String
){
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Text(
            text = stringResource(id = R.string.DeleteSelected),
            color = MaterialTheme.colors.primaryVariant,
            fontWeight = FontWeight.SemiBold,
            fontFamily = UniversalFamily,
            fontSize = 18.sp
        )

        Text(
            text = confirmMessage,
            color = MaterialTheme.colors.onSecondary,
            fontFamily = UniversalFamily,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DeleteConfirmationSelection(
    cancel: () -> Unit,
    confirmDelete: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = cancel::invoke
        ){
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(R.string.DeleteCancel),
                color = MaterialTheme.colors.onSecondary,
                fontFamily = UniversalFamily,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Card(
            modifier = Modifier.height(30.dp),
            backgroundColor = MaterialTheme.colors.background
        ) {
            IconButton(
                modifier = Modifier.size(
                    width = 60.dp,
                    height = 30.dp
                ),
                onClick = {
                    confirmDelete()
                    cancel()
                }
            ){
                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.DeleteConfirm),
                    color = MaterialTheme.colors.onError,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = UniversalFamily,
                )
            }
        }
    }
}