package com.ensiie.justine.parcoursup.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FormationsList : NavKey

@Serializable
data object Favorites : NavKey

@Serializable
data object GlobalMap : NavKey

@Serializable
data class FormationDetail(val id: String) : NavKey

enum class TopLevelDestination(
    val route: NavKey,
    val icon: ImageVector,
    val label: String
) {
    FORMATIONS(
        route = FormationsList,
        icon = Icons.Default.Search,
        label = "Formations"
    ),
    FAVORITES(
        route = Favorites,
        icon = Icons.Default.Favorite,
        label = "Favoris"
    ),
    MAP(
        route = GlobalMap,
        icon = Icons.Default.Place,
        label = "Carte"
    )
}
