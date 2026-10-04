package com.costular.atomtasks.ui

import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.search.navgraphs.SearchNavGraph
import com.ramcosta.composedestinations.generated.taskactions.navgraphs.TaskActionsNavGraph
import com.ramcosta.composedestinations.generated.agenda.navgraphs.AgendaNavGraph
import com.ramcosta.composedestinations.generated.detail.navgraphs.TaskDetailNavGraph
import com.ramcosta.composedestinations.generated.onboarding.navgraphs.OnboardingNavGraph
import com.ramcosta.composedestinations.generated.settings.navgraphs.SettingsNavGraph

@NavHostGraph
annotation class MainGraph {
    @ExternalNavGraph<SearchNavGraph>
    @ExternalNavGraph<TaskActionsNavGraph>
    @ExternalNavGraph<SettingsNavGraph>
    @ExternalNavGraph<TaskDetailNavGraph>()
    @ExternalNavGraph<OnboardingNavGraph>()
    @ExternalNavGraph<AgendaNavGraph>(start = true)
    companion object Includes
}
