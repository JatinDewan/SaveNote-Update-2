package note.notes.savenote.Composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainComposable(
    primaryViewModel: PrimaryViewModel = viewModel(factory = PrimaryViewModel.Factory),
    checklistViewModel: ChecklistViewModel = viewModel(initializer = { ChecklistViewModel(primaryViewModel) }),
    notesViewModel: NotesViewModel = viewModel(initializer = { NotesViewModel(primaryViewModel) }),
    loaded:() -> Unit
) {
    val primaryUiState by primaryViewModel.statGetter.collectAsState()
    val notesUiState by notesViewModel.uiState.collectAsState()
    val checklistUiState by checklistViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = FocusRequester()
    val keyboard = keyboardAsState()
    val context = LocalContext.current

    val listView by remember {
        derivedStateOf { primaryUiState.allEntries.sortedWith(primaryViewModel.compareLastEdit()) }
    }

    LaunchedEffect(primaryViewModel.isReady) { loaded() }

    BoxWithConstraints {
        val pageAnimationIn = slideInVertically(
            animationSpec =  tween(durationMillis = 300, easing = EaseIn),
            initialOffsetY = { initialOffset -> initialOffset }
        )
        val pageAnimationOut = slideOutVertically(
            animationSpec = tween(durationMillis = 300, easing = EaseIn),
            targetOffsetY = { targetOffset -> +targetOffset }
        )

        AnimatedVisibility(
            visible = !checklistUiState.navigateNewChecklist && !notesUiState.navigateNewNote,
            enter = pageAnimationIn,
            exit = pageAnimationOut,
            content = {
                AllNotesView(
                    primaryViewModel = primaryViewModel,
                    notesViewModel = notesViewModel,
                    allEntries = listView,
                    favoriteEntries = primaryUiState.favoriteEntries,
                    checklistViewModel = checklistViewModel,
                    focusRequester = focusRequester,
                    context = context,
                    focusManager = focusManager,
                )
            }
        )

        AnimatedVisibility(
            visible = checklistUiState.navigateNewChecklist,
            enter = pageAnimationIn,
            exit = pageAnimationOut,
            content = {
                ChecklistComposer(
                    coroutineScope = scope,
                    focusManager = focusManager,
                    checklistViewModel = checklistViewModel,
                    focusRequester = focusRequester,
                    keyboard = keyboard,
                    context = context,
                )
            }
        )

        AnimatedVisibility(
            visible = notesUiState.navigateNewNote,
            enter = pageAnimationIn,
            exit = pageAnimationOut,
            content = {
                NoteComposer(
                    notesViewModel = notesViewModel,
                    focusRequester = focusRequester,
                    coroutineScope = scope,
                    focusManager = focusManager,
                    context = context,
                    keyboard = keyboard,
                    closeScreen = { notesViewModel.openNewNote(false) }
                )
            }
        )
    }
}