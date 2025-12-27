package com.ensiie.justine.parcoursup

import DsfrScaffold
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ensiie.justine.parcoursup.navigation.Favorites
import com.ensiie.justine.parcoursup.navigation.FormationDetail
import com.ensiie.justine.parcoursup.navigation.FormationsList
import com.ensiie.justine.parcoursup.navigation.GlobalMap
import com.ensiie.justine.parcoursup.navigation.Navigator
import com.ensiie.justine.parcoursup.navigation.TopLevelDestination
import com.ensiie.justine.parcoursup.navigation.rememberNavigationState
import com.ensiie.justine.parcoursup.navigation.toEntries
import com.ensiie.justine.parcoursup.ui.detail.FormationDetailScreen
import com.ensiie.justine.parcoursup.ui.favorites.FavoritesScreen
import com.ensiie.justine.parcoursup.ui.formations.FormationsScreen
import com.ensiie.justine.parcoursup.ui.map.MapScreen
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

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
    var topLevelRoutes = setOf(FormationsList, Favorites, GlobalMap)

    val navigationState = rememberNavigationState(
        startRoute = FormationsList,
        topLevelRoutes
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
    val showBottomBar = currentRoute in topLevelRoutes

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
                    NavigationBar(
                        containerColor = DSFRTheme.colors.backgroundRaised,
                    ) {
                        TopLevelDestination.entries.forEach { destination ->
                            val selected = destination.route == navigationState.topLevelRoute

                            NavigationBarItem(
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DSFRTheme.colors.actionHigh,
                                    unselectedIconColor = DSFRTheme.colors.textDefault,
                                    selectedTextColor = DSFRTheme.colors.actionHigh,
                                    unselectedTextColor = DSFRTheme.colors.textDefault,
                                    indicatorColor = DSFRTheme.colors.backgroundActionLow
                                ),
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
        val routeIndexMap = TopLevelDestination.entries
            .mapIndexed { index, destination -> destination.route.toString() to index }
            .toMap()

        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
            transitionSpec = {
                val fromRoute = initialState.key.toString()
                val toRoute = targetState.key.toString()

                val fromIndex = routeIndexMap[fromRoute] ?: -1
                val toIndex = routeIndexMap[toRoute] ?: -1

                if (fromIndex != -1 && toIndex != -1) {
                    if (toIndex > fromIndex) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(500)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(500)
                        )
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(500)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(500)
                        )
                    }
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(500)
                    )
                }
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            },
            predictivePopTransitionSpec = {
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
