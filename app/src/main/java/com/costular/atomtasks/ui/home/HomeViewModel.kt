package com.costular.atomtasks.ui.home

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.data.settings.GetThemeUseCase
import com.costular.atomtasks.data.settings.DefaultTab
import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.data.settings.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@HiltViewModel
class AppViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val settingsRepository: SettingsRepository,
    autoforwardManager: AutoforwardManager,
    recurrenceScheduler: RecurrenceScheduler,
) : MviViewModel<AppState>(AppState.Empty) {

    init {
        getTheme()
        loadDefaultTab()
        recurrenceScheduler.initialize()
        viewModelScope.launch { autoforwardManager.scheduleOrCancelAutoforwardTasks() }
    }

    private fun loadDefaultTab() {
        viewModelScope.launch {
            // Keep the start graph stable while this activity is alive, including rotations.
            val defaultTab = settingsRepository.observeDefaultTab().first()
            setState { copy(defaultTab = defaultTab) }
        }
    }

    private fun getTheme() {
        viewModelScope.launch {
            getThemeUseCase(Unit)
            getThemeUseCase.flow
                .collect { theme ->
                    setState { copy(theme = theme) }
                }
        }
    }
}

data class AppState(
    val theme: Theme = Theme.System,
    val defaultTab: DefaultTab? = null,
) {
    companion object {
        val Empty = AppState()
    }
}
