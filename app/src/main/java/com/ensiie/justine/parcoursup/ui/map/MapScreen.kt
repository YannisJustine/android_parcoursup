package com.ensiie.justine.parcoursup.ui.map


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.City
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.ui.filters.LocationFilterCard
import com.ensiie.justine.parcoursup.ui.filters.LocationSearchModal
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import com.ensiie.justine.parcoursup.utils.LocationFilter
import com.ensiie.justine.parcoursup.utils.LocationHelper
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.formatToString
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

@Composable
fun MapScreen(
    onNavigateToFormations: (String) -> Unit,
    viewModel: MapViewModel = koinViewModel(),
    adresseRepository: AdresseRepository = koinInject(),
    locationHelper: LocationHelper = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is MapUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        is MapUiState.Success -> {
            MapLibre(
                formations = state.formations,
                onMarkerClick = { recordId -> onNavigateToFormations(recordId) },
                viewModel = viewModel,
                locationHelper = locationHelper,
                adresseRepository = adresseRepository
            )
        }

        is MapUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.retry() }) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLibre(
    formations: List<FormationMap>,
    onMarkerClick: (String) -> Unit,
    viewModel: MapViewModel,
    locationHelper: LocationHelper,
    adresseRepository: AdresseRepository
) {
    val coroutineScope = rememberCoroutineScope()
    LocalContext.current

    val locationFilter by viewModel.locationFilter.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val formationsAtClickedLocation by viewModel.clickedFormations.collectAsState()

    val locationSheetState = rememberModalBottomSheetState()
    val formationSelectionSheetState = rememberModalBottomSheetState()
    var showLocationModal by remember { mutableStateOf(false) }

    // Group formations by coordinates to detect multiple formations at same location
    val formationsByLocation = remember(formations) {
        formations.groupBy { "${it.etabGps.lat},${it.etabGps.lon}" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            formations = formations,
            locationFilter = locationFilter,
            formationsByLocation = formationsByLocation,
            onSingleMarkerClick = onMarkerClick,
            onMultipleFormationsClick = { formationList ->
                viewModel.setClickedFormations(formationList)
            }
        )

        LocationFilterCard(
            locationFilter = locationFilter,
            currentLocation = currentLocation,
            onClick = { showLocationModal = true },
            onClear = { viewModel.clearLocationFilter() },
            modifier = Modifier.padding(horizontal = DSFRTheme.spacing.md, vertical = 4.dp)
        )

        if (showLocationModal) {
            LocationSearchModal(
                locationSheetState = locationSheetState,
                onDismiss = { showLocationModal = false },
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
                onCurrentLocationSelected = { latitude, longitude, radius ->
                    viewModel.setLocationFilter(
                        LocationFilter(
                            latitude = latitude,
                            longitude = longitude,
                            radiusKm = radius,
                            cityName = null
                        )
                    )
                    showLocationModal = false
                },
                onCurrentLocationUpdate = { latitude, longitude ->
                    viewModel.setCurrentLocation(latitude, longitude)
                },
                searchCities = { query -> adresseRepository.searchCities(query) },
                locationHelper = locationHelper,
                currentLocation = currentLocation,
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

        FormationSelectionBottomSheet(
            formations = formationsAtClickedLocation,
            sheetState = formationSelectionSheetState,
            onDismiss = { viewModel.setClickedFormations(null) },
            onFormationClick = { formation ->
                coroutineScope.launch {
                    formationSelectionSheetState.hide()
                }.invokeOnCompletion(
                    {
                        onMarkerClick(formation.id)
                    }
                )

            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormationSelectionBottomSheet(
    formations: List<FormationMap>?,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onFormationClick: (FormationMap) -> Unit
) {
    formations?.let { formationList ->
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = DSFRTheme.colors.backgroundDefault,
            onDismissRequest = onDismiss,
            sheetGesturesEnabled = true
        ) {
            FormationListContent(
                formations = formationList,
                onFormationClick = onFormationClick
            )
        }
    }
}

@Composable
private fun FormationListContent(
    formations: List<FormationMap>,
    onFormationClick: (FormationMap) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
            .padding(
                horizontal = DSFRTheme.spacing.lg,
                vertical = DSFRTheme.spacing.md
            )
    ) {
        DsfrText(
            text = "Formations à cet emplacement (${formations.size})",
            textStyle = DsfrTextStyle.DisplayXs,
            color = DsfrTextColor.Title,
            modifier = Modifier.padding(bottom = DSFRTheme.spacing.lg)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md),
        ) {
            items(formations) { formation ->
                FormationItem(
                    formation = formation,
                    onClick = { onFormationClick(formation) },
                    showDivider = formation != formations.last()
                )
            }
        }
    }
}

@Composable
private fun FormationItem(
    formation: FormationMap,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DSFRTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                DsfrText(
                    text = formation.etablissement ?: "Établissement inconnu",
                    textStyle = DsfrTextStyle.BodyMd,
                    color = DsfrTextColor.Title
                )
                DsfrText(
                    text = formation.filiDetailString ?: "Formation inconnue",
                    textStyle = DsfrTextStyle.BodyMd,
                    color = DsfrTextColor.Mention,
                    modifier = Modifier.padding(top = DSFRTheme.spacing.xs)
                )
            }

            Spacer(modifier = Modifier.width(DSFRTheme.spacing.sm))

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Voir la formation",
                    tint = DSFRTheme.colors.actionHigh
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DSFRTheme.spacing.sm),
                color = DSFRTheme.colors.borderDefault
            )
        }
    }
}

