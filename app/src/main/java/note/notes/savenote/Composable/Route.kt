package note.notes.savenote.Composable

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
import note.notes.savenote.ViewModel.ChecklistViewModel
import note.notes.savenote.ViewModel.NotesViewModel
import note.notes.savenote.ViewModel.PrimaryViewModel
import note.notes.savenote.ViewModel.model.PrimaryUiState
import note.notes.savenote.ui.theme.SaveNoteTheme
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import java.util.UUID

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

    val reorderableState = reorderableListState(checklistViewModel = checklistViewModel)
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 2 }
    )

    LaunchedEffect(primaryViewModel.isReady) { loaded() }

    HomeScreenLayout(
        allowPageInteraction = pagerState.isScrollInProgress,
        applicationTheme = primaryUiState.setTheme
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            beyondBoundsPageCount = 1,
            pageSpacing = 100.dp,
            key = { UUID.randomUUID() }
        ) { page ->
            when (page) {
                0 -> {
                    val resetValues = {
                        notesViewModel.resetValues()
                        checklistViewModel.resetValues()
                    }
                    AllNotesView(
                        primaryViewModel = primaryViewModel,
                        notesViewModel = notesViewModel,
                        allEntries = primaryUiState.allEntries,
                        favoriteEntries = primaryUiState.favoriteEntries,
                        checklistViewModel = checklistViewModel,
                        focusManager = focusManager,
                        primaryUiState = primaryUiState,
                        navigateChecklistView = {
                            resetValues.invoke()
                            checklistViewModel.navigateNewChecklist()
                            scope.launch(Dispatchers.Main) {
                                pagerAnimation(pagerState = pagerState, page = 1)
                            }
                        },
                        navigateNoteView = {
                            resetValues.invoke()
                            notesViewModel.navigateNewNote()
                            scope.launch(Dispatchers.Main) {
                                pagerAnimation(pagerState = pagerState, page = 1)
                            }
                        },
                        onEditClick = { noteEntry ->
                            editOnClick(
                                primaryViewModel = primaryViewModel,
                                notesViewModel = notesViewModel,
                                checklistViewModel = checklistViewModel,
                                pagerState = pagerState,
                                noteEntry = noteEntry,
                                coroutineScope = scope,
                                primaryUiState = primaryUiState
                            )
                        }
                    )
                }

                1 -> {
                    when {
                        checklistUiState.navigateNewChecklist -> {
                            ChecklistComposer(
                                checklistViewModel = checklistViewModel,
                                reorderLazyListState = reorderableState,
                                focusManager = focusManager,
                                checklistUiState = checklistUiState,
                                saveAndExit = {
                                    checklistViewModel.exitAndSave(focusManager)
                                    scope.launch(Dispatchers.Main) {
                                        pagerAnimation(pagerState = pagerState, page = 0)
                                        reorderableState.listState.scrollToItem(0)
                                    }.invokeOnCompletion {
                                        checklistViewModel.resetValues()
                                    }
                                }
                            )
                        }

                        notesUiState.navigateNewNote -> {
                            NoteComposer(
                                notesViewModel = notesViewModel,
                                focusManager = focusManager,
                                notesUiState = notesUiState,
                                pagerState = {
                                    notesViewModel.exitAndSave(focusManager)
                                    scope.launch(Dispatchers.Main) {
                                        pagerAnimation(pagerState = pagerState, page = 0)
                                    }.invokeOnCompletion {
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
}

@OptIn(ExperimentalFoundationApi::class)
fun editOnClick(
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    checklistViewModel: ChecklistViewModel,
    primaryUiState: PrimaryUiState,
    pagerState: PagerState,
    noteEntry: Note,
    coroutineScope: CoroutineScope
) {
    if (primaryUiState.temporaryEntryHold.isEmpty()) {
        if (noteEntry.checkList.isNullOrEmpty()) {
            primaryViewModel.cardFunctionSelection(
                navigateEntry = { notesViewModel.editNote(noteEntry) },
                note = noteEntry
            )
            coroutineScope.launch(Dispatchers.Main) {
                pagerAnimation(pagerState = pagerState, page = 1)
            }
        } else {
            primaryViewModel.cardFunctionSelection(
                navigateEntry = { checklistViewModel.editChecklist(noteEntry) },
                note = noteEntry
            )
            coroutineScope.launch(Dispatchers.Main) {
                pagerAnimation(pagerState = pagerState, page = 1)
            }
        }
    } else {
        primaryViewModel.noteSelector(noteEntry)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenLayout(
    applicationTheme: Int,
    allowPageInteraction: Boolean,
    mainContent: @Composable () -> Unit
) {
    SaveNoteTheme(legacyColour = applicationTheme){
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(color = colors.background)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)) { mainContent() }
            }

            if (allowPageInteraction) {
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

@Composable
fun reorderableListState(
    checklistViewModel: ChecklistViewModel
): ReorderableLazyListState {
    val checklistUiState by checklistViewModel.stateGetter.collectAsState()
    return rememberReorderableLazyListState(
        canDragOver = { from, _ ->
            from.index <= checklistViewModel.checklistUncheckedUpdater().lastIndex + 6 && from.index >= 6
        },
        onMove = { from, to ->
            val fromIndex = checklistUiState.temporaryChecklist.indexOfFirst { it.key == from.key }
            val toIndex = checklistUiState.temporaryChecklist.indexOfFirst { it.key == to.key }
            if (fromIndex != -1 && toIndex != -1) {
                checklistUiState.temporaryChecklist.apply { add(toIndex, removeAt(fromIndex)) }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
suspend fun pagerAnimation(
    pagerState:
    PagerState, page: Int,
) {
    val animationSpec = tween<Float>(durationMillis = 600, easing = EaseInOut)

    pagerState.animateScrollToPage(page = page, animationSpec = animationSpec)
}
