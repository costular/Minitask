package com.costular.atomtasks.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.costular.atomtasks.core.ui.AppSnackbarHostEffect
import com.costular.atomtasks.core.ui.rememberAppSnackbarState
import com.costular.atomtasks.core.ui.DestinationsScaffold
import com.costular.atomtasks.core.ui.SnackbarController
import com.costular.atomtasks.ui.AppNavigation
import com.costular.atomtasks.data.settings.DefaultTab
import com.costular.designsystem.theme.AtomTheme
import com.ramcosta.composedestinations.generated.agenda.destinations.AgendaScreenDestination
import com.ramcosta.composedestinations.generated.verticaltasks.destinations.VerticalTasksScreenDestination
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.costular.atomtasks.core.ui.R.string as S

internal const val CreateTaskFabTag = "CreateTaskFab"

@Composable
fun App(
    isDarkTheme: Boolean,
    defaultTab: DefaultTab,
    modifier: Modifier = Modifier,
) {
    AtomTheme(darkTheme = isDarkTheme) {
        val engine = rememberNavHostEngine()
        val navController = engine.rememberNavController()

        Home(
            atomAppState = rememberAtomAppState(
                navController = navController,
            ),
            defaultTab = defaultTab,
            modifier = modifier,
        )
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun Home(
    atomAppState: AtomAppState,
    modifier: Modifier = Modifier,
    defaultTab: DefaultTab = DefaultTab.Agenda,
) {
    val fabActions = remember { mutableStateMapOf<String, () -> Unit>() }
    val setFabOnClick = remember {
        { route: String, action: () -> Unit -> fabActions[route] = action }
    }
    val currentDestination = atomAppState.currentDestination
    val snackbarState = rememberAppSnackbarState()

    AppSnackbarHostEffect(
        appSnackbarState = snackbarState,
        snackbarController = SnackbarController,
    )

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            HomeNavigationDestination.entries.forEach { destination ->
                val isCurrentDestination = currentDestination == destination.screen

                item(
                    selected = isCurrentDestination,
                    onClick = {
                        atomAppState.navigateToTopLevelDestination(destination.graph)
                    },
                    icon = {
                        HomeNavigationItemIcon(
                            destination = destination,
                            selected = isCurrentDestination,
                        )
                    },
                    label = { Text(stringResource(destination.labelResId)) },
                )
            }
        },
        layoutType = atomAppState.navigationLayoutType,
    ) {
        DestinationsScaffold(
            navController = atomAppState.navController,
            snackbarHostState = snackbarState.hostState,
            floatingActionButton = {
                AddTaskFloatingActionButton(
                    shouldBeShown = currentDestination == AgendaScreenDestination ||
                        currentDestination == VerticalTasksScreenDestination,
                    fabOnclick = fabActions[currentDestination?.route],
                    shouldBeExpanded = true,
                )
            },
        ) { padding ->
            AppNavigation(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                appState = atomAppState,
                defaultTab = defaultTab,
                fabClick = setFabOnClick,
            )
        }
    }
}

@Composable
private fun AddTaskFloatingActionButton(
    shouldBeShown: Boolean,
    shouldBeExpanded: Boolean,
    fabOnclick: (() -> Unit)?,
) {
    AnimatedVisibility(
        visible = shouldBeShown,
        enter = scaleIn(
            animationSpec = tween(
                easing = FastOutSlowInEasing,
            ),
        ),
        exit = scaleOut(
            animationSpec = tween(
                easing = FastOutSlowInEasing,
            ),
        ),
    ) {
        if (shouldBeExpanded) {
            ExtendedFloatingActionButton(
                modifier = Modifier.testTag(CreateTaskFabTag),
                onClick = {
                    fabOnclick?.invoke()
                },
                text = {
                    Text(stringResource(S.agenda_create_new_task))
                },
                icon = {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                }
            )
        } else {
            FloatingActionButton(modifier = Modifier.testTag(CreateTaskFabTag), onClick = {
                fabOnclick?.invoke()
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }
    }
}
