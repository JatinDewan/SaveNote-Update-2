package note.notes.savenote.Composables

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import note.notes.savenote.Composables.Components.AppBars.TopNavigationNote
import note.notes.savenote.Composables.Components.CustomTextField
import note.notes.savenote.Composables.Components.TextFieldPlaceHolder
import note.notes.savenote.R
import note.notes.savenote.Utils.Keyboard
import note.notes.savenote.Utils.observeAsState
import note.notes.savenote.ViewModelClasses.NotesViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteComposer(
    notesViewModel: NotesViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    coroutineScope: CoroutineScope,
    context : Context,
    keyboard: State<Keyboard>,
    closeScreen:() -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val notesUiState by notesViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()
    var keyboardRefocusState:Keyboard? by rememberSaveable { mutableStateOf(null) }

    BackHandler(onBack = { notesViewModel.returnAndSaveNote(closeScreen::invoke)})
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
            keyboardRefocusState = keyboard.value
            notesViewModel.saveNoteEdit()
        }
    }

    Scaffold(
        topBar = {
            TopNavigationNote(
                backButton = {
                    focusManager.clearFocus()
                    notesViewModel.returnAndSaveNote(closeScreen::invoke)
                },
                share = { context.startActivity(notesViewModel.shareNote()) },
                showHeader = false
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = notesUiState.header,
                onValueChange = { notesViewModel.header(it) },
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textColour = MaterialTheme.colors.onSecondary,
                onTextLayout = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                decorationBox = {
                    TextFieldPlaceHolder(
                        showPlaceHolder = notesUiState.header.isEmpty(),
                        text = R.string.Title,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            Divider(color = MaterialTheme.colors.secondary)

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