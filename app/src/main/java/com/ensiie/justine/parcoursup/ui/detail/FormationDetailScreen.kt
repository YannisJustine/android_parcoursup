package com.ensiie.justine.parcoursup.ui.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.osmdroid.compose.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationDetailScreen(
    id: String,
    onNavigateBack: () -> Unit,
    viewModel: FormationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState(initial = false)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite 
                                             else Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.remove_from_favorites 
                                    else R.string.add_to_favorites
                                ),
                                tint = if (isFavorite) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is DetailUiState.Success -> {
                FormationDetailContent(
                    formation = state.formation,
                    baseFormation = state.baseFormation,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
}

@SuppressLint("DefaultLocale")
@Composable
fun FormationDetailContent(
    formation: Formation,
    baseFormation: Formation,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formation.etablissement
                        ?: stringResource(R.string.establishment_unknown),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formation.formationDetailleeString
                        ?: formation.formationLibString
                        ?: stringResource(R.string.formation),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${formation.ville ?: ""} - ${formation.departement ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = formation.filiereString ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Text(
            text = stringResource(R.string.stats_2024),
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = stringResource(R.string.capacity),
                value = formation.capacite?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.wishes),
                value = formation.voeux?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = stringResource(R.string.admitted),
                value = formation.admis?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.access_rate),
                value = formation.tauxAcces?.let { 
                    String.format("%.1f%%", it) 
                } ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = stringResource(R.string.gender_distribution),
            style = MaterialTheme.typography.titleMedium
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressRow(
                    label = stringResource(R.string.women),
                    percentage = formation.pctFemmes ?: 0.0,
                    count = formation.accFemmes ?: 0
                )
                ProgressRow(
                    label = stringResource(R.string.men),
                    percentage = 100 - (formation.pctFemmes ?: 0.0),
                    count = formation.admis ?: 0
                )
            }
        }
        
        Text(
            text = stringResource(R.string.bac_distribution),
            style = MaterialTheme.typography.titleMedium
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressRow(
                    label = stringResource(R.string.bac_general),
                    percentage = formation.pctBacGeneral ?: 0.0,
                    count = formation.accBacGeneral ?: 0
                )
                ProgressRow(
                    label = stringResource(R.string.bac_tech),
                    percentage = formation.pctBacTech ?: 0.0,
                    count = formation.accBacTech ?: 0
                )
                ProgressRow(
                    label = stringResource(R.string.bac_pro),
                    percentage = formation.pctBacPro ?: 0.0,
                    count = formation.accBacPro ?: 0
                )
            }
        }
        
        Text(
            text = stringResource(R.string.bac_mentions),
            style = MaterialTheme.typography.titleMedium
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MentionRow(stringResource(R.string.mention_none), formation.mentionSans ?: 0)
                MentionRow(stringResource(R.string.mention_ab), formation.mentionAB ?: 0)
                MentionRow(stringResource(R.string.mention_b), formation.mentionB ?: 0)
                MentionRow(stringResource(R.string.mention_tb), formation.mentionTB ?: 0)
                MentionRow(stringResource(R.string.mention_tbf), formation.mentionTBF?.toInt() ?: 0)
            }
        }
        
        val coords = baseFormation.coordonnees
        if (coords != null && coords.size >= 2) {
            Text(
                text = stringResource(R.string.location),
                style = MaterialTheme.typography.titleMedium
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                FormationMap(
                    latitude = coords[0],
                    longitude = coords[1],
                    etablissement = baseFormation.etablissement ?: ""
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProgressRow(
    label: String,
    percentage: Double,
    count: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${String.format("%.1f", percentage)}% ($count)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun MentionRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = count.toString(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FormationMap(
    latitude: Double,
    longitude: Double,
    etablissement: String
) {
    MapView(
        modifier = Modifier.fillMaxSize(),
        onInitMapView = { mapView ->
            Configuration.getInstance().userAgentValue = "ParcoursupApp"
            
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(GeoPoint(latitude, longitude))
        },
        onUpdateMapView = { mapView ->
            mapView.overlays.clear()
            
            val marker = Marker(mapView).apply {
                position = GeoPoint(latitude, longitude)
                title = etablissement
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    )
}
