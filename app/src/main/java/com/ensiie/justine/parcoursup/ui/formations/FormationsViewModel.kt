package com.ensiie.justine.parcoursup.ui.formations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationStats
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormationsViewModel @Inject constructor(
    private val repository: FormationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<FormationsUiState>(FormationsUiState.Loading)
    val uiState: StateFlow<FormationsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFiliere = MutableStateFlow<String?>(null)
    val selectedFiliere: StateFlow<String?> = _selectedFiliere.asStateFlow()
    
    private val _selectedTypeEtab = MutableStateFlow<String?>(null)
    val selectedTypeEtab: StateFlow<String?> = _selectedTypeEtab.asStateFlow()
    
    private val _filieres = MutableStateFlow<List<String>>(emptyList())
    val filieres: StateFlow<List<String>> = _filieres.asStateFlow()
    
    private val _contratsEtab = MutableStateFlow<List<String>>(emptyList())
    val contratsEtab: StateFlow<List<String>> = _contratsEtab.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
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
        filiere: String? = null,
        contratEtab: String? = null,
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
            
            repository.searchFormations(
                name = null,
                filiere = filiere,
                contratEtab = contratEtab,
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
            loadFormations(_selectedFiliere.value, contratEtab = _selectedTypeEtab.value)
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
            
            repository.searchFormations(
                name = query,
                filiere = _selectedFiliere.value,
                contratEtab = _selectedTypeEtab.value,
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
            loadFormations(_selectedFiliere.value, contratEtab = _selectedTypeEtab.value, append = true)
        }
    }
    
    fun selectFiliere(filiere: String?) {
        _selectedFiliere.value = filiere
        loadFormations(filiere, contratEtab = _selectedTypeEtab.value)
    }
        
    fun selectContratEtab(contratEtab: String?) {
        _selectedTypeEtab.value = contratEtab
        loadFormations(_selectedFiliere.value, contratEtab = contratEtab)
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
}

sealed interface FormationsUiState {
    data object Loading : FormationsUiState
    data class Success(
        val formations: List<Formation>,
    ) : FormationsUiState
    data class Error(val message: String) : FormationsUiState
}
