package note.notes.savenote.Composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import note.notes.savenote.Composable.Components.AppBars.NotesAndChecklistNavigationBar
import note.notes.savenote.Composable.Components.CustomTextField.CustomTextField
import note.notes.savenote.Composable.Components.CustomTextField.TextFieldPlaceHolder
import note.notes.savenote.R
import note.notes.savenote.Utils.Keyboard
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.Utils.observeAsState
import note.notes.savenote.ViewModel.model.NotesUiState
import note.notes.savenote.ViewModel.NotesViewModel
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun NoteComposer(
    notesViewModel: NotesViewModel,
    focusManager: FocusManager,
    pagerState:() -> Unit,
    notesUiState: NotesUiState
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = keyboardAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()
    var keyboardRefocusState:Keyboard? by rememberSaveable { mutableStateOf(null) }

    BackHandler(
        onBack = pagerState::invoke
    )
    if(keyboard.value == Keyboard.Closed) {
        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(stateLifecycle) {
        if(notesViewModel.noteEntry.text.isNotEmpty()) {
            if(stateLifecycle == Lifecycle.Event.ON_RESUME) {
                if(keyboardRefocusState == Keyboard.Opened){
                    focusRequester.requestFocus()
                }
            }
        }
        if(stateLifecycle == Lifecycle.Event.ON_PAUSE) {
            notesViewModel.saveNewOrEditExistingNote()
            keyboardRefocusState = keyboard.value
        }
    }

    Scaffold(
        topBar = {
            NotesAndChecklistNavigationBar(
                backButton = pagerState::invoke,
                buttonAction = { context.startActivity(notesViewModel.shareNote()) },
                isNote = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(horizontal = 15.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = notesViewModel.showDate(),
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontFamily = UniversalFamily,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(7.dp))

            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textColour = MaterialTheme.colors.onSecondary,
                value = notesUiState.header,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { notesViewModel.header(it) },
                decorationBox = {
                    TextFieldPlaceHolder(
                        showPlaceHolder = notesUiState.header.isEmpty(),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        note = notesUiState.fullNote
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(15.dp),
                content = { Text(text = "") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 10.dp),
                value = notesViewModel.noteEntry.text,
                onValueChange = { notesViewModel.updateNoteEntry(it) },
                decorationBox = {
                    TextFieldPlaceHolder(
                        showPlaceHolder = notesViewModel.noteEntry.text.isEmpty(),
                        text = R.string.Note,
                        fontSize = 15.sp
                    )
                }
            )
        }
    }
}