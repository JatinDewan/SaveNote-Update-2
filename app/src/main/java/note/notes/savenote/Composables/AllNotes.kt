package note.notes.savenote.Composables

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Composables.Components.AppBars.BottomPopUpBar
import note.notes.savenote.Composables.Components.AppBars.TopNavigationBarHome
import note.notes.savenote.Composables.Components.BackupAndRestore
import note.notes.savenote.Composables.Components.ConfirmDelete
import note.notes.savenote.Composables.Components.EntryCards
import note.notes.savenote.Composables.Components.OptionMenus.MoreOptionsMain
import note.notes.savenote.Composables.Components.OptionMenus.NewEntryButton
import note.notes.savenote.Database.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.DateUtils
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryUiState
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import java.util.UUID

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllNotesView(
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    allEntries: List<Note>,
    favoriteEntries: List<Note>,
    date: DateUtils,
    checklistViewModel: ChecklistViewModel,
    keyboardController: SoftwareKeyboardController,
    navigateNewNote:() -> Unit,
    navigateNewChecklist:() -> Unit,
    focusRequester: FocusRequester,
    context: Context,
    focusManager: FocusManager,
    offset: IntOffset,
    gridState: LazyStaggeredGridState,
    nestedScrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
    gridStateObserver: Boolean
) {

    val primaryUiState by primaryViewModel.uiState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val gridSize by remember { derivedStateOf { if (primaryUiState.currentPage) 2 else 1 } }
    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        uri -> primaryViewModel.backUpNotes(uri,context)
    }
    val restoreBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> primaryViewModel.restoreNotes(uri,context)
    }

