package com.ensiie.justine.parcoursup

import DsfrScaffold
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.navigation.*
import com.ensiie.justine.parcoursup.ui.detail.FormationDetailScreen
import com.ensiie.justine.parcoursup.ui.favorites.FavoritesScreen
import com.ensiie.justine.parcoursup.ui.formations.FormationsScreen
import com.ensiie.justine.parcoursup.ui.map.MapScreen
import com.ensiie.justine.parcoursup.ui.theme.ParcoursupTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DSFRTheme {
                ParcoursupApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ParcoursupApp() {
    val navigationState = rememberNavigationState(
        startRoute = FormationsList,
        topLevelRoutes = setOf(FormationsList, Favorites, GlobalMap)
    )

    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<FormationsList> {
            FormationsScreen(
                onNavigateToDetail = { recordId ->
                    navigator.navigate(FormationDetail(recordId))
                }
            )
        }

        entry<Favorites> {
            FavoritesScreen(
                onNavigateToDetail = { recordId ->
                    navigator.navigate(FormationDetail(recordId))
                }
            )
        }

        entry<GlobalMap> {
            MapScreen(
                onNavigateToFormations = { recordId ->
                    navigator.navigate(FormationDetail(recordId))
                }
            )
        }

        entry<FormationDetail> { detail ->
            FormationDetailScreen(
                id = detail.id,
                onNavigateBack = { navigator.goBack() }
            )
        }
    }

    val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
    val showBottomBar = currentRoute is FormationsList ||
            currentRoute is Favorites ||
            currentRoute is GlobalMap

    DsfrScaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedContent(
                targetState = showBottomBar,
                transitionSpec = {
                    slideInVertically { height -> height } with
                            slideOutVertically { height -> height }
                }
            ) { isVisible ->
                if (isVisible) {
                    NavigationBar {
                        TopLevelDestination.entries.forEach { destination ->
                            val selected = destination.route == navigationState.topLevelRoute

                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigator.navigate(destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(
                                            when (destination) {
                                                TopLevelDestination.FORMATIONS -> R.string.nav_formations
                                                TopLevelDestination.FAVORITES -> R.string.nav_favorites
                                                TopLevelDestination.MAP -> R.string.nav_map
                                            }
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    ) { innerPadding ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
            transitionSpec = {
                // Slide in from right when navigating forward
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(500)
                )
            },
            popTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            },
            predictivePopTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            }
        )
    }
}
