package com.ensiie.justine.parcoursup.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.utils.LocationFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: FormationRepository,
    private val adresseRepository: AdresseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
        
    private val _locationFilter = MutableStateFlow<LocationFilter?>(null)
    val locationFilter: StateFlow<LocationFilter?> = _locationFilter.asStateFlow()
    
    init {
        loadFormationsForMap()
    }
    
    private fun loadFormationsForMap() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            
            repository.getFormationsForMap()
                .onSuccess { formations ->
                    Log.d("MapViewModel", "Formations count: ${formations.size}")
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
        loadFormationsForMap()
    }

    fun setLocationFilter(filter: LocationFilter) {
        _locationFilter.value = filter
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            
            repository.getFormationsForMap(location = filter)
                .onSuccess { formations ->
                    Log.d("MapViewModel", "Formations count: ${formations.size}")
                    _uiState.value = MapUiState.Success(formations)
                }
                .onFailure { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Erreur de chargement de la carte"
                    )
                }
        }
    }

    fun clearLocationFilter() {
        _locationFilter.value = null
        loadFormationsForMap()
    }

}

sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(val formations: List<FormationMap>) : MapUiState
    data class Error(val message: String) : MapUiState
}
