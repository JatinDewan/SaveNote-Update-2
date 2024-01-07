package note.notes.savenote.Composables

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import note.notes.savenote.Composables.Components.AppBars.TopNavigationChecklist
import note.notes.savenote.Composables.Components.CustomTextField
import note.notes.savenote.Composables.Components.OptionMenus.MoreOptionsChecklist
import note.notes.savenote.Composables.Components.TextFieldPlaceHolder
import note.notes.savenote.Database.CheckList
import note.notes.savenote.R
import note.notes.savenote.Utils.Keyboard
import note.notes.savenote.Utils.SizeUtils
import note.notes.savenote.Utils.maxScrollFlingBehavior
import note.notes.savenote.Utils.observeAsState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ChecklistComposer(
    checklistViewModel: ChecklistViewModel,
    coroutineScope: CoroutineScope,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    keyboard: State<Keyboard>,
    context: Context,
    closeScreen:() -> Unit
) {

    val checklistUiState by checklistViewModel.uiState.collectAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()
    val state = rememberReorderableLazyListState(
        canDragOver = { from, _ -> checklistViewModel.dragRestriction(from.index) },
        onMove = { from, to -> checklistViewModel.onMoveIndexer(from.key, to.key) }
    )
    val showButton by remember { derivedStateOf { state.listState.firstVisibleItemIndex > 1 } }

//    if(keyboard.value == Keyboard.Closed) {
//        LaunchedEffect(!checklistUiState.reArrange) {
//            focusManager.clearFocus()
//            checklistViewModel.clearChecklistEdit()
//        }
//    }

//    LaunchedEffect(stateLifecycle) {
//        if(checklistViewModel.checklistEntry.text.isNotEmpty()) {
//            if (stateLifecycle == Lifecycle.Event.ON_RESUME) {
//                focusRequester.requestFocus()
//            }
//        }
//
//        if (stateLifecycle == Lifecycle.Event.ON_PAUSE) {
//            checklistViewModel.saveChecklistEdit()
//        }
//    }

    BackHandler(
        onBack = { checklistViewModel.onBackPress(closeScreen) }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopNavigationChecklist(
                backButton = {
                    checklistViewModel.pullUp()
                    focusManager.clearFocus()
                    checklistViewModel.returnAndSaveChecklist(closeScreen)
                },
                moreOptions = {
                    focusManager.clearFocus()
                    checklistViewModel.pullUp(!checklistUiState.isVisible)
                },
                header = checklistUiState.header,
                showHeader = showButton,
                moreOptionsOpened = checklistUiState.isVisible
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints{
                LazyColumn(
                    state = state.listState,
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(vertical = 5.dp),
                    flingBehavior = maxScrollFlingBehavior(),
                    modifier = Modifier
                        .padding(padding)
                        .background(colors.background)
                        .reorderable(state)
                ) {
                    stickyHeader(key = UUID.randomUUID()) {
                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp),
                            value = checklistUiState.header,
                            onValueChange = { checklistViewModel.header(it) },
                            decorationBox = {
                                TextFieldPlaceHolder(
                                    showPlaceHolder = checklistUiState.header.isEmpty(),
                                    text = R.string.Title,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            keyboardAction = KeyboardActions(
                                onDone = { checklistViewModel.addChecklistEntry() }
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            textColour = colors.onSecondary
                        )
                    }

                    item(key = UUID.randomUUID()) { Spacer(modifier = Modifier.height(10.dp)) }

                    stickyHeader(key = UUID.randomUUID()) { }

                    items(
                        items = checklistViewModel.checklistUncheckedUpdater(),
                        key = { notes -> notes.key!! },
                    ) { items ->
                        Spacer(modifier = Modifier.height(6.dp))
                        ReorderableItem(state, key = items.key) { isDragging ->
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                            ){
                                CheckList(
                                    modifier = Modifier.detectReorder(state),
                                    checkList = CheckList(items.note, items.strike, items.key),
                                    editEntry = checklistUiState.checklistKey == items.key,
                                    checklistViewModel = checklistViewModel,
                                    isDragging = isDragging,
                                    reArrange = checklistUiState.reArrange,
                                    focusManager = focusManager
                                )
                            }
                        }
                    }

                    item(key = UUID.randomUUID()) { Spacer(modifier = Modifier.height(10.dp)) }

                    item(key = UUID.randomUUID()) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .bringIntoViewRequester(bringIntoViewRequester)
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        ) {
                            ChecklistIcons(
                                modifier = Modifier.align(Alignment.Top),
                                enabled = false,
                                onClick = { },
                                icon = R.drawable.plus,
                                iconColour = colors.onSecondary
                            )

                            CustomTextField(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            checklistViewModel.editChecklistEntry(0)
//                                            coroutineScope.launch{ bringIntoViewRequester.bringIntoView() }
//                                            focusRequester.requestFocus()
                                        }
                                        if(!it.isFocused) {
                                            focusRequester.requestFocus()
                                        }
                                    },
                                value = checklistViewModel.checklistEntry.text,
                                onValueChange = { checklistViewModel.update(it) },
                                maxLines = 15,
                                onTextLayout = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } },
                                decorationBox = {
                                    TextFieldPlaceHolder(
                                        showPlaceHolder = checklistViewModel.checklistEntry.text.isEmpty(),
                                        text = R.string.AddEntry,
                                        fontSize = 15.sp
                                    )
                                },
                                keyboardAction = KeyboardActions(
                                    onDone = {
                                        checklistViewModel.addChecklistEntry()
                                    }
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                    }

                    item(key = UUID.randomUUID()) { Spacer(modifier = Modifier.height(10.dp)) }

                    items(
                        items = checklistViewModel.checklistCheckedUpdater(),
                        key = { notes -> notes.key!! }
                    ) { items ->
                        if(checklistUiState.showCompleted){
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                            ) {
                                CheckListCompleted(
                                    checkList = CheckList(items.note, items.strike, items.key),
                                    checklistViewModel = checklistViewModel,
                                )
                            }
                        }
                    }
                    item(key = UUID.randomUUID()) { Spacer(modifier = Modifier.height(10.dp)) }
                }

                MoreOptionsChecklist(
                    dismiss = checklistUiState.isVisible,
                    share = { context.startActivity(checklistViewModel.shareChecklist()) },
                    reArrange = {
                        checklistViewModel.reArrange(!checklistUiState.reArrange)
                        checklistViewModel.pullUp()
                    },
                    unCheckCompleted = {
                        checklistViewModel.unCheckCompleted()
                    },
                    confirmDelete = {
                        checklistViewModel.confirmDeleteAllChecked()
                    },
                    expandedIsFalse = { checklistViewModel.pullUp() },
                    showCompleted = {
                        checklistViewModel.updateShowCompleted(!checklistUiState.showCompleted)
                    },
                    showCompletedBoolean = checklistUiState.showCompleted,
                    canReArrange = checklistUiState.reArrange
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckList(
    modifier: Modifier = Modifier,
    checkList: CheckList,
    editEntry: Boolean,
    isDragging: Boolean,
    checklistViewModel: ChecklistViewModel,
    reArrange: Boolean,
    focusManager: FocusManager
){
    val myUiState by checklistViewModel.uiState.collectAsState()
    var updateEntry by rememberSaveable { mutableStateOf(checkList.note) }
    val localBringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }

    val entryBackground: Color by animateColorAsState(
        targetValue = if(editEntry || isDragging) colors.secondary else colors.background,
        animationSpec = tween(150),
        label = ""
    )

    Card(
        modifier = Modifier
            .bringIntoViewRequester(localBringIntoViewRequester)
            .fillMaxSize(),
        backgroundColor = entryBackground,
        elevation = 0.dp
    ) {
        Row {

            AnimatedVisibility(
                visible = reArrange,
                content = {
                    ChecklistIcons(
                        modifier = modifier,
                        enabled = !editEntry,
                        onClick = { },
                        icon = R.drawable.drag_handle_01,
                        iconColour = colors.onSurface
                    )
                }
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChecklistIcons(
                    onClick = { checklistViewModel.deleteOrComplete(checkList) },
                    icon = checklistViewModel.iconSelection(
                        iconSelectionOne = isDragging,
                        iconSelectionTwo = myUiState.checklistKey == checkList.key
                    )
                )

                CustomTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focus ->
                            checklistViewModel.focusChange(
                                focusState = focus,
                                checkList = checkList,
                                isEntryEmpty = updateEntry.isEmpty(),
                                entry = updateEntry
                            )
                        },
                    value = updateEntry,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onTextLayout = {
                        checklistViewModel.bringInToViewRequest(
                            checkList = checkList,
                            bringIntoViewRequester = localBringIntoViewRequester
                        )
                    },
                    keyboardAction = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            checklistViewModel.clearChecklistEdit()
                            checklistViewModel.entryEditOrAdd(
                                entry = updateEntry,
                                checkList = checkList
                            )
                        }
                    ),
                    onValueChange = {
                        updateEntry = it
                        checklistViewModel.entryEditOrAdd(
                            entry = updateEntry,
                            deletable = false,
                            checkList = checkList
                        )
                    },
                    decorationBox = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
fun CheckListCompleted(
    modifier: Modifier = Modifier,
    checkList: CheckList,
    checklistViewModel: ChecklistViewModel,
){
    val size = SizeUtils()
    val firstIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) == 0
    val lastIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) ==
            checklistViewModel.checklistCheckedUpdater().lastIndex
    val topShape = size.DPISelection(firstIndex, 15.dp, 0.dp )
    val bottomShape = size.DPISelection(lastIndex, 15.dp, 0.dp )

    Card(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.secondary,
        shape = RoundedCornerShape(
            topStart = topShape, topEnd = topShape,
            bottomStart = bottomShape, bottomEnd = bottomShape
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    start = 5.dp,
                    end = 5.dp,
                    top = if(firstIndex) 7.dp else 5.dp,
                    bottom =  if(lastIndex) 7.dp else 5.dp
                ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                ChecklistIcons(
                    modifier = Modifier.align(Alignment.Top),
                    onClick = { checklistViewModel.checklistCompletedTask(checkList) },
                    icon = R.drawable.check_circle,
                    iconSize = 20.dp
                )
                Text(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(),
                    text = checkList.note,
                    color = colors.onSurface,
                    fontFamily = UniversalFamily,
                    fontSize = 15.sp
                )
            }
            if(!lastIndex) Divider(color = colors.background)
        }
    }
}

@Composable
fun ChecklistIcons(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick:() -> Unit,
    buttonSize: Dp = 30.dp,
    iconSize: Dp = 25.dp,
    iconColour: Color = colors.primary,
    icon: Int
){
    IconButton(
        enabled = enabled,
        modifier = modifier.size(buttonSize),
        onClick = onClick::invoke,
    ) {
        Icon(
            tint = iconColour,
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.Check),
            modifier = Modifier.size(iconSize)
        )
    }
}