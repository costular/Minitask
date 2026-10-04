package com.costular.atomtasks.ui.home

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.data.settings.GetThemeUseCase
import com.costular.atomtasks.data.settings.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    autoforwardManager: AutoforwardManager,
    recurrenceScheduler: RecurrenceScheduler,
) : MviViewModel<AppState>(AppState.Empty) {

    init {
        getTheme()
        recurrenceScheduler.initialize()
        viewModelScope.launch { autoforwardManager.scheduleOrCancelAutoforwardTasks() }
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
) {
    companion object {
        val Empty = AppState()
    }
}
