package com.ensiie.justine.parcoursup.ui.map


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import org.maplibre.compose.map.MaplibreMap
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.FormationMap
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position

import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.span
import androidx.compose.ui.unit.em
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.toJson
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun MapScreen(
    onNavigateToFormations: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Compose-based MapLibre implementation (requires MapLibre Compose bindings).
    val first = formations.firstOrNull()
    val initialLat = first?.etabGps?.lat ?: 46.603354
    val initialLon = first?.etabGps?.lon ?: 1.888334

    val cameraState   = rememberCameraState (
        CameraPosition(target = Position(initialLat, initialLon), zoom = 6.0)
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
            if (lat.isFinite() && lon.isFinite()) {
                Feature(
                    geometry = Point(Position(latitude = lat, longitude = lon)),
                    properties = mapOf("id" to JsonPrimitive(f.id))
                )
            } else null
        }

        val fcJson = FeatureCollection(features).toJson()

        val source = rememberGeoJsonSource(
            GeoJsonData.JsonString(fcJson),
            GeoJsonOptions(cluster = true, clusterRadius = 32, clusterMaxZoom = 16, clusterMinPoints = 3)
        )

        CircleLayer(
            id = "clusters",
            source = source,
            filter = feature.has("point_count"),
            color = const(androidx.compose.ui.graphics.Color(50, 205, 5)),
            opacity = const(0.5f),
            radius =
            step(
                input = feature["point_count"].asNumber(),
                fallback = const(15.dp),
                25 to const(20.dp),
                100 to const(30.dp),
                500 to const(40.dp),
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
            textField = format(
                span(feature["point_count"].asString())
            ),
            textFont = const(listOf("Noto Sans Regular")),
            textColor = const(androidx.compose.ui.graphics.Color.White),
        )

        org.maplibre.compose.layers.SymbolLayer(
            id = "unclustered-marker",
            source = source,
            filter = !feature.has("point_count"),
            iconImage = image(
                value = painterResource(R.drawable.marker),
                size = DpSize(30.dp, 42.dp)
            ),
            iconAllowOverlap = const(true),
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
