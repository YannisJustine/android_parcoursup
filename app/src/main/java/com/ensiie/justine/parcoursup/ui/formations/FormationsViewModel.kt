package com.ensiie.justine.parcoursup.ui.formations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.ui.filters.SharedFilterViewModel
import com.ensiie.justine.parcoursup.utils.LocationFilter
import com.ensiie.justine.parcoursup.utils.LocationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FormationsViewModel(
    private val repository: FormationRepository,
    private val adresseRepository: AdresseRepository,
    private val locationHelper: LocationHelper,
    private val sharedFilterViewModel: SharedFilterViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<FormationsUiState>(FormationsUiState.Loading)
    val uiState: StateFlow<FormationsUiState> = _uiState.asStateFlow()

    // Use shared filter state from SharedFilterViewModel
    val searchQuery: StateFlow<String> = sharedFilterViewModel.searchQuery
    val selectedFilieres: StateFlow<Set<String>> = sharedFilterViewModel.selectedFilieres
    val selectedTypesEtab: StateFlow<Set<String>> = sharedFilterViewModel.selectedTypesEtab
    val locationFilter: StateFlow<LocationFilter?> = sharedFilterViewModel.locationFilter

    private val _filieres = MutableStateFlow<List<String>>(emptyList())
    val filieres: StateFlow<List<String>> = _filieres.asStateFlow()

    private val _contratsEtab = MutableStateFlow<List<String>>(emptyList())
    val contratsEtab: StateFlow<List<String>> = _contratsEtab.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentOffset = 0
    private var canLoadMore = true
    private var isSearchMode = false

    init {
        loadFormations()
        loadFilieres()
        loadContratsEtab()
        observeFilterChanges()
    }

    private fun loadFilieres() {
        viewModelScope.launch {
            repository.getFilieres()
                .onSuccess { filieres ->
                    _filieres.value = filieres
                }
                .onFailure {
                    _filieres.value = emptyList()
                }
        }
    }

    /**
     * Observe changes in shared filter state and reload data accordingly
     */
    private fun observeFilterChanges() {
        viewModelScope.launch {
            sharedFilterViewModel.filterState.collect { filterState ->
                // Reload formations when filters change
                if (filterState.searchQuery.isNotBlank()) {
                    searchFormations(filterState.searchQuery)
                } else {
                    loadFormations(filterState.selectedFilieres, filterState.selectedTypesEtab)
                }
            }
        }
    }

    fun loadFormations(
        filieres: Set<String> = emptySet(),
        contratsEtab: Set<String> = emptySet(),
        append: Boolean = false
    ) {
        viewModelScope.launch {
            if (!append) {
                _uiState.value = FormationsUiState.Loading
                currentOffset = 0
                canLoadMore = true
                isSearchMode = false
            } else if (!canLoadMore || _isLoadingMore.value) {
                return@launch
            }

            if (append) _isLoadingMore.value = true

            val location = locationFilter.value
            repository.searchFormations(
                name = null,
                filieres = filieres,
                contratsEtab = contratsEtab,
                latitude = location?.latitude,
                longitude = location?.longitude,
                radiusKm = location?.radiusKm,
                offset = currentOffset,
                limit = 100
            )
                .onSuccess { formations ->
                    canLoadMore = formations.size >= 100
                    currentOffset += formations.size

                    val currentState = _uiState.value
                    val allFormations = if (append && currentState is FormationsUiState.Success) {
                        currentState.formations + formations
                    } else {
                        formations
                    }
                    _uiState.value = FormationsUiState.Success(
                        formations = allFormations,
                    )
                    _isLoadingMore.value = false
                }
                .onFailure { error ->
                    if (!append) {
                        _uiState.value = FormationsUiState.Error(
                            error.message ?: "Unknown error"
                        )
                    }
                    _isLoadingMore.value = false
                }
        }
    }

    private fun loadContratsEtab() {
        viewModelScope.launch {
            repository.getContratsEtab()
                .onSuccess { types ->
                    _contratsEtab.value = types
                }
                .onFailure {
                    _contratsEtab.value = emptyList()
                }
        }
    }

    fun searchFormations(query: String, append: Boolean = false) {
        sharedFilterViewModel.setSearchQuery(query)

        if (query.isBlank()) {
            loadFormations(selectedFilieres.value, contratsEtab = selectedTypesEtab.value)
            return
        }

        viewModelScope.launch {
            if (!append) {
                _uiState.value = FormationsUiState.Loading
                currentOffset = 0
                canLoadMore = true
                isSearchMode = true
            } else if (!canLoadMore || _isLoadingMore.value) {
                return@launch
            }

            if (append) _isLoadingMore.value = true

            val location = locationFilter.value
            repository.searchFormations(
                name = query,
                filieres = selectedFilieres.value,
                contratsEtab = selectedTypesEtab.value,
                latitude = location?.latitude,
                longitude = location?.longitude,
                radiusKm = location?.radiusKm,
                offset = currentOffset,
                limit = 50
            )
                .onSuccess { formations ->
                    canLoadMore = formations.size >= 50
                    currentOffset += formations.size

                    val currentState = _uiState.value
                    val allFormations = if (append && currentState is FormationsUiState.Success) {
                        currentState.formations + formations
                    } else {
                        formations
                    }

                    _uiState.value = FormationsUiState.Success(
                        formations = allFormations,
                    )
                    _isLoadingMore.value = false
                }
                .onFailure { error ->
                    if (!append) {
                        _uiState.value = FormationsUiState.Error(
                            error.message ?: "Unknown error"
                        )
                    }
                    _isLoadingMore.value = false
                }
        }
    }

    fun loadMoreFormations() {
        if (isSearchMode && searchQuery.value.isNotBlank()) {
            searchFormations(searchQuery.value, append = true)
        } else {
            loadFormations(
                selectedFilieres.value,
                contratsEtab = selectedTypesEtab.value,
                append = true
            )
        }
    }

    fun toggleFiliere(filiere: String) {
        sharedFilterViewModel.toggleFiliere(filiere)
    }

    fun clearFilieres() {
        sharedFilterViewModel.clearFilieres()
    }

    fun toggleContratEtab(contrat: String) {
        sharedFilterViewModel.toggleTypeEtab(contrat)
    }

    fun clearContratsEtab() {
        sharedFilterViewModel.clearTypesEtab()
    }

    fun setLocationFilter(location: LocationFilter) {
        sharedFilterViewModel.setLocationFilter(location)
    }

    fun clearLocationFilter() {
        sharedFilterViewModel.clearLocationFilter()
    }

    fun toggleFavorite(formation: Formation) {
        viewModelScope.launch {
            val formationId = formation.id
            repository.isFavorite(formationId).first().let { isFav ->
                if (isFav) {
                    repository.removeFavorite(formationId)
                } else {
                    repository.addFavorite(formation)
                }
            }
        }
    }

    fun isFavorite(id: String): Flow<Boolean> =
        repository.isFavorite(id)

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            currentOffset = 0
            canLoadMore = true

            fun refresh() {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    currentOffset = 0
                    canLoadMore = true

                    if (isSearchMode && searchQuery.value.isNotBlank()) {
                        searchFormations(searchQuery.value, append = false)
                    } else {
                        loadFormations(
                            selectedFilieres.value,
                            contratsEtab = selectedTypesEtab.value,
                            append = false
                        )
                    }

                    _isRefreshing.value = false
                }
            }
        }
    }
}

sealed interface FormationsUiState {
    data object Loading : FormationsUiState
    data class Success(
        val formations: List<Formation>,
    ) : FormationsUiState

    data class Error(val message: String) : FormationsUiState
}
