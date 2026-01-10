package com.ensiie.justine.parcoursup.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import com.ensiie.justine.parcoursup.ui.formations.FormationCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTag
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagSize
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is FavoritesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DsfrCircularProgressIndicator()
                }
            }

            is FavoritesUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        DsfrText(
                            text = stringResource(R.string.no_favorites),
                            textStyle = DsfrTextStyle.HeadingMd
                        )
                        DsfrText(
                            text = stringResource(R.string.add_formations_to_favorites),
                            textStyle = DsfrTextStyle.BodyMd
                        )
                    }
                }
            }

            is FavoritesUiState.Success -> {
                val listState = rememberLazyListState()
                val showScrollToTop by remember {
                    snapshotFlow { listState.firstVisibleItemIndex > 2 }
                }.collectAsState(initial = false)
                val coroutineScope = rememberCoroutineScope()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState
                ) {
                    item {
                        DsfrText(
                            text = stringResource(R.string.my_favorites, state.favorites.size),
                            textStyle = DsfrTextStyle.HeadingLg,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.favorites, key = { it.id }) { favorite ->
                        FormationCard(
                            formation = favorite,
                            isFavorite = true,
                            onClick = { onNavigateToDetail(favorite.id) },
                            onFavoriteClick = { viewModel.removeFavorite(favorite.id) }
                        )
                    }
                }

                if (showScrollToTop) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        containerColor = DSFRTheme.colors.actionHigh,
                        contentColor = DSFRTheme.colors.backgroundDefault,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.scroll_to_top)
                        )
                    }
                }
            }
        }
    }
}