package note.notes.savenote.Composables.Components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.PersistentStorage.roomDatabase.Note
import note.notes.savenote.R
import note.notes.savenote.Utils.HighlightedText
import note.notes.savenote.Utils.rangeFinder
import note.notes.savenote.ViewModelClasses.PrimaryUiState
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
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
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    when(isSearchQuery) {
        false -> {
            note.header?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = colors.onSecondary,
                    fontFamily = UniversalFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        true -> {
            note.header?.let {
                HighlightedText(
                    text = it,
                    selectedString = primaryViewModel.searchQuery,
                    maxLines = 1,
                    fontSize = fontSize
                )
            }
        }
    }
}

@Composable
fun NoteDisplay(
    note: Note,
    isSearchQuery: Boolean,
    primaryViewModel: PrimaryUiState
) {
    when(isSearchQuery) {
        false -> {
            note.note?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = colors.primaryVariant,
                    fontFamily = UniversalFamily,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        true -> {
            note.note?.let {
                HighlightedText(
                    text = it,
                    selectedString = primaryViewModel.searchQuery,
                    maxLines = 10,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CheckListDisplay(
    note: Note,
    isSearchQuery: Boolean,
    primaryViewModel: PrimaryUiState
) {
    val checklistDisplay by remember { derivedStateOf { note.checkList?.rangeFinder(5) } }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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

                when(isSearchQuery) {
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
        }
    }
}



