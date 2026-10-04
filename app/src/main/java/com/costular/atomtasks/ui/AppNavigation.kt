package com.costular.atomtasks.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.costular.atomtasks.ui.home.AppNavigator
import com.costular.atomtasks.ui.home.AtomAppState
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.scope.DestinationScopeWithNoDependencies

fun DestinationScopeWithNoDependencies<*>.currentNavigator(): AppNavigator {
    return AppNavigator(navController)
}

@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    appState: AtomAppState,
    fabClick: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fabRegistrars = remember(fabClick) {
        mutableMapOf<String, (() -> Unit) -> Unit>()
    }
    DestinationsNavHost(
        engine = rememberNavHostEngine(),
        navController = appState.navController,
        navGraph = NavGraphs.main,
        modifier = modifier,
        dependenciesContainerBuilder = {
            dependency(currentNavigator())
            val route = destination.route
            dependency(fabRegistrars.getOrPut(route) {
                { action -> fabClick(route, action) }
            })
        },
    )
}
