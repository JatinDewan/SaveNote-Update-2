package note.notes.savenote.Composables.Components.AppBars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Composables.Components.CustomTextField
import note.notes.savenote.Composables.Components.TextFieldPlaceHolder
import note.notes.savenote.R
import note.notes.savenote.ViewModelClasses.PrimaryUiState
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun TopNavigationBarHome(
    startedScrolling: Boolean,
    animateBarOffset: Boolean,
    isMenuOpen: Boolean,
    startSearch:() -> Unit,
    endSearch:() -> Unit,
    moreOptions: () -> Unit,
    changeView: () -> Unit,
    offset: IntOffset,
    focusRequester: FocusRequester,
    primaryViewModel: PrimaryViewModel
) {
    val primaryUiState by primaryViewModel._uiState.collectAsState()
    val barAnimation by animateIntOffsetAsState(targetValue = offset, label = "")
    val animateDividerColour by animateColorAsState(
        targetValue = if(startedScrolling || isMenuOpen) colors.onBackground else colors.background,
        animationSpec = tween(150),
        label = ""
    )
    val optionsOpenColor: Color by animateColorAsState(
        targetValue = if(primaryUiState.dropDown) colors.onSecondary else colors.primary,
        animationSpec = tween(150),
        label = ""
    )

    Card(
        modifier = Modifier
            .absoluteOffset { if (animateBarOffset) barAnimation else offset }
            .height(60.dp)
            .fillMaxWidth(),
        elevation = if (startedScrolling) 10.dp else 0.dp,
        backgroundColor = colors.background,
        shape = RoundedCornerShape(0)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = animateDividerColour)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 5.dp),
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
                    startSearch = startSearch::invoke,
                    endSearch = endSearch::invoke,
                    focusRequester = focusRequester,
                    enabled = !primaryUiState.dropDown
                )

                Row(
                    modifier = Modifier.wrapContentSize(Alignment.CenterEnd, true)
                ) {
                    Crossfade(
                        animationSpec = tween(300),
                        targetState = primaryUiState.currentPage,
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
                        optionsColourEnabled = optionsOpenColor,
                        icon = R.drawable.dots_vertical,
                        iconScale = 2.dp,
                        enabled = !primaryUiState.showSearchBar
                    )
                }
            }
            Divider(color = animateDividerColour)
        }
    }
}

@Composable
fun HomeNavigationHeader(
    visibleHeader: Boolean,
    headerVisibility: Boolean
) {

    @Composable
    fun headerText(modifier: Modifier = Modifier, title: Int, color: Color = colors.onSecondary){
        Text(
            modifier = modifier,
            text = stringResource(id = title),
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = UniversalFamily,
            textAlign = TextAlign.Start

        )
    }

    AnimatedVisibility(
        visible = headerVisibility,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(0))
    ) {
        Row (modifier = Modifier.fillMaxWidth(0.7f).padding(start = 10.dp)){
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
                            headerText(
                                title = R.string.Menu,
                                color = colors.onSecondary,
                            )
                        }
                    }

                    else -> headerText(title = R.string.AppName)
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    primaryViewModel: PrimaryViewModel,
    primaryUiState: PrimaryUiState,
    startSearch: () -> Unit,
    endSearch: () -> Unit,
    focusRequester: FocusRequester,
    enabled: Boolean
){

    Card(
        modifier = Modifier.padding(start = 5.dp),
        backgroundColor = if(primaryUiState.showSearchBar) colors.onBackground else colors.background,
        shape = RoundedCornerShape(15.dp),
        elevation = 0.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            when(primaryUiState.showSearchBar){
                true -> {
                    NavigationButtons(
                        moreOptions = endSearch::invoke,
                        icon = R.drawable.x_close
                    )
                }
                else -> {
                    NavigationButtons(
                        moreOptions = startSearch::invoke,
                        icon = R.drawable.search_lg,
                        enabled = enabled
                    )
                }
            }

            CustomTextField(
                modifier = Modifier
                    .imePadding()
                    .focusRequester(focusRequester)
                    .padding(end = 10.dp)
                    .fillMaxWidth(0.75f)
                    .animateContentSize(tween(150)),
                value = primaryUiState.searchQuery,
                searchIsActive = primaryUiState.showSearchBar,
                onValueChange = { primaryViewModel.searchQuery(it) },
                decorationBox = {
                    TextFieldPlaceHolder(
                        showPlaceHolder = primaryUiState.showSearchBar && primaryUiState.searchQuery.isEmpty() ,
                        text = R.string.SearchNotes,
                        fontSize = 15.sp
                    )
                },
                singleLine = true,
                fontSize = 15.sp
            )
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
    enabled: Boolean = true
){
    val buttonColour = if(enabled) optionsColourEnabled else optionsColourDisabled
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