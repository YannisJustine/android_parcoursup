package com.ensiie.justine.parcoursup.ui.map


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.FormationMap
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
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
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

@Composable
fun MapScreen(
    onNavigateToFormations: (String) -> Unit,
    viewModel: MapViewModel = koinViewModel()
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
                onMarkerClick = { recordId -> onNavigateToFormations(recordId) }
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

@Composable
fun MapLibre(
    formations: List<FormationMap>,
    onMarkerClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val markerIcon = painterResource(R.drawable.marker)
    val parisCoordinates = doubleArrayOf(48.856614, 2.3522219)
    val (centerLat, centerLon, initialZoom) = Triple(parisCoordinates[0], parisCoordinates[1], 10.0)

    val cameraState = rememberCameraState(
        CameraPosition(target = Position( centerLon, centerLat), zoom = initialZoom)
    )

    MaplibreMap(
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState ,
        styleState = rememberStyleState(),
        onMapClick = { point, screenPoint ->
            ClickResult.Pass
        },
        options =
            MapOptions(
                ornamentOptions = OrnamentOptions.AllDisabled,
                gestureOptions =
                    GestureOptions(
                        isTiltEnabled = true,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true,
                    ),
            ),
    ) {

        val features = formations.mapNotNull { f ->
            val lat = f.etabGps.lat
            val lon = f.etabGps.lon
                Feature(
                    geometry = Point(Position(lon, lat)),
                    properties = mapOf("id" to JsonPrimitive(f.id)))
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
                            val zoomLevel = (cluster.properties?.get("point_count") as? JsonPrimitive)?.content?.toIntOrNull()?.let {
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

        org.maplibre.compose.layers.SymbolLayer(
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
                        onMarkerClick(id)
                        return@SymbolLayer ClickResult.Consume
                    }
                }
                ClickResult.Pass
            }
        )
    }
}
