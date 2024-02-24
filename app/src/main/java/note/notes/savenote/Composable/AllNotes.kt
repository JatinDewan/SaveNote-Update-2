package note.notes.savenote.Composable

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Composable.Components.AppBars.BottomPopUpBar
import note.notes.savenote.Composable.Components.AppBars.HeaderText
import note.notes.savenote.Composable.Components.AppBars.TopNavigationBarHome
import note.notes.savenote.Composable.Components.OptionMenus.NewEntryButton
import note.notes.savenote.Composable.Components.OptionMenus.OptionsMenuMainView
import note.notes.savenote.Composable.Components.PopUpTasks.BackupAndRestore
import note.notes.savenote.Composable.Components.PopUpTasks.ConfirmDelete
import note.notes.savenote.Composable.Components.Templates.EntryCards
import note.notes.savenote.Composable.Components.Templates.LoadingScreen
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.animateOnScroll
import note.notes.savenote.Utils.customNestedScrollConnection
import note.notes.savenote.Utils.rememberForeverLazyListState
import note.notes.savenote.ViewModel.ChecklistViewModel
import note.notes.savenote.ViewModel.NotesViewModel
import note.notes.savenote.ViewModel.PrimaryViewModel
import note.notes.savenote.ViewModel.model.PrimaryUiState
import note.notes.savenote.ui.theme.UniversalFamily
import java.util.UUID

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllNotesView(
    modifier: Modifier = Modifier,
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    primaryUiState: PrimaryUiState,
    allEntries: List<Note>,
    favoriteEntries: List<Note>,
    checklistViewModel: ChecklistViewModel,
    focusManager: FocusManager,
    navigateChecklistView: () -> Unit,
    navigateNoteView: () -> Unit,
    onEditClick: (Note) -> Unit
) {

    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val allNotesState = rememberForeverLazyListState(key = UUID.randomUUID().toString())
    val searchNotesState = rememberForeverLazyListState(key = UUID.randomUUID().toString())
    val allNotesObserver = remember { derivedStateOf{ allNotesState.firstVisibleItemIndex > 0 } }
    val allowAnimation = remember { mutableStateOf(false) }
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomPopUpBar(primaryViewModel = primaryViewModel)

            OptionsMenuMainView(
                dismiss = primaryUiState.dropDown,
                backUp = { primaryViewModel.backup(true) },
                rateApp = { primaryViewModel.rateApp(context) },
                expandedIsFalse = { primaryViewModel.dropDown(false) },
                help = { primaryViewModel.help(context) },
                primaryViewModel = primaryViewModel,
                sortBy = { primaryViewModel.updateSortByPreference() },
                themeBy = { primaryViewModel.updateTheme() }
            )

            TopNavigationBarHome(
                startedScrolling = allNotesObserver.value,
                primaryViewModel = primaryViewModel,
                primaryUiState = primaryUiState,
                focusRequester = focusRequester,
                startSearch = { primaryViewModel.startSearch(focusRequester) },
                endSearch = { primaryViewModel.endSearch(focusManager) },
                moreOptions = { primaryViewModel.dropDown() },
                changeView = { primaryViewModel.selectLayout() },
                offset =  animateOnScroll(animateScroll = allowAnimation, offset = toolbarOffsetHeightPx)
            )

            NewEntryButton(
                dismiss = primaryUiState.newEntryButton,
                expand = { primaryViewModel.newEntryButton(true) },
                collapse = { primaryViewModel.newEntryButton() },
                hideButton = !primaryUiState.showSearchBar && !primaryUiState.dropDown,
                navigateNewChecklist = {
                    checklistViewModel.navigateNewChecklist()
                    navigateChecklistView()
                },
                navigateNewNote = {
                    notesViewModel.navigateNewNote()
                    navigateNoteView()
                }
            )

            ConfirmDelete(
                popUp = primaryUiState.confirmDelete,
                cancel = { primaryViewModel.confirmDelete(false) },
                confirmDelete = { primaryViewModel.deleteSelected() },
                confirmMessage = stringResource(
                    id = R.string.ConfirmDelete,"${primaryUiState.temporaryEntryHold.size}"
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
                        primaryUiState = primaryUiState,
                        keyboardController = keyboardController!!,
                        focusManager = focusManager,
                        onEditClick = { onEditClick(it) }
                    )
                }
                false -> {
                    AllEntriesView(
                        allEntries = allEntries,
                        favoriteEntries = favoriteEntries,
                        verticalGridState = allNotesState,
                        primaryViewModel = primaryViewModel,
                        primaryUiState = primaryUiState,
                        onEditClick = { onEditClick(it) },
                        nestedScrollConnection = customNestedScrollConnection(
                            toolbarOffsetHeightPx = toolbarOffsetHeightPx,
                            allNotesObserver = allNotesObserver,
                            allowAnimation = allowAnimation
                        )
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
    primaryUiState: PrimaryUiState,
    nestedScrollConnection: NestedScrollConnection,
    onEditClick: (Note) -> Unit
){
    val gridOrientation = if (primaryUiState.layoutView)
        StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane

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
            top = 60.dp,
            bottom = 15.dp,
            start = 10.dp,
            end = 10.dp
        )
    ) {
        item { Spacer(modifier = Modifier.height(0.dp)) }

        item(
            key = UUID.randomUUID(),
            span = StaggeredGridItemSpan.FullLine
        ) {
            PinnedDivider(
                modifier = Modifier.animateItemPlacement(),
                hasFavorites = favoriteEntries.isNotEmpty(),
                text = R.string.Pinned,
                icon = R.drawable.pin_01,
                addSpacer = false,
            )
        }

        items(
            items = favoriteEntries,
            key = { note -> note.uid!! },
            span = { gridOrientation }
        ) { favoritesEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                noteEntry = favoritesEntries,
                onEditClick = { onEditClick(favoritesEntries) }
            )
        }

        item(
            key = UUID.randomUUID(),
            span = StaggeredGridItemSpan.FullLine
        ) {
            PinnedDivider(
                modifier = Modifier.animateItemPlacement(),
                hasFavorites = favoriteEntries.isNotEmpty(),
                text = R.string.Others,
                addSpacer = true
            )
        }

        items(
            items = allEntries,
            key = { note -> note.uid!! },
            span = { gridOrientation }
        ) { allEntries ->
            EntryTemplate(
                modifier = Modifier.animateItemPlacement(),
                primaryViewModel = primaryViewModel,
                noteEntry = allEntries,
                onEditClick = { onEditClick(allEntries) }
            )
        }

        item(
            key = UUID.randomUUID(),
            span = StaggeredGridItemSpan.FullLine
        ) {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun SearchView(
    verticalGridState: LazyStaggeredGridState,
    primaryViewModel: PrimaryViewModel,
    primaryUiState: PrimaryUiState,
    keyboardController: SoftwareKeyboardController,
    focusManager: FocusManager,
    onEditClick: (Note) -> Unit
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

    BackHandler { primaryViewModel.endSearch(focusManager) }

    Column{
        Spacer(modifier = Modifier.height(60.dp))

        Column{
            ResultsFound(primaryUiState = primaryUiState)
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .nestedScroll(customNestedScrollConnection)
                    .windowInsetsPadding(WindowInsets.imeAnimationTarget),
                state = verticalGridState,
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                contentPadding = PaddingValues(
                    bottom = 55.dp,
                    start = 10.dp,
                    end = 10.dp
                )
            ) {
                items(
                    items = primaryUiState.searchEntries,
                    key = { note -> note.uid!! },
                    span = { gridOrientation }
                ) { searchResults ->
                    EntryTemplate(
                        modifier = Modifier.animateItemPlacement(),
                        primaryViewModel = primaryViewModel,
                        noteEntry = searchResults,
                        isSearchQuery = true,
                        onEditClick = { keyboardController.hide(); onEditClick(searchResults) }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryTemplate(
    modifier: Modifier = Modifier,
    primaryViewModel: PrimaryViewModel,
    noteEntry: Note,
    isSearchQuery: Boolean = false,
    onEditClick: () -> Unit

){
    val haptic = LocalHapticFeedback.current

    Column(modifier.animateContentSize(tween(500))){
        AnimatedContent(
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            targetState = noteEntry,
            label = ""
        ){ _ ->
            EntryCards(
                primaryViewModel = primaryViewModel,
                note = noteEntry,
                isSearchQuery = isSearchQuery,
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    primaryViewModel.noteSelector(noteEntry)
                },
                onEditClick = onEditClick::invoke
            )
        }
    }
}

@Composable
fun ResultsFound(
    primaryUiState: PrimaryUiState
) {
    AnimatedVisibility(
        visible = primaryUiState.showSearchBar && primaryUiState.searchQuery.isNotEmpty(),
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(0))
    ){
        Row(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                tint = colors.secondaryVariant,
                painter = painterResource(id = R.drawable.search_lg),
                contentDescription = stringResource(R.string.Check),
            )
            Text(
                text = "Results found ",
                fontSize = 14.sp,
                fontFamily = UniversalFamily,
                color = colors.secondaryVariant,
            )

            Text(
                text = "${primaryUiState.searchEntries.size}",
                fontSize = 14.sp,
                fontFamily = UniversalFamily,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PinnedDivider(
    modifier: Modifier = Modifier,
    hasFavorites: Boolean,
    addSpacer: Boolean = false,
    text: Int,
    icon: Int? = null
) {
    if(hasFavorites) {
        Column(modifier = modifier){
            if(addSpacer){ Spacer(modifier = Modifier.height(30.dp)) }
            Row(
                modifier = modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        modifier = Modifier.size(11.dp),
                        painter = painterResource(id = icon),
                        contentDescription = stringResource(text),
                        tint = colors.primary
                    )
                }
                HeaderText(
                    title = text,
                    fontSize = 13.sp,
                    color = colors.primary
                )
            }
        }
    }
}