package com.ensiie.justine.parcoursup.ui.filters

import com.ensiie.justine.parcoursup.utils.LocationFilter

/**
 * Data class representing the complete filter state
 * that can be shared across multiple ViewModels and screens
 */
data class FilterState(
    val locationFilter: LocationFilter? = null,
    val selectedFilieres: Set<String> = emptySet(),
    val selectedTypesEtab: Set<String> = emptySet(),
    val searchQuery: String = ""
) {
    /**
     * Check if any filters are currently active
     */
    fun hasActiveFilters(): Boolean {
        return locationFilter != null ||
                selectedFilieres.isNotEmpty() ||
                selectedTypesEtab.isNotEmpty() ||
                searchQuery.isNotBlank()
    }
    
    /**
     * Clear all filters
     */
    fun clearAll(): FilterState {
        return FilterState()
    }
    
    /**
     * Get a count of active filters (excluding search query)
     */
    fun getActiveFilterCount(): Int {
        var count = 0
        if (locationFilter != null) count++
        if (selectedFilieres.isNotEmpty()) count++
        if (selectedTypesEtab.isNotEmpty()) count++
        return count
    }
}
