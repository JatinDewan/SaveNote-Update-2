package note.notes.savenote.Composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.SaveNoteTheme
import org.burnoutcrew.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalFoundationApi::class)
suspend fun pagerAnimation(
    pagerState:
    PagerState, page: Int,
)  {
    val animationSpec = tween<Float>(
        durationMillis = 500,
        easing = EaseInOut
    )
    pagerState.animateScrollToPage(
        page = page,
        animationSpec = animationSpec
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainComposable(
    primaryViewModel: PrimaryViewModel = viewModel(factory = PrimaryViewModel.Factory),
    checklistViewModel: ChecklistViewModel = viewModel(initializer = { ChecklistViewModel(primaryViewModel) }),
    notesViewModel: NotesViewModel = viewModel(initializer = { NotesViewModel(primaryViewModel) }),
    loaded:() -> Unit
) {
    val primaryUiState by primaryViewModel.stateGetter.collectAsState()
    val notesUiState by notesViewModel.stateGetter.collectAsState()
    val checklistUiState by checklistViewModel.stateGetter.collectAsState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboard = keyboardAsState()
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 3 }
    )
    val state = rememberReorderableLazyListState(
        canDragOver = { from, _ -> checklistViewModel.dragRestriction(from.index)},
        onMove = { from, to ->  checklistViewModel.onMoveIndexer(from.key, to.key) }
    )

    LaunchedEffect(primaryViewModel.isReady) { loaded() }

    SaveNoteTheme(legacyColour = primaryUiState.setTheme){
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(color = colors.background)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        beyondBoundsPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> {
                                AllNotesView(
                                    primaryViewModel = primaryViewModel,
                                    notesViewModel = notesViewModel,
                                    allEntries = primaryUiState.allEntries,
                                    favoriteEntries = primaryUiState.favoriteEntries,
                                    checklistViewModel = checklistViewModel,
                                    focusRequester = focusRequester,
                                    context = context,
                                    focusManager = focusManager,
                                    navigateChecklistView = {
                                        scope.launch {
                                            checklistViewModel.navigateNewChecklist().join()
                                            pagerAnimation(pagerState = pagerState, page = 1)
                                        }
                                    },
                                    navigateNoteView = {
                                        scope.launch {
                                            notesViewModel.navigateNewNote().join()
                                            pagerAnimation(pagerState = pagerState, page = 1)
                                        }
                                    },
                                    onEditClick = { noteEntry ->
                                        if (primaryViewModel.temporaryEntryHold.isEmpty()) {
                                            if (noteEntry.checkList.isNullOrEmpty()) {
                                                scope.launch {
                                                    primaryViewModel.cardFunctionSelection(
                                                        navigateEntry = { notesViewModel.editNote(noteEntry) },
                                                        note = noteEntry
                                                    ).join()
                                                    pagerAnimation(pagerState = pagerState, page = 1)
                                                }
                                            } else {
                                                scope.launch {
                                                    primaryViewModel.cardFunctionSelection(
                                                        navigateEntry = { checklistViewModel.editChecklist(noteEntry) },
                                                        note = noteEntry
                                                    ).join()
                                                    pagerAnimation(pagerState = pagerState, page = 1)
                                                }
                                            }
                                        } else {
                                            primaryViewModel.deleteTally(noteEntry)
                                        }
                                    }
                                )
                            }

                            1 -> {
                                if (checklistUiState.navigateNewChecklist){
                                    ChecklistComposer(
                                        focusManager = focusManager,
                                        checklistViewModel = checklistViewModel,
                                        focusRequester = focusRequester,
                                        keyboard = keyboard,
                                        context = context,
                                        state = state,
                                        saveAndExit = {
                                            checklistViewModel.exitAndSave(focusManager)
                                            scope.launch {
                                                pagerAnimation(pagerState = pagerState, page = 0)
                                                state.listState.scrollToItem(0)
                                                checklistViewModel.resetValues()
                                            }
                                        }
                                    )
                                }

                                if (notesUiState.navigateNewNote) {
                                    NoteComposer(
                                        notesViewModel = notesViewModel,
                                        focusRequester = focusRequester,
                                        focusManager = focusManager,
                                        context = context,
                                        keyboard = keyboard,
                                        pagerState = {
                                            notesViewModel.exitAndSave(focusManager)
                                            scope.launch {
                                                pagerAnimation(pagerState = pagerState, page = 0)
                                                notesViewModel.resetValues()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (pagerState.isScrollInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = false) { },
                    content = { Text("") }
                )
            }
        }
    }
}

