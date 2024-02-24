package note.notes.savenote.Composable.Components.AppBars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Composable.Components.CustomTextField.CustomTextField
import note.notes.savenote.Composable.Components.CustomTextField.TextFieldPlaceHolder
import note.notes.savenote.R
import note.notes.savenote.ViewModel.PrimaryViewModel
import note.notes.savenote.ViewModel.model.PrimaryUiState
import note.notes.savenote.ui.theme.UniversalFamily


@Composable
fun TopNavigationBarHome(
    primaryViewModel: PrimaryViewModel,
    primaryUiState: PrimaryUiState,
    startedScrolling: Boolean,
    moreOptions: () -> Unit,
    startSearch:() -> Unit,
    changeView: () -> Unit,
    endSearch:() -> Unit,
    focusRequester: FocusRequester,
    offset: IntOffset
) {

    Card(
        modifier = Modifier
            .absoluteOffset { offset }
            .height(61.dp),
        elevation = if (startedScrolling || primaryUiState.showSearchBar) 20.dp else 0.dp,
        backgroundColor = colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeNavigationHeader(
                    visibleHeader = primaryUiState.dropDown,
                    headerVisibility = !primaryUiState.showSearchBar
                )

                SearchBar(
                    primaryViewModel = primaryViewModel,
                    primaryUiState = primaryUiState,
                    endSearch = endSearch::invoke,
                    focusRequester = focusRequester
                )

                Row(
                    modifier = Modifier.wrapContentSize(Alignment.CenterEnd, true)
                ) {
                    NavigationButtons(
                        modifier = Modifier.width(44.dp),
                        moreOptions = startSearch::invoke,
                        icon = R.drawable.search_lg,
                        buttonVisibility = !primaryUiState.showSearchBar
                    )

                    Crossfade(
                        animationSpec = tween(300),
                        targetState = primaryUiState.layoutView,
                        label = ""
                    ) { targetAnimation ->
                        when (targetAnimation) {
                            true -> NavigationButtons(
                                moreOptions = changeView::invoke,
                                icon = R.drawable.rows_01,
                                enabled = !primaryUiState.dropDown
                            )

                            else -> NavigationButtons(
                                moreOptions = changeView::invoke,
                                icon = R.drawable.grid_01,
                                enabled = !primaryUiState.dropDown
                            )
                        }
                    }

                    NavigationButtons(
                        modifier = Modifier.width(30.dp),
                        moreOptions = moreOptions::invoke,
                        icon = R.drawable.dots_vertical,
                        iconScale = 2.dp,
                        enabled = !primaryUiState.showSearchBar
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    primaryViewModel: PrimaryViewModel,
    primaryUiState: PrimaryUiState,
    endSearch: () -> Unit,
    focusRequester: FocusRequester,
){
    AnimatedVisibility(
        visible = primaryUiState.showSearchBar,
        enter = slideInHorizontally(tween(durationMillis = 100)),
    ) {
        if(primaryUiState.showSearchBar){
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.onBackground,
                shape = RoundedCornerShape(15.dp),
                elevation = 0.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavigationButtons(
                        moreOptions = endSearch::invoke,
                        icon = R.drawable.x_close
                    )
                    CustomTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(end = 10.dp)
                            .fillMaxWidth(0.80f)
                            .animateContentSize(tween(150)),
                        value = primaryUiState.searchQuery,
                        searchIsActive = primaryUiState.showSearchBar,
                        onValueChange = { primaryViewModel.processSearchRequest(it) },
                        decorationBox = {
                            TextFieldPlaceHolder(
                                showPlaceHolder = primaryUiState.searchQuery.isEmpty(),
                                text = R.string.SearchNotes,
                                fontSize = 15.sp,
                                colour = colors.onSurface
                            )
                        },
                        singleLine = true,
                        fontSize = 15.sp,
                        textColour = colors.primaryVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderText(
    modifier: Modifier = Modifier,
    title: Any,
    color: Color = colors.onSecondary,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.SemiBold
){
    Text(
        modifier = modifier,
        text = if(title is Int) stringResource(id = title) else title as String,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = UniversalFamily,
        textAlign = TextAlign.Start

    )
}

@Composable
fun HomeNavigationHeader(
    visibleHeader: Boolean,
    headerVisibility: Boolean,
) {
    AnimatedVisibility(
        visible = headerVisibility,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(0))
    ) {
        Row (modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(start = 10.dp)){
            Crossfade(
                animationSpec = tween(durationMillis = 200),
                targetState = visibleHeader,
                label = ""
            ) { showHeader ->
                when (showHeader) {
                    true -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(id = R.drawable.menu_01),
                                contentDescription = stringResource(R.string.DeleteNote),
                                tint = colors.primary
                            )
                            HeaderText(
                                title = R.string.Menu,
                                color = colors.onSecondary,
                            )
                        }
                    }

                    else -> HeaderText(title = R.string.AppName)
                }
            }
        }
    }
}

@Composable
fun NavigationButtons(
    modifier: Modifier = Modifier,
    icon: Int,
    iconScale: Dp = 0.dp,
    moreOptions: () -> Unit,
    optionsColourEnabled: Color = colors.primary,
    optionsColourDisabled: Color = colors.secondary,
    enabled: Boolean = true,
    buttonVisibility: Boolean = true
){
    val buttonColour = if(enabled) optionsColourEnabled else optionsColourDisabled
    if(buttonVisibility){
        IconButton(
            modifier = modifier.width(35.dp),
            onClick = moreOptions::invoke,
            enabled = enabled
        ) {
            Icon(
                modifier = Modifier.size(22.dp + iconScale),
                painter = painterResource(id = icon),
                contentDescription = stringResource(R.string.DeleteNote),
                tint = buttonColour
            )
        }
    }
}