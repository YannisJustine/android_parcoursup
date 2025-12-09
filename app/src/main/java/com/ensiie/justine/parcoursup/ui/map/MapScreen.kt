package com.ensiie.justine.parcoursup.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.osmdroid.compose.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

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
            ) {
                CircularProgressIndicator()
            }
        }
        
        is MapUiState.Success -> {
            OsmDroidMap(
                formations = state.formations,
                onMarkerClick = { recordId ->
                    onNavigateToFormations(recordId)
                }
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
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.retry() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
fun OsmDroidMap(
    formations: List<FormationMap>,
    onMarkerClick: (String) -> Unit
) {
    var savedZoom by remember { mutableDoubleStateOf(6.0) }
    var savedCenter by remember { mutableStateOf(GeoPoint(46.603354, 1.888334)) }
    var isInitialized by remember { mutableStateOf(false) }
    
    MapView(
        modifier = Modifier.fillMaxSize(),
        onInitMapView = { mapView ->
            Configuration.getInstance().userAgentValue = "ParcoursupApp"
            
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            
            if (!isInitialized) {
                mapView.controller.setZoom(savedZoom)
                mapView.controller.setCenter(savedCenter)
                isInitialized = true
            }
        },
        onUpdateMapView = { mapView ->
            savedZoom = mapView.zoomLevelDouble
            savedCenter = mapView.mapCenter as GeoPoint
            
            mapView.overlays.clear()
            

            formations.forEach { formation ->
                val lat = formation.etabGps.lat
                val lon = formation.etabGps.lon
                
                val marker = Marker(mapView).apply {
                    position = GeoPoint(lat, lon)
                    title = formation.etablissement ?: "Établissement"
                    snippet = formation.ville ?: ""
                    
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(formation.id)
                        true
                    }
                }
                
                mapView.overlays.add(marker)
            }
            
            mapView.invalidate()
        }
    )
}
