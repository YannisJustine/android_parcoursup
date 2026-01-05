package com.ensiie.justine.parcoursup.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.EtablissementContactInfo
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.repository.EtablissementRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FormationDetailViewModel(
    private val recordId: String,
    private val repository: FormationRepository,
    private val etablissementRepository: EtablissementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isFavorite: Flow<Boolean> = repository.isFavorite(recordId)

    init {
        println("FormationDetailViewModel créé avec recordId: $recordId")
        loadFormationDetail()
    }

    private fun loadFormationDetail() {
        viewModelScope.launch {
            println("Chargement de la formation avec recordId: $recordId")
            _uiState.value = DetailUiState.Loading
            repository.getFormationById(recordId)
                .onSuccess { formation ->
                    println("Formation trouvée: $formation")
                    etablissementRepository.getEtablissementContactInfo(formation.etablissementUai)
                        .onSuccess { etablissementInfo ->
                            // Load similar formations
                            val coords = formation.coordonnees
                            repository.getSimilarFormations(
                                currentFormationId = formation.id,
                                latitude = coords?.getOrNull(0),
                                longitude = coords?.getOrNull(1),
                                limit = 5
                            ).onSuccess { similarFormations ->
                                _uiState.value = DetailUiState.Success(
                                    formation,
                                    etablissementInfo,
                                    similarFormations
                                )
                            }.onFailure {
                                // Even if similar formations fail, show the main formation
                                _uiState.value = DetailUiState.Success(
                                    formation,
                                    etablissementInfo,
                                    emptyList()
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.value = DetailUiState.Error(
                                error.message ?: "Erreur de chargement"
                            )
                        }
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
    data class Success(
        val formation: Formation,
        val etablissementInfo: EtablissementContactInfo,
        val similarFormations: List<Formation> = emptyList()
    ) : DetailUiState

    data class Error(val message: String) : DetailUiState
}
