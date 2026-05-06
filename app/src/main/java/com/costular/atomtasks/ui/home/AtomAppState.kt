package com.costular.atomtasks.ui.home

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.generated.agenda.destinations.AgendaScreenDestination
import com.ramcosta.composedestinations.generated.settings.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.settings.destinations.ThemeSelectorScreenDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState

@Composable
fun rememberAtomAppState(
    navController: NavHostController,
): AtomAppState {
    return remember(navController) {
        AtomAppState(
            navController = navController,
        )
    }
}

@Stable
class AtomAppState(
    val navController: NavHostController,
) {
    val currentDestination: DestinationSpec?
        @Composable get() = navController.currentDestinationAsState().value

    val shouldShowNavigation: Boolean
        @Composable get() = currentDestination?.route in listOf(
            AgendaScreenDestination,
            SettingsScreenDestination,
            ThemeSelectorScreenDestination,
        ).map { it.route }

    val navigationLayoutType: NavigationSuiteType
        @Composable get() {
            val adaptiveInfo = currentWindowAdaptiveInfo()
            return if (!shouldShowNavigation) {
                NavigationSuiteType.None
            } else {
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
            }
        }

    fun navigateToTopLevelDestination(selected: DestinationSpec) {
        navController.navigate(selected.route) {
            launchSingleTop = true
            restoreState = true

            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
}
