package com.ensiie.justine.parcoursup.ui.filters

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButton
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrButtonType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrFilterChip
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextField
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import com.ensiie.justine.parcoursup.utils.LocationFilter
import com.ensiie.justine.parcoursup.utils.LocationHelper

/**
 * State holder for location search.
 */
class LocationSearchState(
    initialLocation: Pair<Double, Double>? = null
) {
    var savedCurrentLocation by mutableStateOf(initialLocation)
}

/**
 * Remember the location search state across configuration changes and process death.
 */
@Composable
fun rememberLocationSearchState(): LocationSearchState {
    val state = remember { LocationSearchState() }

    return state
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
                            text = locationFilter.cityName
                                ?: stringResource(R.string.current_location),
                            textStyle = DsfrTextStyle.BodyMd,
                            fontWeight = FontWeight.Bold
                        )
                        DsfrText(
                            text = stringResource(R.string.radius_km, locationFilter.radiusKm),
                            textStyle = DsfrTextStyle.BodySm,
                            color = DsfrTextColor.Mention
                        )
                    }
                } else {
                    DsfrText(
                        text = stringResource(R.string.filter_by_location),
                        textStyle = DsfrTextStyle.BodyMd
                    )
                }
            }
            if (locationFilter != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_location_filter),
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
    onCurrentLocationSelected: (Double, Double, Int) -> Unit,
    searchCities: suspend (String) -> Result<List<City>>,
    locationHelper: LocationHelper,
    sharedFilterViewModel: SharedFilterViewModel,
    initialRadius: Int = 10,
    initialCity: City? = null
) {
    val savedCurrentLocation by sharedFilterViewModel.pendingCurrentLocation.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(initialCity?.name ?: "") }
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var selectedRadius by remember { mutableStateOf(initialRadius) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<City?>(initialCity) }
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
                    sharedFilterViewModel.setPendingCurrentLocation(Pair(latitude, longitude))
                    isLoadingLocation = false
                },
                onGetCurrentLocationFailed = { exception ->
                    isLoadingLocation = false
                    locationError =
                        exception.message ?: "Erreur lors de la récupération de votre position"
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

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            kotlinx.coroutines.delay(400)
            searchCities(searchQuery)
                .onSuccess { cities = it }
                .onFailure { cities = emptyList() }
            isSearching = false
        } else {
            cities = emptyList()
        }
    }

    ModalBottomSheet(
        sheetState = locationSheetState,
        onDismissRequest = onDismiss,
        containerColor = DSFRTheme.colors.backgroundDefault,
        modifier = Modifier.fillMaxWidth(),
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
                    onClick = {
                        if (savedCurrentLocation != null) {
                            sharedFilterViewModel.clearPendingCurrentLocation()
                            locationError = null
                        } else if (locationHelper.areLocationPermissionsGranted()) {
                            isLoadingLocation = true
                            locationError = null
                            locationHelper.getCurrentLocation(
                                onGetCurrentLocationSuccess = { (latitude, longitude) ->
                                    sharedFilterViewModel.setPendingCurrentLocation(
                                        Pair(
                                            latitude,
                                            longitude
                                        )
                                    )
                                    isLoadingLocation = false
                                },
                                onGetCurrentLocationFailed = { exception ->
                                    isLoadingLocation = false
                                    locationError = exception.message
                                        ?: "Erreur lors de la récupération de votre position"
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
                    DsfrText(
                        savedCurrentLocation?.let {
                            stringResource(R.string.remove_current_location)
                        } ?: stringResource(R.string.get_current_location)
                    )
                }

                locationError?.let {
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
                                text = it,
                                textStyle = DsfrTextStyle.BodySm,
                                color = DsfrTextColor.Error
                            )
                        }
                    }
                }

                if (savedCurrentLocation == null) {
                    DsfrTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search_city_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DsfrText(
                    text = stringResource(R.string.search_radius),
                    textStyle = DsfrTextStyle.BodyMd,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DSFRTheme.spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        DsfrCircularProgressIndicator()
                    }
                } else if (cities.isNotEmpty() && savedCurrentLocation == null) {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
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
                                    .then(
                                        if (selectedCity == city) Modifier.border(
                                            2.dp,
                                            DSFRTheme.colors.actionHigh
                                        ) else Modifier
                                    ),
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
                    Text(stringResource(R.string.cancel))
                }
                DsfrButton(
                    onClick = {
                        if (selectedCity != null) {
                            onLocationSelected(selectedCity!!, selectedRadius)
                        } else {
                            savedCurrentLocation?.let { (latitude, longitude) ->
                                onCurrentLocationSelected(latitude, longitude, selectedRadius)
                            }
                        }
                    },
                    enabled = selectedCity != null || savedCurrentLocation != null
                ) {
                    Text(stringResource(R.string.validate))
                }
            }
        }
    }
}