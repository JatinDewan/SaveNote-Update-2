package note.notes.savenote.Composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyScopeMarker
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import note.notes.savenote.Composable.Components.AppBars.NotesAndChecklistNavigationBar
import note.notes.savenote.Composable.Components.CustomTextField
import note.notes.savenote.Composable.Components.OptionMenus.OptionsMenuChecklist
import note.notes.savenote.Composable.Components.TextFieldPlaceHolder
import note.notes.savenote.PersistentStorage.RoomDatabase.CheckList
import note.notes.savenote.R
import note.notes.savenote.Utils.Keyboard
import note.notes.savenote.Utils.conditional
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.Utils.maxScrollFlingBehavior
import note.notes.savenote.Utils.observeAsState
import note.notes.savenote.ViewModel.model.ChecklistUiState
import note.notes.savenote.ViewModel.ChecklistViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.reorderable
import java.util.UUID


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistComposer(
    checklistViewModel: ChecklistViewModel,
    checklistUiState: ChecklistUiState,
    focusManager: FocusManager,
    saveAndExit:() -> Unit,
    reorderLazyListState: ReorderableLazyListState
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = keyboardAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()
    val showHeader = remember { derivedStateOf { reorderLazyListState.listState.firstVisibleItemIndex > 3 } }
    /*For future in app scaling, text scale should scale 1 size less than icon*/
    val iconScale = remember { mutableIntStateOf(20) }
    val textScale = remember { mutableIntStateOf(14) }
    val checkedEntries = remember {
        derivedStateOf { checklistViewModel.checklistUncheckedUpdater() }
    }
    val uncheckedEntries = remember {
        derivedStateOf { checklistViewModel.checklistCheckedUpdater() }
    }

    val customAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 200,
        easing = LinearEasing
    )

    val animateDivider = animateDpAsState(
        animationSpec = tween(250),
        targetValue = if (showHeader.value) 0.dp else 15.dp,
        label = ""
    )

    if(keyboard.value == Keyboard.Closed) {
        LaunchedEffect(!checklistUiState.reArrange) {
            focusManager.clearFocus()
            checklistViewModel.clearChecklistEdit()
        }
    }

    LaunchedEffect(stateLifecycle) {
        if(checklistViewModel.checklistEntry.text.isNotEmpty()) {
            if (stateLifecycle == Lifecycle.Event.ON_RESUME) {
                focusRequester.requestFocus()
            }
        }

        if (stateLifecycle == Lifecycle.Event.ON_PAUSE) {
            checklistViewModel.saveNewOrEditExistingChecklist()
        }
    }

    BackHandler { saveAndExit() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NotesAndChecklistNavigationBar(
                backButton = { saveAndExit() },
                buttonAction = {
                    focusManager.clearFocus()
                    checklistViewModel.moreOptionsMenu(!checklistUiState.showMoreOptionsMenu)
                },
                header = checklistUiState.header,
                showHeader = showHeader.value,
                moreOptionsOpened = checklistUiState.showMoreOptionsMenu,
                date = checklistViewModel.showDate()
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints{
                LazyColumn(
                    state = reorderLazyListState.listState,
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(vertical = 15.dp),
                    flingBehavior = maxScrollFlingBehavior(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .animateContentSize()
                        .windowInsetsPadding(WindowInsets.ime)
                        .reorderable(reorderLazyListState)

                ) {
                    item(key = 9038403832028408932) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .animateItemPlacement(customAnimationSpec)
                        ){
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = checklistViewModel.showDate(),
                                color = colors.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = UniversalFamily,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(7.dp))
                    }

                    item(key = 9038403849028408932) {
                        Column(modifier = Modifier.animateItemPlacement(customAnimationSpec)){
                            CustomTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp),
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                textColour = colors.onSecondary,
                                value = checklistUiState.header,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                onValueChange = { checklistViewModel.header(it) },
                                decorationBox = {
                                    TextFieldPlaceHolder(
                                        showPlaceHolder = checklistUiState.header.isEmpty(),
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        note = checklistUiState.fullChecklist
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    stickyHeader(
                        key = UUID.randomUUID().toString()
                    ) {
                        Column(modifier = Modifier.animateItemPlacement(customAnimationSpec)){
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = animateDivider.value)
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = colors.secondary,
                                shape = RoundedCornerShape(15.dp),
                                content = { Text(text = "") }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(7.dp))
                    }

                    items(
                        items = checkedEntries.value,
                        key = { notes -> notes.key!! },
                    ) { items ->
                        ReorderableItem(reorderableState = reorderLazyListState, key = items.key){
                            CheckList(
                                modifier = Modifier.detectReorder(reorderLazyListState),
                                modifierColumn = Modifier.animateItemPlacement(customAnimationSpec),
                                checkList = CheckList(items.note, items.strike, items.key),
                                editEntry = checklistUiState.checklistKey == items.key,
                                checklistViewModel = checklistViewModel,
                                reArrange = checklistUiState.reArrange,
                                focusManager = focusManager,
                                keyboard = keyboard,
                                textScale = textScale.intValue.sp,
                                iconScale = iconScale.intValue.dp,
                                isDragging = it
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(7.dp)) }

                    item(key = 3249702398470392847) {
                        NewEntry(
                            modifierColumn = Modifier
                                .conditional(!checklistUiState.isTextFocused) {
                                    it.animateItemPlacement(customAnimationSpec)
                                },
                            modifier = Modifier.padding(horizontal = 15.dp),
                            checklistViewModel = checklistViewModel,
                            checklistUiState = checklistUiState,
                            keyboard = keyboard,
                            textScale = textScale.intValue.sp,
                            iconScale = iconScale.intValue.dp
                        )
                    }

                    items(
                        items = uncheckedEntries.value,
                        key = { notes -> notes.key!! }
                    ) { items ->
                        CheckListCompleted(
                            modifierColumn = Modifier
                                .animateItemPlacement(customAnimationSpec)
                                .padding(horizontal = 6.dp),
                            checkList = CheckList(items.note, items.strike, items.key),
                            checklistViewModel = checklistViewModel,
                            showChecked = checklistUiState.showCompleted,
                            textScale = textScale.intValue.sp,
                            iconScale = iconScale.intValue.dp,
                        )
                    }
                }

                OptionsMenuChecklist(
                    dismiss = checklistUiState.showMoreOptionsMenu,
                    canReArrange = checklistUiState.reArrange,
                    showCompletedBoolean = checklistUiState.showCompleted,
                    expandedIsFalse = { checklistViewModel.moreOptionsMenu() },
                    unCheckCompleted = { checklistViewModel.unCheckCompleted() },
                    confirmDelete = { checklistViewModel.confirmDeleteAllChecked() },
                    share = { context.startActivity(checklistViewModel.shareChecklist()) },
                    reArrange = {
                        checklistViewModel.reArrange()
                        checklistViewModel.moreOptionsMenu()
                    },
                    showCompleted = {
                        checklistViewModel.updateShowCompleted(!checklistUiState.showCompleted)
                    }
                )
            }
        }
    }
}

@LazyScopeMarker
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewEntry(
    modifier: Modifier = Modifier,
    modifierColumn: Modifier = Modifier,
    textBoxModifier: Modifier = Modifier,
    checklistViewModel: ChecklistViewModel,
    checklistUiState: ChecklistUiState,
    keyboard: State<Keyboard>,
    iconScale: Dp,
    textScale: TextUnit
){
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(keyboard.value == Keyboard.Opened) {
        if(checklistUiState.isTextFocused){
            delay(100)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = modifierColumn.bringIntoViewRequester(bringIntoViewRequester)
    ){
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(bottom = 20.dp, top = 5.dp)
                .fillMaxWidth()

        ) {
            ChecklistIcons(
                modifier = Modifier.align(Alignment.Top),
                enabled = false,
                onClick = { },
                icon = R.drawable.plus,
                iconColour = colors.onSecondary,
                buttonSize = iconScale
            )

            CustomTextField(
                modifier = textBoxModifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) {
                            checklistViewModel.editChecklistEntry(0)
                        }
                        checklistViewModel.isTextFocused(it.isFocused)
                    },
                value = checklistViewModel.checklistEntry.text,
                onValueChange = { checklistViewModel.updateChecklistEntry(it) },
                maxLines = 15,
                onTextLayout = { checklistViewModel.bringInToViewRequester(bringIntoViewRequester) },
                keyboardAction = KeyboardActions(
                    onDone = {
                        checklistViewModel.addEntryToChecklist(bringIntoViewRequester)
                    }
                ),
                fontSize = textScale,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                decorationBox = {
                    TextFieldPlaceHolder(
                        showPlaceHolder = checklistViewModel.checklistEntry.text.isEmpty(),
                        text = R.string.AddEntry,
                        fontSize = textScale,
                        colour = animateColorAsState(
                            animationSpec = tween(200),
                            targetValue = if (checklistUiState.isTextFocused) colors.secondaryVariant else colors.primary,
                            label = ""
                        ).value
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckList(
    modifier: Modifier = Modifier,
    modifierColumn: Modifier = Modifier,
    checkList: CheckList,
    editEntry: Boolean,
    isDragging: Boolean,
    checklistViewModel: ChecklistViewModel,
    reArrange: Boolean,
    focusManager: FocusManager,
    keyboard: State<Keyboard>,
    iconScale: Dp,
    textScale: TextUnit,
){
    val myUiState by checklistViewModel.stateGetter.collectAsState()
    var updateEntry by rememberSaveable { mutableStateOf(checkList.note) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val focusedText = remember { mutableStateOf(false) }


    LaunchedEffect(keyboard.value == Keyboard.Opened) {
        if(focusedText.value){
             delay(100)
            bringIntoViewRequester.bringIntoView()
        }
    }

    val entryBackground: Color by animateColorAsState(
        targetValue = if (editEntry || isDragging) colors.secondary else colors.background,
        animationSpec = tween(150),
        label = ""
    )

    Column(
        modifier = modifierColumn
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(vertical = 10.dp)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxSize(),
            backgroundColor = entryBackground,
            elevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = if(editEntry || isDragging) 10.dp else 0.dp)) {
                AnimatedVisibility(
                    visible = reArrange,
                    content = {
                        ChecklistIcons(
                            modifier = modifier,
                            enabled = !editEntry,
                            onClick = { },
                            icon = R.drawable.drag_handle_01,
                            iconColour = colors.onSurface,
                            buttonSize = iconScale
                        )
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ChecklistIcons(
                        onClick = { checklistViewModel.deleteOrComplete(checkList) },
                        icon = checklistViewModel.iconSelection(
                            iconSelectionOne = isDragging,
                            iconSelectionTwo = myUiState.checklistKey == checkList.key
                        ),
                        buttonSize = iconScale
                    )
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focus ->
                                checklistViewModel.focusChange(
                                    focusState = focus,
                                    checkList = checkList,
                                    isEntryEmpty = updateEntry.isEmpty(),
                                    entry = updateEntry
                                )
                                focusedText.value = focus.isFocused
                            },
                        value = updateEntry,
                        fontSize = textScale,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        onTextLayout = {
                            checklistViewModel.bringInToViewRequest(
                                checkList = checkList,
                                bringIntoViewRequester = bringIntoViewRequester
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
}

@Composable
fun CheckListCompleted(
    modifier: Modifier = Modifier,
    modifierColumn: Modifier,
    showChecked: Boolean,
    checkList: CheckList,
    checklistViewModel: ChecklistViewModel,
    iconScale: Dp,
    textScale: TextUnit
){
    val firstIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) == 0
    val lastIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) ==
                            checklistViewModel.checklistCheckedUpdater().lastIndex
    val topShape = if (firstIndex) 15.dp else 0.dp
    val bottomShape = if (lastIndex) 15.dp else 0.dp

    if(showChecked){
        Column(modifier = modifierColumn){
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
                            start = 9.dp,
                            end = 9.dp,
                            top = if (firstIndex) 11.dp else 9.dp,
                            bottom = if (lastIndex) 11.dp else 9.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChecklistIcons(
                            modifier = Modifier.align(Alignment.Top),
                            onClick = { checklistViewModel.checklistCompletedTask(checkList) },
                            icon = R.drawable.check_circle,
                            buttonSize = iconScale
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = checkList.note,
                            color = colors.onSurface,
                            fontFamily = UniversalFamily,
                            fontSize = textScale
                        )
                    }
                    if (!lastIndex) Divider(color = colors.background)
                }
            }
        }
    }
}

@Composable
fun ChecklistIcons(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick:() -> Unit,
    buttonSize: Dp = 20.dp,
    iconColour: Color = colors.onSurface,
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
            modifier = Modifier.fillMaxSize()
        )
    }
}