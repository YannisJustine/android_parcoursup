package com.ensiie.justine.parcoursup.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FormationRepository
) : ViewModel() {
    
    val uiState: StateFlow<FavoritesUiState> = repository.getAllFavorites()
        .map<List<FavoriteFormation>, FavoritesUiState> { favorites ->
            if (favorites.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Success(favorites)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading
        )
    
    fun removeFavorite(id: String) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }
}

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val favorites: List<FavoriteFormation>) : FavoritesUiState
}
