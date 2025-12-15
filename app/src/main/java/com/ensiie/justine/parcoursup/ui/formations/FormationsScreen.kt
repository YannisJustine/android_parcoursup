package com.ensiie.justine.parcoursup.ui.formations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.Formation
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FormationsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filieres by viewModel.filieres.collectAsState()
    val contratsEtab by viewModel.contratsEtab.collectAsState()
    val selectedFiliere by viewModel.selectedFiliere.collectAsState()
    val selectedTypeEtab by viewModel.selectedTypeEtab.collectAsState()

    var showFiliereDialog by remember { mutableStateOf(false) }
    var showContratEtabDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::searchFormations,
            onFiliereClick = { showFiliereDialog = true },
            selectedFiliere = selectedFiliere,
            onApprentissageClick = { showContratEtabDialog = true },
            selectedTypeEtab = selectedTypeEtab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when (val state = uiState) {
            is FormationsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is FormationsUiState.Success -> {
                val isLoadingMore by viewModel.isLoadingMore.collectAsState()
                val listState = rememberLazyListState()

                LaunchedEffect(Unit) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                        .map { visibleItems ->
                            val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                            val totalItems = listState.layoutInfo.totalItemsCount
                            Pair(lastVisibleItemIndex, totalItems)
                        }
                        .collect { (lastVisibleItemIndex, totalItems) ->
                            if (state.formations.isEmpty()) {
                                return@collect
                            }

                            if (lastVisibleItemIndex >= totalItems - 1) {
                                viewModel.loadMoreFormations()
                            }
                        }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
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

    if (showFiliereDialog) {
        FiliereDialog(
            filieres = filieres,
            selectedFiliere = selectedFiliere,
            onDismiss = { showFiliereDialog = false },
            onFiliereSelected = { filiere ->
                viewModel.selectFiliere(filiere)
                showFiliereDialog = false
            }
        )
    }

    if (showContratEtabDialog) {
        ContratEtabDialog(
            options = contratsEtab,
            selectedTypeEtab = selectedTypeEtab,
            onDismiss = { showContratEtabDialog = false },
            onContratEtabSelected = { contratEtab ->
                viewModel.selectContratEtab(contratEtab)
                showContratEtabDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFiliereClick: () -> Unit,
    selectedFiliere: String?,
    onApprentissageClick: () -> Unit,
    selectedTypeEtab: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher une formation...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        if (query.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFiliere != null,
                    onClick = onFiliereClick,
                    label = {
                        Text(selectedFiliere ?: "Toutes les filières")
                    }
                )
                FilterChip(
                    selected = selectedTypeEtab != null,
                    onClick = onApprentissageClick,
                    label = {
                        Text(selectedTypeEtab ?: "Toutes formations")
                    }
                )
            }
        }
    }
}

@Composable
fun FormationCard(
    formation: Formation,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean
) {
    Card(
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
                Text(
                    text = formation.etablissement
                        ?: stringResource(R.string.establishment_unknown),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formation.formationDetailleeString
                        ?: formation.formationLibString
                        ?: stringResource(R.string.formation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${formation.ville ?: ""} - ${formation.departement ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Chip("id: ${formation.id}")
                    formation.capacite?.let {
                        Chip(stringResource(R.string.capacity) + ": $it")
                    }
                    formation.tauxAcces?.let {
                        Chip(stringResource(R.string.access_format, it))
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
                    tint = if (isFavorite) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun FiliereDialog(
    filieres: List<String>,
    selectedFiliere: String?,
    onDismiss: () -> Unit,
    onFiliereSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_filiere)) },
        text = {
            LazyColumn {
                item {
                    TextButton(
                        onClick = {
                            if (selectedFiliere != null) {
                                onFiliereSelected(null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selectedFiliere == null)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(stringResource(R.string.all_filieres))
                    }
                }

                items(filieres) { filiere ->
                    TextButton(
                        onClick = {
                            if (filiere != selectedFiliere) {
                                onFiliereSelected(filiere)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selectedFiliere == filiere)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(filiere)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun ContratEtabDialog(
    options: List<String>,
    selectedTypeEtab: String?,
    onDismiss: () -> Unit,
    onContratEtabSelected: (String?) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Type de formation") },
        text = {
            Column {
                TextButton(
                    onClick = {
                        if (selectedTypeEtab != null) {
                            onContratEtabSelected(null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selectedTypeEtab == null)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface
                    )


                ) {
                    Text("Toutes formations")
                }

                options.forEach { option ->
                    TextButton(
                        onClick = {
                            if (option != selectedTypeEtab) {
                                onContratEtabSelected(option)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selectedTypeEtab == option)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
