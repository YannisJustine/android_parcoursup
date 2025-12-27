package com.ensiie.justine.parcoursup.ui.formations

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.City
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.ui.filters.LocationFilterCard
import com.ensiie.justine.parcoursup.ui.filters.LocationSearchModal
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButtonType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrFilterChip
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTag
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagSize
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextField
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import com.ensiie.justine.parcoursup.utils.LocationFilter
import com.ensiie.justine.parcoursup.utils.LocationHelper
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FormationsViewModel = koinViewModel(),
    adresseRepository: AdresseRepository = koinInject(),
    locationHelper: LocationHelper = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filieres by viewModel.filieres.collectAsState()
    val contratsEtab by viewModel.contratsEtab.collectAsState()
    val selectedFilieres by viewModel.selectedFilieres.collectAsState()
    val selectedTypesEtab by viewModel.selectedTypesEtab.collectAsState()
    val locationFilter by viewModel.locationFilter.collectAsState()
    val filterSheetState = rememberModalBottomSheetState()
    val locationSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var filterType by remember { mutableStateOf<FilterType?>(null) }
    var showLocationModal by remember { mutableStateOf(false) }
    val context = LocalContext.current


    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::searchFormations,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSFRTheme.spacing.md, vertical = DSFRTheme.spacing.sm)
        )

        LocationFilterCard(
            locationFilter = locationFilter,
            onClick = { showLocationModal = true },
            onClear = { viewModel.clearLocationFilter() },
            modifier = Modifier.padding(horizontal = DSFRTheme.spacing.md, vertical = 4.dp)
        )

        FilterTagsRow(
            selectedFilieresCount = selectedFilieres.size,
            selectedContratsCount = selectedTypesEtab.size,
            onFiliereClick = { filterType = FilterType.FILIERE },
            onContratClick = { filterType = FilterType.CONTRAT },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSFRTheme.spacing.md, vertical = DSFRTheme.spacing.sm)
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
                val listState = rememberLazyListState()

                LaunchedEffect(Unit) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                        .map { visibleItems ->
                            val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                            val totalItems = listState.layoutInfo.totalItemsCount
                            Pair(lastVisibleItemIndex, totalItems)
                        }
                        .collect { (lastVisibleItemIndex, totalItems) ->
                            if (state.formations.isEmpty() || isLoadingMore) {
                                return@collect
                            }

                            if (lastVisibleItemIndex >= totalItems - 1) {
                                viewModel.loadMoreFormations()
                            }
                        }
                }

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
                    }
                ) {
                    if (state.formations.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DsfrText(
                                text = stringResource(R.string.no_formations_found),
                                textStyle = DsfrTextStyle.BodyMd,
                                color = DsfrTextColor.Label
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(DSFRTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm),
                            state = listState
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
                                            .padding(DSFRTheme.spacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DsfrCircularProgressIndicator()
                                    }
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

    if (showLocationModal) {
        var isLoadingLocation by remember { mutableStateOf(false) }
        var locationError by remember { mutableStateOf<String?>(null) }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                isLoadingLocation = true
                locationError = null
                
                locationHelper.getCurrentLocation(
                    onGetCurrentLocationSuccess = { (latitude, longitude) ->
                        viewModel.setLocationFilter(
                            LocationFilter(
                                latitude = latitude,
                                longitude = longitude,
                                radiusKm = 10,
                                cityName = null
                            )
                        )
                        showLocationModal = false
                        isLoadingLocation = false
                    },
                    onGetCurrentLocationFailed = { exception ->
                        isLoadingLocation = false
                        locationError = exception.message ?: "Erreur lors de la récupération de votre position"
                    }
                )
            } else {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        }

        LocationSearchModal(
            locationSheetState = locationSheetState,
            onDismiss = {
                showLocationModal = false
                locationError = null
            },
            onLocationSelected = { selectedCity, radius ->
                viewModel.setLocationFilter(
                    LocationFilter(
                        latitude = selectedCity.latitude,
                        longitude = selectedCity.longitude,
                        radiusKm = radius,
                        cityName = selectedCity.name
                    )
                )
                showLocationModal = false
            },
            onUseCurrentLocation = { radius ->
                if (locationHelper.areLocationPermissionsGranted()) {
                    isLoadingLocation = true
                    locationError = null
                    locationHelper.getCurrentLocation(
                        onGetCurrentLocationSuccess = { (latitude, longitude) ->
                            viewModel.setLocationFilter(
                                LocationFilter(
                                    latitude = latitude,
                                    longitude = longitude,
                                    radiusKm = radius,
                                    cityName = null
                                )
                            )
                            showLocationModal = false
                            isLoadingLocation = false
                        },
                        onGetCurrentLocationFailed = { exception ->
                            isLoadingLocation = false
                            locationError = exception.message ?: "Erreur lors de la récupération de votre position"
                        }
                    )
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            searchCities = { query -> adresseRepository.searchCities(query) },
            isLoadingLocation = isLoadingLocation,
            locationError = locationError,
            initialRadius = locationFilter?.radiusKm ?: 10,
            initialCity = locationFilter?.let {
                if (it.cityName != null) {
                    City(
                        name = it.cityName,
                        fullLabel = it.cityName,
                        longitude = it.longitude,
                        latitude = it.latitude,
                        context = ""
                    )
                } else null
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(query) }

    LaunchedEffect(text) {
        if (text != query) {
            kotlinx.coroutines.delay(400)
            onQueryChange(text)
        }
    }

    DsfrTextField(
        value = text,
        onValueChange = { text = it },
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
                .padding(DSFRTheme.spacing.md),
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

                Spacer(modifier = Modifier.height(DSFRTheme.spacing.sm))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md)
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
        horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
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
                horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                ) {
                DsfrText(stringResource(R.string.filter_by_filiere), textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(DSFRTheme.spacing.md)
                )
                }
            } else {
                DsfrText(stringResource(R.string.filter_by_filiere_count, selectedFilieresCount), textStyle = DsfrTextStyle.BodySm)
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
                horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                ) {
                DsfrText(stringResource(R.string.filter_by_type), textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(DSFRTheme.spacing.md)
                )
                }
            } else {
                DsfrText(stringResource(R.string.filter_by_type_count, selectedContratsCount), textStyle = DsfrTextStyle.BodySm)
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
    var tempSelectedOptions by remember { mutableStateOf(selectedOptions) }
    
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = DSFRTheme.colors.backgroundDefault,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSFRTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DSFRTheme.spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DsfrText(
                        text = title,
                        textStyle = DsfrTextStyle.HeadingMd
                    )
                    
                    if (tempSelectedOptions.isNotEmpty()) {
                        DsfrTextButton(onClick = { tempSelectedOptions = emptySet() }) {
                            Text(stringResource(R.string.clear_all))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                ) {
                    items(options) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelectedOptions =
                                        if (tempSelectedOptions.contains(option)) {
                                            tempSelectedOptions - option
                                        } else {
                                            tempSelectedOptions + option
                                        }
                                }
                                .padding(vertical = DSFRTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tempSelectedOptions.contains(option),
                                onCheckedChange = { 
                                    tempSelectedOptions = if (tempSelectedOptions.contains(option)) {
                                        tempSelectedOptions - option
                                    } else {
                                        tempSelectedOptions + option
                                    }
                                },
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
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DSFRTheme.spacing.md),
                horizontalArrangement = Arrangement.End
            ) {
                DsfrButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.padding(end = DSFRTheme.spacing.sm),
                    type = DsfrButtonType.Secondary
                ) {
                    Text(stringResource(R.string.cancel))
                }
                DsfrButton(
                    onClick = {
                        val toRemove = selectedOptions - tempSelectedOptions
                        val toAdd = tempSelectedOptions - selectedOptions
                        
                        toRemove.forEach { onToggleOption(it) }
                        toAdd.forEach { onToggleOption(it) }
                        
                        if (tempSelectedOptions.isEmpty() && selectedOptions.isNotEmpty()) {
                            onClearAll()
                        }
                        
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.validate))
                }
            }
        }
    }
}

enum class FilterType {
    FILIERE,
    CONTRAT
}
