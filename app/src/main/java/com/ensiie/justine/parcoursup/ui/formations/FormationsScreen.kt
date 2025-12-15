package com.ensiie.justine.parcoursup.ui.formations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val selectedFiliere by viewModel.selectedFiliere.collectAsState()
    val selectedTypeEtab by viewModel.selectedTypeEtab.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showFiliereDialog by remember { mutableStateOf(false) }
    var showContratEtabDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::searchFormations,
            onFiliereClick = { showFiliereDialog = true },
            onFilterClick = { showBottomSheet = true },
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
                    DsfrCircularProgressIndicator()
                }
            }

            is FormationsUiState.Success -> {
                val isLoadingMore by viewModel.isLoadingMore.collectAsState()

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
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            // Sheet content
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }) {
                Text("Hide bottom sheet")
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
    onFilterClick: () -> Unit,
    selectedFiliere: String?,
    onApprentissageClick: () -> Unit,
    selectedTypeEtab: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DsfrTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher une formation...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            DsfrIconButton(
                onClick = onFilterClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.filter_list_24px),
                    contentDescription = stringResource (R.string.filter_btn),
                    tint = DSFRTheme.colors.textDefault
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
        if (query.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DsfrFilterChip(
                    selected = selectedFiliere != null,
                    onClick = onFiliereClick,
                    label = {
                        Text(selectedFiliere ?: "Toutes les filières")
                    }
                )
                DsfrFilterChip(

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
fun FiliereDialog(
    filieres: List<String>,
    selectedFiliere: String?,
    onDismiss: () -> Unit,
    onFiliereSelected: (String?) -> Unit
) {
    DsfrAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_filiere)) },
        text = {
            LazyColumn {
                item {
                    DsfrTextButton(
                        onClick = {
                            if (selectedFiliere != null) {
                                onFiliereSelected(null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        selected = selectedFiliere == null
                    ) {
                        Text(stringResource(R.string.all_filieres))
                    }
                }

                items(filieres) { filiere ->
                    DsfrTextButton(
                        onClick = {
                            if (filiere != selectedFiliere) {
                                onFiliereSelected(filiere)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        selected = filiere == selectedFiliere
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

    DsfrAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Type de formation") },
        text = {
            Column {
                DsfrTextButton(
                    onClick = {
                        if (selectedTypeEtab != null) {
                            onContratEtabSelected(null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    selected = selectedTypeEtab == null
                ) {
                    Text("Toutes formations")
                }

                options.forEach { option ->
                    DsfrTextButton(
                        onClick = {
                            if (option != selectedTypeEtab) {
                                onContratEtabSelected(option)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        selected = selectedTypeEtab == option
                    ) {
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            DsfrTextButton(onClick = onDismiss) {
                DsfrText(stringResource(R.string.close))
            }
        }
    )
}
