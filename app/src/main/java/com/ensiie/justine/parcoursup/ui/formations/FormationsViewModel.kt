package com.ensiie.justine.parcoursup.ui.formations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationStats
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.utils.LocationFilter
import com.ensiie.justine.parcoursup.utils.LocationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FormationsViewModel(
    private val repository: FormationRepository,
    private val adresseRepository: AdresseRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<FormationsUiState>(FormationsUiState.Loading)
    val uiState: StateFlow<FormationsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilieres = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilieres: StateFlow<Set<String>> = _selectedFilieres.asStateFlow()
    
    private val _selectedTypesEtab = MutableStateFlow<Set<String>>(emptySet())
    val selectedTypesEtab: StateFlow<Set<String>> = _selectedTypesEtab.asStateFlow()
    
    private val _locationFilter = MutableStateFlow<LocationFilter?>(null)
    val locationFilter: StateFlow<LocationFilter?> = _locationFilter.asStateFlow()
    
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
            
            val location = _locationFilter.value
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
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
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
            
            val location = _locationFilter.value
            repository.searchFormations(
                name = query,
                filieres = _selectedFilieres.value,
                contratsEtab = _selectedTypesEtab.value,
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
        if (isSearchMode && _searchQuery.value.isNotBlank()) {
            searchFormations(_searchQuery.value, append = true)
        } else {
            loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value, append = true)
        }
    }
    
    fun toggleFiliere(filiere: String) {
        _selectedFilieres.value = if (_selectedFilieres.value.contains(filiere)) {
            _selectedFilieres.value - filiere
        } else {
            _selectedFilieres.value + filiere
        }
        loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
    }
    
    fun clearFilieres() {
        _selectedFilieres.value = emptySet()
        loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
    }
        
    fun toggleContratEtab(contrat: String) {
        _selectedTypesEtab.value = if (_selectedTypesEtab.value.contains(contrat)) {
            _selectedTypesEtab.value - contrat
        } else {
            _selectedTypesEtab.value + contrat
        }
        loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
    }
    
    fun clearContratsEtab() {
        _selectedTypesEtab.value = emptySet()
        loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
    }
    
    fun setLocationFilter(location: LocationFilter) {
        _locationFilter.value = location
        if (_searchQuery.value.isBlank()) {
            loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
        } else {
            searchFormations(_searchQuery.value)
        }
    }
    
    fun clearLocationFilter() {
        _locationFilter.value = null
        if (_searchQuery.value.isBlank()) {
            loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value)
        } else {
            searchFormations(_searchQuery.value)
        }
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
            
            if (isSearchMode && _searchQuery.value.isNotBlank()) {
                searchFormations(_searchQuery.value, append = false)
            } else {
                loadFormations(_selectedFilieres.value, contratsEtab = _selectedTypesEtab.value, append = false)
            }
            
            _isRefreshing.value = false
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
