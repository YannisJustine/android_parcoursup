package com.ensiie.justine.parcoursup.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FormationRepository
) : ViewModel() {
    
    private val args = savedStateHandle.toRoute<Route.FormationDetail>()
    private val recordId = args.id
    
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    val isFavorite: Flow<Boolean> = repository.isFavorite(recordId)
    
    init {
        loadFormationDetail()
    }
    
    private fun loadFormationDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            
            repository.getFormationById(recordId)
                .onSuccess { formation ->
                    _uiState.value = DetailUiState.Success(formation, formation)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.message ?: "Erreur de chargement"
                    )
                }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val formation = currentState.formation
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
    }
    
    fun retry() {
        loadFormationDetail()
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val formation: Formation, val baseFormation: Formation) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
