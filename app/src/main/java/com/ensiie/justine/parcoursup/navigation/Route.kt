package com.ensiie.justine.parcoursup.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object FormationsList : Route
    
    @Serializable
    data object Favorites : Route
    
    @Serializable
    data object GlobalMap : Route
    
    @Serializable
    data class FormationDetail(val id: String) : Route
}

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    val label: String
) {
    FORMATIONS(
        route = Route.FormationsList,
        icon = Icons.Default.Search,
        label = "Formations"
    ),
    FAVORITES(
        route = Route.Favorites,
        icon = Icons.Default.Favorite,
        label = "Favoris"
    ),
    MAP(
        route = Route.GlobalMap,
        icon = Icons.Default.Place,
        label = "Carte"
    )
}
