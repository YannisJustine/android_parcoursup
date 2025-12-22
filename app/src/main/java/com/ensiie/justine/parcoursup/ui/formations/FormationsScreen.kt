package com.ensiie.justine.parcoursup.ui.formations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrAlertDialog
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrFilterChip
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrIconButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTag
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagSize
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextField
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FormationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filieres by viewModel.filieres.collectAsState()
    val contratsEtab by viewModel.contratsEtab.collectAsState()
    val selectedFilieres by viewModel.selectedFilieres.collectAsState()
    val selectedTypesEtab by viewModel.selectedTypesEtab.collectAsState()
    val filterSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var filterType by remember { mutableStateOf<FilterType?>(null) }


    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::searchFormations,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        FilterTagsRow(
            selectedFilieresCount = selectedFilieres.size,
            selectedContratsCount = selectedTypesEtab.size,
            onFiliereClick = { filterType = FilterType.FILIERE },
            onContratClick = { filterType = FilterType.CONTRAT },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (val state = uiState) {
            is FormationsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DsfrCircularProgressIndicator()
                }
            }

            is FormationsUiState.Success -> {
                val isLoadingMore by viewModel.isLoadingMore.collectAsState()
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val pullToRefreshState = rememberPullToRefreshState()

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = isRefreshing,
                            containerColor = DSFRTheme.colors.backgroundDefault,
                            color = DSFRTheme.colors.actionHigh,
                            state = pullToRefreshState
                        )
                    },
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(state.formations, key = { it.id }) { formation ->
                            FormationCard(
                                formation = formation,
                                onClick = { onNavigateToDetail(formation.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(formation) },
                                isFavorite = viewModel.isFavorite(formation.id)
                                    .collectAsState(initial = false).value
                            )
                        }

                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DsfrCircularProgressIndicator()
                                }
                            }
                        } else if (state.formations.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreFormations()
                                }
                            }
                        }
                    }
                }
            }

            is FormationsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    filterType?.let { type ->
        FilterModalBottomSheet(
            title = when (type) {
                FilterType.FILIERE -> stringResource(R.string.select_filiere)
                FilterType.CONTRAT -> stringResource(R.string.select_formation)
            },

            options = when (type) {
                FilterType.FILIERE -> filieres
                FilterType.CONTRAT -> contratsEtab
            },
            selectedOptions = when (type) {
                FilterType.FILIERE -> selectedFilieres
                FilterType.CONTRAT -> selectedTypesEtab
            },
            onDismissRequest = { filterType = null },
            sheetState = filterSheetState,
            onToggleOption = { option ->
                when (type) {
                    FilterType.FILIERE -> viewModel.toggleFiliere(option)
                    FilterType.CONTRAT -> viewModel.toggleContratEtab(option)
                }
            },
            onClearAll = {
                when (type) {
                    FilterType.FILIERE -> viewModel.clearFilieres()
                    FilterType.CONTRAT -> viewModel.clearContratsEtab()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DsfrTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher une formation...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        maxLines = 1,
        modifier = modifier,
    )
}

@Composable
fun FormationCard(
    formation: Formation,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean
) {
    DsfrCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DsfrText(
                    text = formation.etablissement
                        ?: stringResource(R.string.establishment_unknown),
                    textStyle = DsfrTextStyle.HeadingMd
                )
                DsfrText(
                    text = formation.formationDetailleeString
                        ?: formation.formationLibString
                        ?: stringResource(R.string.formation),
                    textStyle = DsfrTextStyle.BodySm,
                    color = DsfrTextColor.Mention
                )
                DsfrText(
                    text = "${formation.ville ?: ""} - ${formation.departement ?: ""}",
                    textStyle = DsfrTextStyle.BodyXs,
                    color = DsfrTextColor.Label
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    formation.capacite?.let {
                        DsfrTag(
                            stringResource(R.string.capacity) + ": $it",
                            type = DsfrTagType.Primary,
                            size = DsfrTagSize.Small
                        )
                    }
                    formation.tauxAcces?.let {
                        DsfrTag(
                            stringResource(R.string.access_format, it),
                            type = DsfrTagType.Primary,
                            size = DsfrTagSize.Small
                        )
                    }
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite
                    else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.remove_from_favorites
                        else R.string.add_to_favorites
                    ),
                    tint = if (isFavorite) DSFRTheme.colors.error
                    else DSFRTheme.colors.textDefault
                )
            }
        }
    }
}


@Composable
fun FilterTagsRow(
    selectedFilieresCount: Int,
    selectedContratsCount: Int,
    onFiliereClick: () -> Unit,
    onContratClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DsfrText(
            text = "Filtrer par :",
            textStyle = DsfrTextStyle.BodyMd,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        
        DsfrFilterChip(
            selected = selectedFilieresCount > 0,
            onClick = onFiliereClick,
            label = {
            if (selectedFilieresCount == 0) {
                Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                DsfrText("Filtrer par filière", textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                }
            } else {
                DsfrText("Filtrer par filière ($selectedFilieresCount)")
            }
            }
        )

        DsfrFilterChip(
            selected = selectedContratsCount > 0,
            onClick = onContratClick,
            label = {
            if (selectedContratsCount == 0) {
                Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                DsfrText("Filtrer par type", textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                }
            } else {
                DsfrText("Filtrer par type ($selectedContratsCount)")
            }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterModalBottomSheet(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onToggleOption: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val minHeight = configuration.screenHeightDp * 0.5f
    val maxHeight = configuration.screenHeightDp * 0.9f

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = DSFRTheme.colors.backgroundDefault
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp, max = maxHeight.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DsfrText(
                    text = title,
                    textStyle = DsfrTextStyle.HeadingMd
                )
                
                if (selectedOptions.isNotEmpty()) {
                    DsfrTextButton(onClick = onClearAll) {
                        Text("Tout effacer")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(options) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleOption(option) }
                            .padding(vertical = DSFRTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedOptions.contains(option),
                            onCheckedChange = { onToggleOption(option) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DSFRTheme.colors.actionHigh,
                                uncheckedColor = DSFRTheme.colors.textDefault
                            )
                        )
                        Spacer(modifier = Modifier.width(DSFRTheme.spacing.sm))
                        DsfrText(
                            text = option
                        )
                    }
                }
            }
        }
    }
}

enum class FilterType {
    FILIERE,
    CONTRAT
}
