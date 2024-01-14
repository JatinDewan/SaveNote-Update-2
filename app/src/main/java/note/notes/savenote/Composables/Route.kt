package note.notes.savenote.Composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInExpo
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import note.notes.savenote.Utils.DateUtils
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.Utils.rememberForeverLazyListState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainComposable(
    primaryViewModel: PrimaryViewModel = viewModel(factory = PrimaryViewModel.Factory),
    checklistViewModel: ChecklistViewModel = viewModel(initializer = { ChecklistViewModel(primaryViewModel) }),
    notesViewModel: NotesViewModel = viewModel(initializer = { NotesViewModel(primaryViewModel) }),
    loaded:() -> Unit
) {
    val primaryUiState by primaryViewModel.uiState.collectAsState()
    val notesUiState by notesViewModel.uiState.collectAsState()
    val checklistUiState by checklistViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = FocusRequester()
    val keyboard = keyboardAsState()
    val context = LocalContext.current
    val collapsableNavigationBar by remember {
        derivedStateOf { IntOffset(x = 0, y = -primaryUiState.barOffsetY.roundToInt()) }
    }
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
            animationSpec = tween(durationMillis = 200, easing = EaseInExpo),
            targetOffsetY = { targetOffset -> +targetOffset }
        )

        AllNotesView(
            primaryViewModel = primaryViewModel,
            notesViewModel = notesViewModel,
            allEntries = listView,
            favoriteEntries = primaryUiState.favoriteEntries,
            checklistViewModel = checklistViewModel,
            focusRequester = focusRequester,
            context = context,
            focusManager = focusManager,
            offset = collapsableNavigationBar
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
//                    closeScreen = { checklistViewModel.openNewChecklist(false) }
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