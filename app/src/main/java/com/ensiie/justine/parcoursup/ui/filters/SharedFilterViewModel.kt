package com.ensiie.justine.parcoursup.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensiie.justine.parcoursup.utils.LocationFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Shared ViewModel for managing filters across multiple screens.
 * This ViewModel is registered as a singleton in Koin DI, ensuring
 * the same instance is shared between list view and map view.
 */
class SharedFilterViewModel : ViewModel() {
    
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    // Individual state flows for convenient access using map and stateIn
    val locationFilter: StateFlow<LocationFilter?> = 
        _filterState.map { it.locationFilter }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val selectedFilieres: StateFlow<Set<String>> = 
        _filterState.map { it.selectedFilieres }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    
    val selectedTypesEtab: StateFlow<Set<String>> = 
        _filterState.map { it.selectedTypesEtab }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    
    val searchQuery: StateFlow<String> = 
        _filterState.map { it.searchQuery }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    
    /**
     * Set location filter
     */
    fun setLocationFilter(filter: LocationFilter) {
        _filterState.update { it.copy(locationFilter = filter) }
    }
    
    /**
     * Clear location filter
     */
    fun clearLocationFilter() {
        _filterState.update { it.copy(locationFilter = null) }
    }
    
    /**
     * Toggle a filiere selection
     */
    fun toggleFiliere(filiere: String) {
        _filterState.update { currentState ->
            val newFilieres = if (currentState.selectedFilieres.contains(filiere)) {
                currentState.selectedFilieres - filiere
            } else {
                currentState.selectedFilieres + filiere
            }
            currentState.copy(selectedFilieres = newFilieres)
        }
    }
    
    /**
     * Set multiple filieres at once
     */
    fun setFilieres(filieres: Set<String>) {
        _filterState.update { it.copy(selectedFilieres = filieres) }
    }
    
    /**
     * Clear all filiere selections
     */
    fun clearFilieres() {
        _filterState.update { it.copy(selectedFilieres = emptySet()) }
    }
    
    /**
     * Toggle a type etablissement selection
     */
    fun toggleTypeEtab(type: String) {
        _filterState.update { currentState ->
            val newTypes = if (currentState.selectedTypesEtab.contains(type)) {
                currentState.selectedTypesEtab - type
            } else {
                currentState.selectedTypesEtab + type
            }
            currentState.copy(selectedTypesEtab = newTypes)
        }
    }
    
    /**
     * Set multiple types etablissement at once
     */
    fun setTypesEtab(types: Set<String>) {
        _filterState.update { it.copy(selectedTypesEtab = types) }
    }
    
    /**
     * Clear all type etablissement selections
     */
    fun clearTypesEtab() {
        _filterState.update { it.copy(selectedTypesEtab = emptySet()) }
    }
    
    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }
    
    /**
     * Clear search query
     */
    fun clearSearchQuery() {
        _filterState.update { it.copy(searchQuery = "") }
    }
    
    /**
     * Clear all filters
     */
    fun clearAllFilters() {
        _filterState.value = FilterState()
    }
    
    /**
     * Check if any filters are active
     */
    fun hasActiveFilters(): Boolean {
        return _filterState.value.hasActiveFilters()
    }
    
    /**
     * Get count of active filters
     */
    fun getActiveFilterCount(): Int {
        return _filterState.value.getActiveFilterCount()
    }
}
