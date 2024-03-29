package note.notes.savenote.Composable.Components.OptionMenus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import note.notes.savenote.Composable.Components.Templates.ButtonEntries
import note.notes.savenote.Composable.Components.Templates.OptionMenuContainer
import note.notes.savenote.R
import note.notes.savenote.ViewModel.PrimaryViewModel

@Composable
fun OptionsMenuChecklist(
    modifier: Modifier = Modifier,
    dismiss: Boolean,
    canReArrange: Boolean,
    showCompletedBoolean: Boolean,
    share:() -> Unit,
    reArrange:() -> Unit,
    unCheckCompleted:() -> Unit,
    confirmDelete:() -> Unit,
    expandedIsFalse:() -> Unit,
    showCompleted:() -> Unit
) {
    var confirmDeleteMessage by remember { mutableStateOf(false) }
    val defaultTextBackgroundColour = colors.onBackground

    val showCompletedBackgroundColour: Color by animateColorAsState(
        targetValue = if(showCompletedBoolean) colors.primary else colors.onSecondary,
        animationSpec = tween(150),
        label = ""
    )

    val reArrangeBackgroundColour: Color by animateColorAsState(
        targetValue = if(canReArrange) colors.primary else colors.onSecondary,
        animationSpec = tween(200),
        label = ""
    )

    val confirmDeleteColour: Color by animateColorAsState(
        animationSpec = tween(durationMillis = 200, delayMillis = 200),
        targetValue = if(confirmDeleteMessage) colors.onError else defaultTextBackgroundColour,
        label = ""
    )

    val confirmDeleteColourIcon: Color by animateColorAsState(
        animationSpec = tween(durationMillis = 200, delayMillis = 200),
        targetValue = if(confirmDeleteMessage) colors.onError else colors.onSecondary,
        label = ""
    )

    OptionMenuContainer(
        modifier = modifier.padding(4.dp),
        dismiss = dismiss,
        expandedIsFalse = expandedIsFalse::invoke,
        additionalDismissFunction = { confirmDeleteMessage = false }
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            ButtonEntries(
                buttonFunction = share::invoke,
                entryLabel = R.string.ShareList,
                entryIcon = R.drawable.share_03,
                dismiss = dismiss,
                animationDelay = 100,
                iconBackgroundColour = colors.onSecondary,
                textBackgroundColour = defaultTextBackgroundColour,
                iconColour = colors.background,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                buttonFunction = reArrange::invoke,
                entryLabel = R.string.ReArrange,
                entryIcon = R.drawable.switch_vertical_01,
                dismiss = dismiss,
                animationDelay = 150,
                iconBackgroundColour = reArrangeBackgroundColour,
                textBackgroundColour = defaultTextBackgroundColour,
                iconColour = colors.background,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                buttonFunction = showCompleted::invoke,
                entryLabel = if(showCompletedBoolean) R.string.HideCompleted else R.string.ShowCompleted,
                entryIcon = R.drawable.eye,
                dismiss = dismiss,
                animationDelay = 200,
                iconBackgroundColour = showCompletedBackgroundColour,
                textBackgroundColour = defaultTextBackgroundColour,
                iconColour = colors.background,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                buttonFunction = unCheckCompleted::invoke,
                entryLabel = R.string.UncheckEntries,
                entryIcon = R.drawable.circle,
                dismiss = dismiss,
                animationDelay = 250,
                iconBackgroundColour = colors.onSecondary,
                textBackgroundColour = defaultTextBackgroundColour,
                iconColour = colors.background,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                textBoxModifier = Modifier.animateContentSize(),
                buttonFunction = { confirmDeleteMessage = if(!confirmDeleteMessage) true else { confirmDelete(); false } },
                entryLabel = if (!confirmDeleteMessage) R.string.DeleteChecked else R.string.TapToConfirm,
                entryIcon = R.drawable.trash_02,
                dismiss = dismiss,
                animationDelay = 300,
                iconBackgroundColour = confirmDeleteColourIcon,
                textBackgroundColour = confirmDeleteColour,
                iconColour = colors.background,
                textColour = colors.onSecondary

            )
        }
    }
}

@Composable
fun OptionsMenuMainView(
    modifier: Modifier = Modifier,
    dismiss: Boolean,
    backUp:() -> Unit,
    rateApp:() -> Unit,
    themeBy:() -> Unit,
    expandedIsFalse:() -> Unit,
    help:() -> Unit,
    sortBy:() -> Unit,
    primaryViewModel: PrimaryViewModel,
    menuPadding: Dp = 7.dp
) {
    val primaryUiState by primaryViewModel.stateGetter.collectAsState()
    val textBackgroundColour = colors.onBackground
    val iconBackgroundColour = colors.onSecondary
    val chooseTheme = remember {
        derivedStateOf {
            when (primaryUiState.setTheme) {
                1 -> R.string.Legacy
                2 -> R.string.Dark
                else -> R.string.Light
            }
        }
    }

    val chooseSortBy = remember {
        derivedStateOf {
            when (primaryUiState.sortByView) {
                1 -> R.string.LastEdit
                2 -> R.string.Oldest
                else -> R.string.Newest
            }
        }
    }

    OptionMenuContainer(
        modifier = modifier.padding(top = 64.dp),
        dismiss = dismiss,
        expandedIsFalse = expandedIsFalse::invoke
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButtonEntries(
                buttonFunction = sortBy::invoke,
                entryLabel = R.string.Sort,
                entryIcon = R.drawable.sort_by,
                dismiss = dismiss,
                animationDelay = 100,
                padding = menuPadding,
                textChangeCondition = primaryUiState.sortByView,
                additionalText = chooseSortBy.value,
                iconBackgroundColour = iconBackgroundColour,
                textBackgroundColour = textBackgroundColour,
                textColour = colors.onSecondary,
                additionalTextSpacing = 4.dp
            )

            ButtonEntries(
                buttonFunction = themeBy::invoke,
                entryLabel = R.string.Theme,
                entryIcon = R.drawable.paint,
                dismiss = dismiss,
                animationDelay = 150,
                padding = menuPadding,
                textChangeCondition = primaryUiState.setTheme,
                additionalText = chooseTheme.value,
                iconBackgroundColour = iconBackgroundColour,
                textBackgroundColour = textBackgroundColour,
                textColour = colors.onSecondary,
                additionalTextSpacing = 4.dp
            )

            ButtonEntries(
                buttonFunction = backUp::invoke,
                entryLabel = R.string.BackUp,
                entryIcon = R.drawable.server_01,
                dismiss = dismiss,
                animationDelay = 200,
                padding = menuPadding,
                iconBackgroundColour = iconBackgroundColour,
                textBackgroundColour = textBackgroundColour,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                buttonFunction = rateApp::invoke,
                entryLabel = R.string.RateApp,
                entryIcon = R.drawable.thumbs_up,
                dismiss = dismiss,
                animationDelay = 250,
                padding = menuPadding,
                iconBackgroundColour = iconBackgroundColour,
                textBackgroundColour = textBackgroundColour,
                textColour = colors.onSecondary
            )

            ButtonEntries(
                buttonFunction = help::invoke,
                entryLabel = R.string.Help,
                entryIcon = R.drawable.help_circle,
                dismiss = dismiss,
                animationDelay = 300,
                padding = menuPadding,
                iconBackgroundColour = iconBackgroundColour,
                textBackgroundColour = textBackgroundColour,
                textColour = colors.onSecondary
            )
        }
    }
}