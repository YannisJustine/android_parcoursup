package com.ensiie.justine.parcoursup

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.navigation.Route
import com.ensiie.justine.parcoursup.navigation.TopLevelDestination
import com.ensiie.justine.parcoursup.ui.detail.FormationDetailScreen
import com.ensiie.justine.parcoursup.ui.favorites.FavoritesScreen
import com.ensiie.justine.parcoursup.ui.formations.FormationsScreen
import com.ensiie.justine.parcoursup.ui.map.MapScreen
import com.ensiie.justine.parcoursup.ui.theme.ParcoursupTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParcoursupTheme {
                ParcoursupApp()
            }
        }
    }
}

@Composable
fun ParcoursupApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Check if we're on a detail screen (hide bottom bar)
    val showBottomBar = TopLevelDestination.entries.any {
        currentDestination?.hasRoute(it.route::class) == true
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hasRoute(destination.route::class) == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(stringResource(
                                when(destination) {
                                    TopLevelDestination.FORMATIONS -> R.string.nav_formations
                                    TopLevelDestination.FAVORITES -> R.string.nav_favorites
                                    TopLevelDestination.MAP -> R.string.nav_map
                                }
                            )) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.FormationsList,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable<Route.FormationsList> {
                FormationsScreen(
                    onNavigateToDetail = { recordId ->
                        navController.navigate(Route.FormationDetail(recordId))
                    }
                )
            }
            
            composable<Route.Favorites> {
                FavoritesScreen(
                    onNavigateToDetail = { recordId ->
                        navController.navigate(Route.FormationDetail(recordId))
                    }
                )
            }
            
            composable<Route.GlobalMap> {
                MapScreen(
                    onNavigateToFormations = { recordId ->
                        navController.navigate(Route.FormationDetail(recordId))
                    }
                )
            }
            
            composable<Route.FormationDetail> { backStackEntry ->
                val detail = backStackEntry.toRoute<Route.FormationDetail>()
                FormationDetailScreen(
                    id = detail.id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
