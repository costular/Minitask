package com.costular.atomtasks.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.ui.graphics.vector.ImageVector
import com.costular.atomtasks.core.ui.R
import com.ramcosta.composedestinations.generated.agenda.destinations.AgendaScreenDestination
import com.ramcosta.composedestinations.generated.verticaltasks.destinations.VerticalTasksScreenDestination
import com.ramcosta.composedestinations.generated.settings.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.generated.agenda.navgraphs.AgendaNavGraph
import com.ramcosta.composedestinations.generated.verticaltasks.navgraphs.VerticalTasksNavGraph
import com.ramcosta.composedestinations.generated.settings.navgraphs.SettingsNavGraph

enum class HomeNavigationDestination(
    val screen: DirectionDestinationSpec,
    val graph: NavGraphSpec,
    @StringRes val contentDescriptionResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes val labelResId: Int,
) {
    Agenda(
        screen = AgendaScreenDestination,
        graph = AgendaNavGraph,
        contentDescriptionResId = R.string.home_menu_agenda,
        icon = Icons.Outlined.ViewCarousel,
        selectedIcon = Icons.Filled.ViewCarousel,
        labelResId = R.string.home_menu_agenda,
    ),
    VerticalTasks(
        screen = VerticalTasksScreenDestination,
        graph = VerticalTasksNavGraph,
        contentDescriptionResId = R.string.vertical_tasks,
        icon = Icons.Outlined.ViewAgenda,
        selectedIcon = Icons.Filled.ViewAgenda,
        labelResId = R.string.vertical_tasks,
    ),
    Settings(
        screen = SettingsScreenDestination,
        graph = SettingsNavGraph,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        labelResId = R.string.home_menu_settings,
        contentDescriptionResId = R.string.home_menu_settings,
    ),
}
