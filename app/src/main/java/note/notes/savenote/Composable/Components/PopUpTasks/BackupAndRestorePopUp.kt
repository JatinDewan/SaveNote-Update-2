package note.notes.savenote.Composable.Components.PopUpTasks

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R
import note.notes.savenote.ViewModel.PrimaryViewModel

@Composable
fun BackupAndRestore(
    primaryViewModel: PrimaryViewModel,
    isVisible: Boolean,
    dismiss:() -> Unit
){
    var showMore by remember { mutableStateOf(false) }
    val interactionSource = MutableInteractionSource()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            uri -> primaryViewModel.backUpNotes(uri,context)
    }
    val restoreBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            uri -> primaryViewModel.restoreNotes(uri,context)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ){
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { dismiss() }
                )
                .fillMaxSize()
                .background(MaterialTheme.colors.background.copy(0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                backgroundColor = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(0.9f),
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    InstructionsHeader()

                    AnimatedVisibility(
                        visible = showMore,
                        enter = expandVertically(tween(100)) + fadeIn(tween(100)),
                        exit = shrinkVertically(tween(100)) + fadeOut(tween(100))
                    ) {
                        Column(
                            modifier = Modifier
                                .height(260.dp)
                                .verticalScroll(scrollState)
                                .background(
                                    MaterialTheme.colors.onBackground,
                                    RoundedCornerShape(10.dp)
                                )
                        ){
                            Column(
                                modifier = Modifier.padding(7.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ){
                                BackUpNotesInstructions()

                                RestoreNotesInstructions()
                            }
                        }
                    }

                    Text(
                        modifier = Modifier.clickable { showMore = !showMore },
                        text = if (showMore)
                            stringResource(id = R.string.ShowLess) else
                            stringResource(id = R.string.ShowMore),
                        color = MaterialTheme.colors.onSurface,
                        fontSize = 16.sp
                    )

                    BackupAndRestoreOptions(
                        backUp = { createBackup.launch("SaveNote_DB") },
                        restore = { restoreBackup.launch(arrayOf("text/plain")) }
                    )
                }
            }
        }
    }
}


@Composable
fun InstructionsHeader(){
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)){
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_hexagon),
                contentDescription = stringResource(id = R.string.Instructions),
                tint = MaterialTheme.colors.primary
            )
            Text(
                text = stringResource(id = R.string.Instructions),
                color = MaterialTheme.colors.primaryVariant,
                fontSize = 14.sp
            )
        }

        Text(
            text = stringResource(id = R.string.BackupInfo),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BackUpNotesInstructions(){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(
            text = stringResource(id = R.string.BackupStep1),
            color = MaterialTheme.colors.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            InstructionSteps(
                instructionNumber = 1,
                instructionInformation = R.string.BackupStep2
            )

            InstructionSteps(
                instructionNumber = 2,
                instructionInformation = R.string.BackupStep3
            )

            InstructionSteps(
                instructionNumber = 3,
                instructionInformation = R.string.BackupStep4
            )
        }
    }
}

@Composable
fun RestoreNotesInstructions(){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(
            text = stringResource(id = R.string.RestoreNoteHeader),
            color = MaterialTheme.colors.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            InstructionSteps(
                instructionNumber = 1,
                instructionInformation = R.string.RestoreStep1
            )

            InstructionSteps(
                instructionNumber = 2,
                instructionInformation = R.string.RestoreStep2
            )

            InstructionSteps(
                instructionNumber = 3,
                instructionInformation = R.string.RestoreStep3
            )

            InstructionSteps(
                instructionNumber = 4,
                instructionInformation = R.string.RestoreStep4
            )
        }
    }
}


@Composable
fun InstructionSteps(
    instructionNumber: Int,
    instructionInformation: Int
) {
    Row(
        verticalAlignment = Alignment.Top
    ){
        Text(
            text = stringResource(id = R.string.InstructionsOrder, instructionNumber.toString()),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = stringResource(id = instructionInformation),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BackupAndRestoreOptions(
    backUp: () -> Unit,
    restore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BackupAndRestoreButton(
            icon = R.drawable.database_01,
            text = R.string.Backup,
            onClick = backUp::invoke
        )

        BackupAndRestoreButton(
            icon = R.drawable.refresh_ccw_01,
            text = R.string.Restore,
            onClick = restore::invoke,
            elevation = 0.dp,
            colour = Color.Transparent
        )
    }
}

@Composable
fun BackupAndRestoreButton(
    icon: Int,
    text: Int,
    onClick:() -> Unit,
    colour: Color = MaterialTheme.colors.background,
    elevation: Dp = 5.dp
){
    Card(
        backgroundColor = colour,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.width(150.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
        elevation = elevation
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = text),
                tint = MaterialTheme.colors.primary
            )
            Text(
                text = stringResource(id = text),
                color = MaterialTheme.colors.primaryVariant,
                fontSize = 14.sp
            )
        }
    }
}