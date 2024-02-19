package note.notes.savenote.Composable.Components.Templates

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.PersistentStorage.RoomDatabase.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.rangeFinder
import note.notes.savenote.ViewModel.PrimaryViewModel
import note.notes.savenote.ViewModel.model.PrimaryUiState
import note.notes.savenote.ui.theme.UniversalFamily

@ExperimentalFoundationApi
@Composable
fun EntryCards(
    modifier: Modifier = Modifier,
    primaryViewModel: PrimaryViewModel,
    onEditClick: () -> Unit,
    onLongPress: () -> Unit,
    isSearchQuery: Boolean = false,
    note: Note
) {
    val selected = primaryViewModel.temporaryEntryHold.contains(note)
    val primaryViewState by primaryViewModel.stateGetter.collectAsState()

    val backgroundColour: Color by animateColorAsState(
        targetValue = if(selected) colors.secondaryVariant else colors.secondary,
        animationSpec = tween(150),
        label = ""
    )

    val dateColour: Color by animateColorAsState(
        targetValue = if(selected) colors.background else colors.onSurface,
        animationSpec = tween(150),
        label = ""
    )

    Card(
        backgroundColor = backgroundColour,
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onEditClick() },
                    onLongClick = { onLongPress() }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(
                modifier = Modifier.padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                HeaderDisplay(
                    note = note,
                    primaryViewModel = primaryViewState,
                    isSearchQuery = isSearchQuery
                )

                when(note.checkList.isNullOrEmpty()) {
                    true -> NoteDisplay(
                        note = note,
                        primaryViewModel = primaryViewState,
                        isSearchQuery = isSearchQuery
                    )
                    else -> CheckListDisplay(
                        note = note,
                        primaryViewModel = primaryViewState,
                        isSearchQuery = isSearchQuery
                    )
                }
            }

            Divider(color = colors.onBackground)

            AdditionalInformation(
                note = note,
                dateColour = dateColour,
                currentDate = { date -> primaryViewModel.dateAndTimeDisplay(date, note) }
            )
        }
    }
}


@Composable
fun HeaderDisplay(
    note: Note,
    isSearchQuery: Boolean,
    primaryViewModel: PrimaryUiState,
    fontSize: TextUnit = 16.sp
) {
    if(!note.header.isNullOrEmpty()){
        when (isSearchQuery) {
            false -> {
                Text(
                    text = note.header,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = colors.onSecondary,
                    fontFamily = UniversalFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            true -> {
                HighlightedText(
                    text = note.header,
                    selectedString = primaryViewModel.searchQuery,
                    maxLines = 1,
                    fontSize = fontSize
                )
            }
        }
    } else {
        Text(
            text = "Note #${note.uid}",
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = colors.onSecondary,
            fontFamily = UniversalFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NoteDisplay(
    note: Note,
    isSearchQuery: Boolean,
    primaryViewModel: PrimaryUiState
) {
    if(!note.note.isNullOrEmpty()){
        when (isSearchQuery) {
            false -> {
                Text(
                    text = note.note,
                    fontSize = 12.sp,
                    color = colors.primaryVariant,
                    fontFamily = UniversalFamily,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
            true -> {
                HighlightedText(
                    text = note.note,
                    selectedString = primaryViewModel.searchQuery,
                    maxLines = 10,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        Text(
            text = "Empty note",
            color = colors.onSurface,
            fontFamily = UniversalFamily,
            maxLines = 3,
            fontSize = 13.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CheckListDisplay(
    note: Note,
    isSearchQuery: Boolean,
    primaryViewModel: PrimaryUiState
) {
    val checklistDisplay by remember { derivedStateOf { note.checkList?.rangeFinder(5) } }
    val completedTasks by remember { derivedStateOf { note.checkList?.filter { it.strike == 1 } } }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if(!checklistDisplay.isNullOrEmpty()){
            checklistDisplay?.forEach { checklistEntries ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        tint = colors.onSurface,
                        painter = painterResource(id = R.drawable.circle),
                        contentDescription = stringResource(R.string.Check)
                    )

                    when (isSearchQuery) {
                        true -> {
                            HighlightedText(
                                text = checklistEntries.note,
                                selectedString = primaryViewModel.searchQuery,
                                maxLines = 3,
                                fontSize = 12.sp
                            )
                        }

                        else -> {
                            Text(
                                text = checklistEntries.note,
                                color = colors.primaryVariant,
                                fontFamily = UniversalFamily,
                                maxLines = 3,
                                fontSize = 12.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = stringResource(id = R.string.CompletedEntries, completedTasks?.size!!),
                    color = colors.onSurface,
                    fontFamily = UniversalFamily,
                    maxLines = 3,
                    fontSize = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AdditionalInformation(
    note: Note,
    dateColour: Color,
    currentDate: (String) -> String
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(
                    horizontal = 15.dp,
                    vertical = 5.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            note.date?.let {
                Text(
                    text = currentDate(it),
                    fontSize = 10.sp,
                    color = dateColour,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = UniversalFamily,
                    maxLines = 1,
                )
            }
            Icon(
                modifier = Modifier.size(12.dp),
                tint = dateColour,
                painter = painterResource(
                    if(note.checkList.isNullOrEmpty()) R.drawable.pencil_01 else R.drawable.dotpoints_01
                ),
                contentDescription = stringResource(R.string.Check),
            )
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    selectedString: String,
    maxLines: Int,
    fontSize: TextUnit,
    colour: Color = colors.primaryVariant
) {
    val color = colors.primary.copy(0.6f)

    val annotatedText = remember(text, selectedString) {
        AnnotatedString.Builder(text).apply {
            if (selectedString.isNotEmpty()){
                val regex = selectedString.lowercase().toRegex()
                val matches = regex.findAll(text.lowercase())
                matches.forEach { match ->
                    addStyle(
                        style = SpanStyle(background = color),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
        }.toAnnotatedString()
    }

    Text(
        text = annotatedText,
        fontSize = fontSize,
        color = colour,
        fontFamily = UniversalFamily,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}



