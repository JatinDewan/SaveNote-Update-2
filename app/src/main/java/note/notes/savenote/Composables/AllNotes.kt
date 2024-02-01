package note.notes.savenote.Composables

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Composables.Components.AppBars.BottomPopUpBar
import note.notes.savenote.Composables.Components.AppBars.TopNavigationBarHome
import note.notes.savenote.Composables.Components.BackupAndRestore
import note.notes.savenote.Composables.Components.ConfirmDelete
import note.notes.savenote.Composables.Components.EntryCards
import note.notes.savenote.Composables.Components.LoadingScreen
import note.notes.savenote.Composables.Components.OptionMenus.MoreOptionsMain
import note.notes.savenote.Composables.Components.OptionMenus.NewEntryButton
import note.notes.savenote.PersistentStorage.roomDatabase.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.rememberForeverLazyListState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryUiState
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import java.util.UUID
import kotlin.math.roundToInt


@Composable
fun animateOnScroll(animateScroll: State<Boolean>, offset:State<Float>): IntOffset {
    val animationSpec = animateFloatAsState(targetValue = offset.value, label = "")
    val animateOrState = if(animateScroll.value) offset else animationSpec
    return IntOffset(x = 0, y = animateOrState.value.roundToInt())
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllNotesView(
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    allEntries: List<Note>,
    favoriteEntries: List<Note>,
    checklistViewModel: ChecklistViewModel,
    focusRequester: FocusRequester,
    context: Context,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {

    val primaryUiState by primaryViewModel.stateGetter.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val allNotesState = rememberForeverLazyListState(key = "grid")
    val searchNotesState = rememberForeverLazyListState(key = "grid")
    val allNotesObserver by remember { derivedStateOf { allNotesState.firstVisibleItemIndex > 0 } }
    val searchNotesObserver by remember { derivedStateOf { searchNotesState.firstVisibleItemIndex > 0 } }

    val toolbarHeightPx = with(LocalDensity.current) { 59.dp.roundToPx().toFloat() }
    val allowAnimation = remember { mutableStateOf(false) }
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val customNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.floatValue + delta
                if(allNotesObserver) toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                allowAnimation.value = true
                 return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                toolbarOffsetHeightPx.floatValue = if(toolbarOffsetHeightPx.floatValue  >= -82.5 || !allNotesObserver) 0f else -165f
                allowAnimation.value = false
                return super.onPostFling(consumed, available)
            }
        }
    }