@Composable
private fun MapContent(
    formations: List<FormationMap>,
    locationFilter: LocationFilter?,
    formationsByLocation: Map<String, List<FormationMap>>,
    onSingleMarkerClick: (String) -> Unit,
    onMultipleFormationsClick: (List<FormationMap>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val markerIcon = painterResource(R.drawable.marker)
    val (centerLat, centerLon, initialZoom) =
        if (locationFilter == null) {
            Triple(48.856614, 2.3522219, 10.0)
        } else {
            Triple(
                locationFilter.latitude,
                locationFilter.longitude,
                12.0
            )
        }

    val cameraState = rememberCameraState(
        CameraPosition(target = Position(centerLon, centerLat), zoom = initialZoom)
    )

    MaplibreMap(
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState,
        styleState = rememberStyleState(),
        onMapClick = { point, screenPoint ->
            ClickResult.Pass
        },
        options = MapOptions(
            ornamentOptions = OrnamentOptions(
                isLogoEnabled = true,
                isAttributionEnabled = true,
                isCompassEnabled = false,
                isScaleBarEnabled = false
            ),
            gestureOptions = GestureOptions(
                isTiltEnabled = true,
                isZoomEnabled = true,
                isRotateEnabled = true,
                isScrollEnabled = true,
            ),
        ),
    ) {
        val features = formations.map { f ->
            val lat = f.etabGps.lat
            val lon = f.etabGps.lon
            Feature(
                geometry = Point(Position(lon, lat)),
                properties = mapOf("id" to JsonPrimitive(f.id))
            )
        }

        val fcJson = FeatureCollection(features).toJson()

        val source = rememberGeoJsonSource(
            GeoJsonData.JsonString(fcJson),
            GeoJsonOptions(
                cluster = true,
                clusterRadius = 50,
                clusterMaxZoom = 14
            )
        )

        CircleLayer(
            id = "clusters",
            source = source,
            filter = feature.has("point_count"),
            color = step(
                input = feature["point_count"].asNumber(),
                fallback = const(androidx.compose.ui.graphics.Color(0xFF51BBD6)),
                100 to const(androidx.compose.ui.graphics.Color(0xFF1E3A8A)),
                750 to const(androidx.compose.ui.graphics.Color(0xFFF28CB1))
            ),
            radius = step(
                input = feature["point_count"].asNumber(),
                fallback = const(20.dp),
                100 to const(30.dp),
                750 to const(40.dp)
            ),
            onClick = { feats ->
                feats
                    .firstOrNull { it.properties?.get("point_count") != null }
                    ?.let { cluster ->
                        coroutineScope.launch {
                            val zoomLevel =
                                (cluster.properties?.get("point_count") as? JsonPrimitive)?.content?.toIntOrNull()
                                    ?.let {
                                        (cameraState.position.zoom + 2.0).coerceAtMost(20.0)
                                    } ?: (cameraState.position.zoom + 2.0)
                            cameraState.animateTo(
                                cameraState.position.copy(
                                    target = (cluster.geometry as Point).coordinates,
                                    zoom = zoomLevel,
                                )
                            )
                        }
                        ClickResult.Consume
                    } ?: ClickResult.Pass
            }
        )

        SymbolLayer(
            id = "cluster-count",
            source = source,
            filter = feature.has("point_count"),
            textField = feature["point_count"].asNumber().formatToString(
                minFractionDigits = 0,
                maxFractionDigits = 0
            ),
            textFont = const(listOf("Noto Sans Regular")),
            textColor = const(MaterialTheme.colorScheme.onBackground),
        )

        SymbolLayer(
            id = "unclustered-marker",
            source = source,
            filter = !feature.has("point_count"),
            iconImage = image(markerIcon),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconAnchor = const(SymbolAnchor.Bottom),
            onClick = { feats ->
                feats.firstOrNull()?.let { f ->
                    val id = (f.properties?.get("id") as? JsonPrimitive)?.content
                    if (id != null) {
                        val formation = formations.find { it.id == id }
                        formation?.let { clickedFormation ->
                            val locationKey =
                                "${clickedFormation.etabGps.lat},${clickedFormation.etabGps.lon}"
                            val formationsAtLocation =
                                formationsByLocation[locationKey] ?: listOf(clickedFormation)

                            if (formationsAtLocation.size > 1) {
                                onMultipleFormationsClick(formationsAtLocation)
                            } else {
                                onSingleMarkerClick(id)
                            }
                        }
                        return@SymbolLayer ClickResult.Consume
                    }
                }
                ClickResult.Pass
            }
        )

        // Location radius circle
        if (locationFilter != null) {
            val circlePolygon = remember(locationFilter) {
                createCirclePolygon(
                    centerLat = locationFilter.latitude,
                    centerLon = locationFilter.longitude,
                    radiusKm = locationFilter.radiusKm.toDouble(),
                    steps = 64
                )
            }

            val circleFeature = Feature(
                geometry = circlePolygon,
                properties = mapOf("type" to JsonPrimitive("radius-circle"))
            )
            val circleJson = FeatureCollection(listOf(circleFeature)).toJson()
            val circleSource = rememberGeoJsonSource(
                GeoJsonData.JsonString(circleJson)
            )

            FillLayer(
                id = "location-radius-fill",
                source = circleSource,
                color = const(DSFRTheme.colors.backgroundActionHigh.copy(alpha = 0.3f)),
            )

            LineLayer(
                id = "location-radius-outline",
                source = circleSource,
                color = const(DSFRTheme.colors.backgroundActionHigh),
                width = const(3.dp),
            )
        }
    }
}

/**
 * Creates a circle polygon similar to turf.circle from Turf.js
 *
 * @param centerLat Latitude of the circle center
 * @param centerLon Longitude of the circle center
 * @param radiusKm Radius in kilometers
 * @param steps Number of points to create the circle (default 64 for smooth circle)
 * @return Polygon representing the circle
 *
 * This function creates a geographically accurate circle that maintains its size
 * regardless of zoom level, unlike pixel-based circles.
 */
private fun createCirclePolygon(
    centerLat: Double,
    centerLon: Double,
    radiusKm: Double,
    steps: Int = 64
): Polygon {
    val earthRadiusKm = 6371.0 // Earth's radius in kilometers
    val points = mutableListOf<Position>()

    val radiusRad = radiusKm / earthRadiusKm

    val centerLatRad = Math.toRadians(centerLat)
    val centerLonRad = Math.toRadians(centerLon)

    for (i in 0 until steps) {
        val angle = (i.toDouble() / steps) * 2.0 * Math.PI

        val pointLatRad = kotlin.math.asin(
            kotlin.math.sin(centerLatRad) * kotlin.math.cos(radiusRad) +
                    kotlin.math.cos(centerLatRad) * kotlin.math.sin(radiusRad) * kotlin.math.cos(
                angle
            )
        )

        val pointLonRad = centerLonRad + kotlin.math.atan2(
            kotlin.math.sin(angle) * kotlin.math.sin(radiusRad) * kotlin.math.cos(centerLatRad),
            kotlin.math.cos(radiusRad) - kotlin.math.sin(centerLatRad) * kotlin.math.sin(pointLatRad)
        )

        val pointLat = Math.toDegrees(pointLatRad)
        val pointLon = Math.toDegrees(pointLonRad)

        points.add(Position(pointLon, pointLat))
    }

    if (points.isNotEmpty()) {
        points.add(points.first())
    }

    return Polygon(listOf(points))
}