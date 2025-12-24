package com.ensiie.justine.parcoursup.ui.formations

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.City
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrAlertDialog
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrFilterChip
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrIconButton
import androidx.compose.material3.Icon
import androidx.room.util.copy
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButtonType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTag
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagSize
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextField
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import com.ensiie.justine.parcoursup.utils.LocationHelper
import com.ensiie.justine.parcoursup.utils.LocationFilter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
                                text = "Aucune formation trouvée.",
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

@OptIn(ExperimentalMaterial3Api::class)
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
                DsfrText("Filtrer par filière", textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(DSFRTheme.spacing.md)
                )
                }
            } else {
                DsfrText("Filtrer par filière ($selectedFilieresCount)", textStyle = DsfrTextStyle.BodySm)
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
                DsfrText("Filtrer par type", textStyle = DsfrTextStyle.BodySm)
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(DSFRTheme.spacing.md)
                )
                }
            } else {
                DsfrText("Filtrer par type ($selectedContratsCount)", textStyle = DsfrTextStyle.BodySm)
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
                            Text("Tout effacer")
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
                                    tempSelectedOptions = if (tempSelectedOptions.contains(option)) {
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
                    Text("Annuler")
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
                    Text("Valider")
                }
            }
        }
    }
}

enum class FilterType {
    FILIERE,
    CONTRAT
}

@Composable
fun LocationFilterCard(
    locationFilter: LocationFilter?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    DsfrCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (locationFilter != null) DSFRTheme.colors.actionHigh else DSFRTheme.colors.textDefault,
                    modifier = Modifier.size(20.dp)
                )
                if (locationFilter != null) {
                    Column {
                        DsfrText(
                            text = locationFilter.cityName ?: "Position actuelle",
                            textStyle = DsfrTextStyle.BodyMd,
                            fontWeight = FontWeight.Bold
                        )
                        DsfrText(
                            text = "Rayon : ${locationFilter.radiusKm} km",
                            textStyle = DsfrTextStyle.BodySm,
                            color = DsfrTextColor.Mention
                        )
                    }
                } else {
                    DsfrText(
                        text = "Filtrer par localisation",
                        textStyle = DsfrTextStyle.BodyMd
                    )
                }
            }
            if (locationFilter != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Retirer le filtre de localisation",
                        tint = DSFRTheme.colors.textDefault,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.add_circle_24dp),
                    contentDescription = null,
                    tint = DSFRTheme.colors.actionHigh,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchModal(
    onDismiss: () -> Unit,
    locationSheetState: SheetState,
    onLocationSelected: (City, Int) -> Unit,
    onUseCurrentLocation: (Int) -> Unit,
    searchCities: suspend (String) -> Result<List<City>>,
    isLoadingLocation: Boolean,
    locationError: String? = null,
    initialRadius: Int = 10,
    initialCity: City? = null
) {
    var searchQuery by remember { mutableStateOf(initialCity?.name ?: "") }
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var selectedRadius by remember { mutableStateOf(initialRadius) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<City?>(initialCity) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            kotlinx.coroutines.delay(400)
            if (searchQuery.length >= 2) {
                searchCities(searchQuery)
                    .onSuccess { cities = it }
                    .onFailure { cities = emptyList() }
            }
            isSearching = false
        } else {
            cities = emptyList()
        }
    }

    ModalBottomSheet(
        sheetState = locationSheetState,
        onDismissRequest = onDismiss,
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
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f),
                verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md)
            ) {
                DsfrText(
                    text = "Rechercher par localisation",
                    textStyle = DsfrTextStyle.HeadingMd
                )

                DsfrButton(
                    onClick = { onUseCurrentLocation(selectedRadius) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        DsfrCircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(DSFRTheme.spacing.sm))
                    DsfrText("Utiliser ma position")
                }

                if (locationError != null) {
                    DsfrCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = DSFRTheme.colors.error,
                                modifier = Modifier.size(20.dp)
                            )
                            DsfrText(
                                text = locationError,
                                textStyle = DsfrTextStyle.BodySm,
                                color = DsfrTextColor.Error
                            )
                        }
                    }
                }

                DsfrTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher une ville...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DsfrText(
                    text = "Rayon de recherche",
                    textStyle = DsfrTextStyle.BodyMd,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                ) {
                    listOf(5, 10, 25, 50, 100).forEach { radius ->
                        DsfrFilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = { DsfrText("$radius km", textStyle = DsfrTextStyle.BodySm) },
                        )
                    }
                }

                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(DSFRTheme.spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        DsfrCircularProgressIndicator()
                    }
                } else if (cities.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm),
                        state = rememberLazyListState()
                    ) {
                        items(cities) { city ->
                            DsfrCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCity = city
                                    }
                                    .then(if (selectedCity == city) Modifier.border(2.dp, DSFRTheme.colors.actionHigh) else Modifier),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    DsfrText(
                                        text = city.name,
                                        textStyle = DsfrTextStyle.BodyMd,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DsfrText(
                                        text = city.context,
                                        textStyle = DsfrTextStyle.BodySm,
                                        color = DsfrTextColor.Mention
                                    )
                                }
                            }
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
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = DSFRTheme.spacing.sm),
                    type = DsfrButtonType.Secondary
                ) {
                    Text("Annuler")
                }
                DsfrButton(
                    onClick = {
                        if (selectedCity != null) {
                            onLocationSelected(selectedCity!!, selectedRadius)
                        }
                    },
                    enabled = selectedCity != null
                ) {
                    Text("Valider")
                }
            }
        }
    }
}