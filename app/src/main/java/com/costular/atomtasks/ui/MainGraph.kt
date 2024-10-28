package com.costular.atomtasks.ui

import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.agenda.navgraphs.AgendaNavGraph
import com.ramcosta.composedestinations.generated.detail.navgraphs.TaskDetailNavGraph
import com.ramcosta.composedestinations.generated.onboarding.navgraphs.OnboardingNavGraph
import com.ramcosta.composedestinations.generated.settings.navgraphs.SettingsNavGraph

@NavHostGraph
annotation class MainGraph {
    @ExternalNavGraph<SettingsNavGraph>
    @ExternalNavGraph<TaskDetailNavGraph>()
    @ExternalNavGraph<OnboardingNavGraph>()
    @ExternalNavGraph<AgendaNavGraph>(start = true)
    companion object Includes
}