//    Would be nice to add a scroll bar to show how many notes till bottom
//    could probs also use tap to top? maybe would be nice to be dynamic, maybe after list is larger
//    than X and only show after a certain point?? maybe animated up arrow ?

    BackHandler(
        onBack = {
            primaryViewModel.endSearch(focusManager)
            keyboardController?.hide()
            primaryViewModel.dropDown(false)
            primaryViewModel.newEntryButton()
            primaryViewModel.backup(false)
        }
    )

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        bottomBar = {

            BottomPopUpBar(primaryViewModel = primaryViewModel)

            MoreOptionsMain(
                dismiss = primaryUiState.dropDown,
                backUp = { primaryViewModel.backup(true) },
                rateApp = { primaryViewModel.rateApp(context) },
                expandedIsFalse = { primaryViewModel.dropDown(false) },
                help = { primaryViewModel.help(context) },
                primaryViewModel = primaryViewModel,
                sortBy = { primaryViewModel.updateSortByPreference(primaryUiState.sortByView) }
            )

            TopNavigationBarHome(
                startedScrolling = allNotesObserver || searchNotesObserver,
                primaryViewModel = primaryViewModel,
                focusRequester = focusRequester,
                startSearch = { primaryViewModel.startSearch(focusRequester) },
                endSearch = { primaryViewModel.endSearch(focusManager) },
                moreOptions = { primaryViewModel.dropDown(!primaryUiState.dropDown) },
                changeView = { primaryViewModel.selectLayout(primaryUiState.layoutView) },
                isMenuOpen = primaryUiState.dropDown,
                offset =  animateOnScroll(animateScroll = allowAnimation, offset = toolbarOffsetHeightPx)
            )

            NewEntryButton(
                dismiss = primaryUiState.newEntryButton,
                expand = { primaryViewModel.newEntryButton(true) },
                collapse = { primaryViewModel.newEntryButton() },
                hideButton = !primaryUiState.showSearchBar && !primaryUiState.dropDown,
                navigateNewChecklist = { checklistViewModel.newChecklist() },
                navigateNewNote = { notesViewModel.navigateNewNote() }
            )

            ConfirmDelete(
                popUp = primaryUiState.confirmDelete,
                cancel = { primaryViewModel.confirmDelete(false) },
                confirmDelete = { primaryViewModel.deleteSelected() },
                confirmMessage = stringResource(
                    id = R.string.ConfirmDelete,"${primaryViewModel.temporaryEntryHold.size}"
                )
            )

            BackupAndRestore(
                primaryViewModel = primaryViewModel,
                isVisible = primaryUiState.showBackup,
                dismiss = { primaryViewModel.backup(false) }
            )

            LoadingScreen(
                isLoading = primaryUiState.loadingScreen,
                message = R.string.LoadingScreenMessageMain
            )
        }
    ){ _ ->
        Crossfade(targetState = primaryUiState.showSearchBar, label = "") { currentView ->
            when(currentView) {
                true -> {
                    SearchView(
                        verticalGridState = searchNotesState,
                        primaryViewModel = primaryViewModel,
                        checklistViewModel = checklistViewModel,
                        primaryUiState = primaryUiState,
                        notesViewModel = notesViewModel,
                        keyboardController = keyboardController!!,
                        focusManager = focusManager
                    )
                }
                false -> {
                    AllEntriesView(
                        allEntries = allEntries,
                        favoriteEntries = favoriteEntries,
                        verticalGridState = allNotesState,
                        primaryViewModel = primaryViewModel,
                        checklistViewModel = checklistViewModel,
                        primaryUiState = primaryUiState,
                        notesViewModel = notesViewModel,
                        nestedScrollConnection = customNestedScrollConnection,
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
    nestedScrollConnection: NestedScrollConnection,
){
    val gridOrientation = if(primaryUiState.layoutView) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp - 50.dp

    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .animateContentSize()
            .nestedScroll(nestedScrollConnection),
        state = verticalGridState,
        columns = StaggeredGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
        contentPadding = PaddingValues(
            top = 55.dp,
            bottom = 15.dp,
            start = 10.dp,
            end = 10.dp
        )
    ) {

        item(
            key = UUID.randomUUID(),
            span = StaggeredGridItemSpan.FullLine,
            content = { Spacer(modifier = Modifier.height(15.dp)) }
        )


        items(
            items = favoriteEntries,
            key = { note -> note.uid!! },
            span = { gridOrientation }
        ) { favoritesEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                notesViewModel = notesViewModel,
                checklistViewModel = checklistViewModel,
                noteEntry = favoritesEntries,
            )
        }

        item(
            key = UUID.randomUUID(),
            span = StaggeredGridItemSpan.FullLine,
            content = {
                PinnedDivider(
                    modifier = Modifier.animateItemPlacement(),
                    hasFavorites = favoriteEntries.isNotEmpty(),
                    spacerWidth = screenWidth / 2
                )
            }
        )

        items(
            items = allEntries,
            key = { note -> note.uid!! },
            span = { gridOrientation }
        ) { allEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                notesViewModel = notesViewModel,
                checklistViewModel = checklistViewModel,
                noteEntry = allEntries,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchView(
    verticalGridState: LazyStaggeredGridState,
    primaryViewModel: PrimaryViewModel,
    checklistViewModel: ChecklistViewModel,
    primaryUiState: PrimaryUiState,
    notesViewModel: NotesViewModel,
    keyboardController: SoftwareKeyboardController,
    focusManager: FocusManager
){
    val gridOrientation = if(primaryUiState.layoutView) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane
    val customNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                keyboardController.hide()
                focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

    Column{

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .nestedScroll(customNestedScrollConnection)
                .background(colors.background),
            state = verticalGridState,
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            contentPadding = PaddingValues(
                top = 45.dp,
                bottom = 55.dp,
                start = 10.dp,
                end = 10.dp
            )
        ) {
            item(
                key = UUID.randomUUID(),
                span = StaggeredGridItemSpan.FullLine,
                content = { Spacer(modifier = Modifier.height(10.dp)) }
            )

            items(
                items = primaryUiState.searchEntries,
                key = { note -> note.uid!! },
                span = { gridOrientation }
            ) { searchResults ->
                EntryTemplate(
                    modifier = Modifier.animateItemPlacement(),
                    primaryViewModel = primaryViewModel,
                    notesViewModel = notesViewModel,
                    checklistViewModel = checklistViewModel,
                    noteEntry = searchResults,
                    isSearchQuery = true,
                )
            }
        }
        NoResultsFound(primaryUiState = primaryUiState)
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryTemplate(
    modifier: Modifier = Modifier,
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    checklistViewModel: ChecklistViewModel,
    noteEntry: Note,
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
                isSearchQuery = isSearchQuery,
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    primaryViewModel.deleteTally(noteEntry)
                },
                onEditClick = {
                    if(noteEntry.checkList.isNullOrEmpty()) {
                        primaryViewModel.cardFunctionSelection(
                            navigateEntry = { notesViewModel.navigateToNote(noteEntry,true) },
                            note = noteEntry
                        )
                    } else {
                        primaryViewModel.cardFunctionSelection(
                            navigateEntry = { checklistViewModel.editChecklist(noteEntry,true) },
                            note = noteEntry
                        )
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
                tint = colors.onSecondary,
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
                color = colors.onSecondary,
            )
        }
    }
}

@Composable
fun PinnedDivider(
    hasFavorites: Boolean,
    modifier: Modifier = Modifier,
    spacerWidth: Dp,
    spacerHeight: Dp = 4.dp,
    divColour: Color = colors.onBackground
) {
    if(hasFavorites) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Surface(
                modifier = Modifier
                    .width(spacerWidth)
                    .height(spacerHeight),
                color = divColour,
                shape = RoundedCornerShape(15.dp),
                content = { Text(text = "") }
            )
            Icon(
                modifier = Modifier
                    .padding(3.dp)
                    .size(13.dp)
                    .rotate(180f),
                painter = painterResource(id = R.drawable.pin_01 ),
                contentDescription = stringResource(R.string.Check),
                tint = colors.onSurface
            )

            Surface(
                modifier = Modifier
                    .width(spacerWidth)
                    .height(spacerHeight),
                color = divColour,
                shape = RoundedCornerShape(15.dp),
                content = { Text(text = "") }
            )
        }
    }
}