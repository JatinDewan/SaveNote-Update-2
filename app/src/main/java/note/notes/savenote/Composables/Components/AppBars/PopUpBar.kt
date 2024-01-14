package note.notes.savenote.Composables.Components.AppBars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.IconButton
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun BottomPopUpBar(
    primaryViewModel: PrimaryViewModel,
) {
    val primaryView by primaryViewModel._uiState.collectAsState()
    val checkContains by remember { derivedStateOf { primaryViewModel.temporaryEntryHold.containsAll(primaryView.allEntries + primaryView.favoriteEntries) } }

    val selectAllIndicator: Color by animateColorAsState(
        targetValue = if(checkContains) colors.primary else colors.onSecondary,
        animationSpec = tween(150),
        label = ""
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = primaryViewModel.temporaryEntryHold.isNotEmpty(),
            enter = slideInVertically(tween(200)) { fullHeight -> fullHeight } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(200)) { fullHeight -> fullHeight } + fadeOut(tween(200))
        ) {
            Column {
                Divider(color = colors.onBackground)
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .background(colors.background),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.height(38.dp),
                        elevation = 0.dp,
                        shape = RoundedCornerShape(15),
                        backgroundColor = colors.onBackground,
                        border = BorderStroke(1.dp, colors.onBackground)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.4f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            DeleteSelected(
                                modifier = Modifier.weight(1f),
                                onClick = { primaryViewModel.confirmDelete(true) },
                                deleteTallysize = primaryViewModel.containerSize()
                            )

                            PopUpBarButtons(
                                modifier = Modifier.weight(1f),
                                onClick = { primaryViewModel.duplicateSelectedNotes() },
                                icon = R.drawable.copy_07,
                                contentDescription = R.string.DeleteNote,
                            )

                            PopUpBarButtons(
                                modifier = Modifier.weight(1f),
                                onClick = { primaryViewModel.selectAllNotes() },
                                icon = R.drawable.grid_02,
                                contentDescription = R.string.DeleteNote,
                                tint = selectAllIndicator
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(15),
                        elevation = 0.dp,
                        backgroundColor = colors.onBackground,
                        border = BorderStroke(1.dp, colors.onBackground)
                    ) {

                        PopUpBarButtons(
                            onClick = { primaryViewModel.favouriteSelected("favourite") },
                            icon = R.drawable.pin_01,
                            contentDescription = R.string.DeleteNote,
                            tint = colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopUpBarButtons(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Int,
    contentDescription: Int,
    tint: Color = colors.onSecondary
){
    IconButton(
        modifier = modifier,
        onClick = onClick::invoke
    ) {
        Icon(
            modifier = Modifier.size(23.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(contentDescription),
            tint = tint
        )
    }
}

@Composable
fun DeleteSelected(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    deleteTallysize: String
){
    IconButton(
        modifier = modifier,
        onClick = onClick::invoke
    ) {
        BoxWithConstraints {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    modifier = Modifier.size(23.dp),
                    painter = painterResource(id = R.drawable.trash_02),
                    contentDescription = stringResource(R.string.DeleteNote),
                    tint = colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .size(30.dp)
                    .padding(
                        bottom = 5.dp,
                        end = 13.dp
                    ),
                contentAlignment = Alignment.BottomEnd
            ){
                Card(
                    modifier = Modifier.size(15.dp),
                    backgroundColor = colors.onError,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()){
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = deleteTallysize,
                            color = colors.onSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = UniversalFamily,
                            maxLines = 1,
                            fontSize = 8.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }
    }
}