//    Would be nice to add a scroll bar to show how many notes till bottom
//    could probs also use tap to top? maybe would be nice to be dynamic, maybe after list is larger
//    than X and only show after a certain point?? maybe animated up arrow ?

    BackHandler(
        onBack = {
            primaryViewModel.endSearch(focusManager)
            keyboardController.hide()
            primaryViewModel.dropDown(false)
            primaryViewModel.newEntryButton()
            primaryViewModel.showBackup(false)
        }
    )

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        bottomBar = {

            BottomPopUpBar(primaryViewModel = primaryViewModel)

            MoreOptionsMain(
                dismiss = primaryUiState.dropDown,
                backUp = {
                    primaryViewModel.dropDown(false)
                    primaryViewModel.showSortBy(false)
                    primaryViewModel.showBackup(true)
                },
                rateApp = { println(primaryUiState.favoriteEntries.size) },
                expandedIsFalse = { primaryViewModel.dropDown(false) },
                help = { primaryViewModel.help(context) },
                primaryViewModel = primaryViewModel,
                sortBy = { primaryViewModel.updateCurrentPageView(primaryUiState.sortByView) }
            )

            TopNavigationBarHome(
                startedScrolling = gridStateObserver,
                primaryViewModel = primaryViewModel,
                focusRequester = focusRequester,
                startSearch = { primaryViewModel.startSearch(focusRequester) },
                endSearch = { primaryViewModel.endSearch(focusManager) },
                moreOptions = { primaryViewModel.dropDown(!primaryUiState.dropDown) },
                changeView = { primaryViewModel.updateCurrentPageView(!primaryUiState.currentPage) },
                offset = offset,
                animateBarOffset = primaryUiState.animateCloseBar
            )

            NewEntryButton(
                dismiss = primaryUiState.newEntryButton,
                expand = { primaryViewModel.newEntryButton(true) },
                collapse = { primaryViewModel.newEntryButton() },
                hideButton = !primaryUiState.showSearchBar && !primaryUiState.dropDown,
                navigateNewChecklist = navigateNewChecklist::invoke,
                navigateNewNote = navigateNewNote::invoke
            )

            ConfirmDelete(
                popUp = primaryUiState.confirmDelete,
                cancel = { primaryViewModel.confirmDelete(false) },
                confirmDelete = { primaryViewModel.deleteSelected() },
                confirmMessage = stringResource(id = R.string.ConfirmDelete,"${primaryViewModel.temporaryEntryHold.size}")
            )

            BackupAndRestore(
                isVisible = primaryUiState.showBackup,
                backUp = { createBackup.launch("SaveNote_DB") },
                restore = { restoreBackup.launch(arrayOf("text/plain")) },
                dismiss = { primaryViewModel.showBackup(false) }
            )

        }
    ){ _ ->
        Crossfade(targetState = primaryUiState.showSearchBar , label = "") { currentView ->
            when(currentView) {
                true -> {
                    SearchView(
                        verticalGridState = gridState,
                        primaryViewModel = primaryViewModel,
                        checklistViewModel = checklistViewModel,
                        primaryUiState = primaryUiState,
                        notesViewModel = notesViewModel,
                        date = date,
                        gridSize = gridSize
                    )
                }
                false -> {
                    AllEntriesView(
                        allEntries = allEntries,
                        favoriteEntries = favoriteEntries,
                        verticalGridState = gridState,
                        primaryViewModel = primaryViewModel,
                        checklistViewModel = checklistViewModel,
                        primaryUiState = primaryUiState,
                        notesViewModel = notesViewModel,
                        date = date,
                        nestedScrollConnection = nestedScrollConnection,
                        gridSize = gridSize
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllEntriesView(
    allEntries: List<Note>,
    favoriteEntries: List<Note>,
    verticalGridState: LazyStaggeredGridState,
    primaryViewModel: PrimaryViewModel,
    checklistViewModel: ChecklistViewModel,
    primaryUiState: PrimaryUiState,
    notesViewModel: NotesViewModel,
    date: DateUtils,
    nestedScrollConnection: NestedScrollConnection,
    gridSize: Int,
){
    LazyVerticalStaggeredGrid(
        state = verticalGridState,
        modifier = Modifier
            .animateContentSize()
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .nestedScroll(nestedScrollConnection),
        columns = StaggeredGridCells.Fixed(gridSize),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
        contentPadding = PaddingValues(
            top = 55.dp,
            bottom = 15.dp,
            start = 10.dp,
            end = 10.dp
        )
    ) {

        item(key = UUID.randomUUID()) { SpacerAnimation(primaryUiState = primaryUiState) }

        item(key = UUID.randomUUID()) { SpacerAnimation(primaryUiState = primaryUiState) }

        items(
            items = favoriteEntries,
            key = { note -> note.uid!! }
        ) { favoritesEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                notesViewModel = notesViewModel,
                checklistViewModel = checklistViewModel,
                noteEntry = favoritesEntries,
                date = date
            )
        }

        items(
            items = allEntries,
            key = { note -> note.uid!! }
        ) { allEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                notesViewModel = notesViewModel,
                checklistViewModel = checklistViewModel,
                noteEntry = allEntries,
                date = date
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    verticalGridState: LazyStaggeredGridState,
    primaryViewModel: PrimaryViewModel,
    checklistViewModel: ChecklistViewModel,
    primaryUiState: PrimaryUiState,
    notesViewModel: NotesViewModel,
    date: DateUtils,
    gridSize: Int,
){
    Column{
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .background(MaterialTheme.colors.background),
            state = verticalGridState,
            columns = StaggeredGridCells.Fixed(gridSize),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            contentPadding = PaddingValues(
                top = 45.dp,
                bottom = 55.dp,
                start = 10.dp,
                end = 10.dp
            )
        ) {
            item(key = UUID.randomUUID()) { SpacerAnimation(primaryUiState = primaryUiState) }

            item(key = UUID.randomUUID()) { SpacerAnimation(primaryUiState = primaryUiState) }

            items(
                items = primaryUiState.searchEntries,
                key = { note -> note.uid!! }
            ) { searchResults ->
                EntryTemplate(
                    modifier = Modifier.animateItemPlacement(),
                    primaryViewModel = primaryViewModel,
                    notesViewModel = notesViewModel,
                    checklistViewModel = checklistViewModel,
                    noteEntry = searchResults,
                    date = date,
                    isSearchQuery = true,
                )
            }
        }
        NoResultsFound(primaryUiState = primaryUiState)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryTemplate(
    modifier: Modifier = Modifier,
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    checklistViewModel: ChecklistViewModel,
    noteEntry: Note,
    date:DateUtils,
    isSearchQuery: Boolean = false

){
    val haptic = LocalHapticFeedback.current
    Column(modifier){
        AnimatedContent(
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            targetState = noteEntry,
            label = ""
        ){ _ ->
            EntryCards(
                modifier = Modifier.animateContentSize(tween(300)),
                primaryViewModel = primaryViewModel,
                note = noteEntry,
                date = date,
                isSearchQuery = isSearchQuery,
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    primaryViewModel.deleteTally(noteEntry)
                },
                onEditClick = {
                    when (noteEntry.checkList.isNullOrEmpty()) {
                        true -> {
                            primaryViewModel.cardFunctionSelection(
                                returnOperationOne = {
                                    notesViewModel.navigateToNote(
                                        noteEntry,
                                        true
                                    )
                                },
                                returnOperationTwo = { primaryViewModel.deleteTally(noteEntry) }
                            )
                        }

                        else -> {
                            primaryViewModel.cardFunctionSelection(
                                returnOperationOne = {
                                    checklistViewModel.navigateToChecklist(
                                        noteEntry,
                                        true
                                    )
                                },
                                returnOperationTwo = { primaryViewModel.deleteTally(noteEntry) }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NoResultsFound(
    primaryUiState: PrimaryUiState
) {
    AnimatedVisibility(
        visible = primaryUiState.showSearchBar &&
                  primaryUiState.searchEntries.isEmpty()&&
                  primaryUiState.searchQuery.isNotEmpty(),
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(0))
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 10.dp,
                    vertical = 150.dp
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                tint = MaterialTheme.colors.onSecondary,
                painter = painterResource(id = R.drawable.search_lg),
                contentDescription = stringResource(R.string.Check),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(40.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = stringResource(id = R.string.NoMatches),
                fontSize = 20.sp,
                fontFamily = UniversalFamily,
                color = MaterialTheme.colors.onSecondary,
            )
        }
    }
}

@Composable
fun SpacerAnimation(primaryUiState: PrimaryUiState) {
    val spacerHeight by animateDpAsState(
        targetValue = derivedStateOf { if (primaryUiState.currentPage) 10.dp else 0.dp }.value,
        label = ""
    )

    Spacer(modifier = Modifier.height(spacerHeight))
}