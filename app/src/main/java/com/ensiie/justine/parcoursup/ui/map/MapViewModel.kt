package com.ensiie.justine.parcoursup.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.ui.filters.SharedFilterViewModel
import com.ensiie.justine.parcoursup.utils.LocationFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: FormationRepository,
    private val adresseRepository: AdresseRepository,
    val sharedFilterViewModel: SharedFilterViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Use shared filter state from SharedFilterViewModel
    val locationFilter: StateFlow<LocationFilter?> = sharedFilterViewModel.locationFilter

    // Track clicked formations at location (persists across navigation)
    private val _clickedFormations = MutableStateFlow<List<FormationMap>?>(null)
    val clickedFormations: StateFlow<List<FormationMap>?> = _clickedFormations.asStateFlow()

    init {
        observeFilterChanges()
    }

    /**
     * Observe changes in shared filter state and reload map data accordingly
     */
    private fun observeFilterChanges() {
        viewModelScope.launch {
            // Collect and reload data whenever filter state changes
            sharedFilterViewModel.locationFilter.collect { location ->
                loadFormationsForMap(location)
            }
        }
    }

    private fun loadFormationsForMap(location: LocationFilter? = null) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading

            repository.getFormationsForMap(location = location)
                .onSuccess { formations ->
                    _uiState.value = MapUiState.Success(formations)
                }
                .onFailure { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Erreur de chargement de la carte"
                    )
                }
        }
    }

    fun retry() {
        loadFormationsForMap(locationFilter.value)
    }

    fun setLocationFilter(filter: LocationFilter) {
        sharedFilterViewModel.setLocationFilter(filter)
    }

    fun clearLocationFilter() {
        sharedFilterViewModel.clearLocationFilter()
    }

    fun setClickedFormations(formations: List<FormationMap>?) {
        _clickedFormations.value = formations
    }

}

sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(val formations: List<FormationMap>) : MapUiState
    data class Error(val message: String) : MapUiState
}
