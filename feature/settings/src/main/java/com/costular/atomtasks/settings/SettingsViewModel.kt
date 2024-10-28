package com.costular.atomtasks.settings

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.usecase.EmptyParams
import com.costular.atomtasks.data.settings.GetThemeUseCase
import com.costular.atomtasks.data.settings.IsAutoforwardTasksSettingEnabledUseCase
import com.costular.atomtasks.data.settings.SetAutoforwardTasksInteractor
import com.costular.atomtasks.data.settings.SetThemeUseCase
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.settings.analytics.SettingsChangeAutoforward
import com.costular.atomtasks.settings.analytics.SettingsChangeTheme
import com.costular.atomtasks.core.usecase.invoke
import com.costular.atomtasks.data.settings.dailyreminder.ObserveDailyReminderUseCase
import com.costular.atomtasks.data.settings.dailyreminder.UpdateDailyReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalTime

@Suppress("LongParameterList")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val isAutoforwardTasksSettingEnabledUseCase: IsAutoforwardTasksSettingEnabledUseCase,
    private val setAutoforwardTasksInteractor: SetAutoforwardTasksInteractor,
    private val getDailyReminderUseCase: ObserveDailyReminderUseCase,
    private val updateDailyReminderUseCase: UpdateDailyReminderUseCase,
    private val atomAnalytics: AtomAnalytics,
) : MviViewModel<SettingsState>(SettingsState.Empty) {

    init {
        observeTheme()
        observeAutoforwardTasks()
        observeDailyReminder()
    }

    private fun observeAutoforwardTasks() {
        viewModelScope.launch {
            isAutoforwardTasksSettingEnabledUseCase()
                .collectLatest {
                    setState { copy(moveUndoneTasksTomorrowAutomatically = it) }
                }
        }
    }

    private fun observeDailyReminder() {
        viewModelScope.launch {
            getDailyReminderUseCase.invoke(EmptyParams)
                .collectLatest { dailyReminder ->
                    setState { copy(dailyReminder = dailyReminder) }
                }
        }
    }

    fun updateDailyReminder(isEnabled: Boolean) {
        viewModelScope.launch {
            val dailyReminder = state.value.dailyReminder ?: return@launch

            updateDailyReminderUseCase(
                UpdateDailyReminderUseCase.Params(
                    isEnabled = isEnabled,
                    time = dailyReminder.time!!,
                )
            )
        }
    }

    fun updateDailyReminderTime(time: LocalTime) {
        dismissDailyReminderTimePicker()
        viewModelScope.launch {
            val dailyReminder = state.value.dailyReminder ?: return@launch

            updateDailyReminderUseCase(
                UpdateDailyReminderUseCase.Params(
                    isEnabled = dailyReminder.isEnabled,
                    time = time,
                )
            )
        }
    }

    fun clickOnDailyReminderTimePicker() {
        viewModelScope.launch {
            setState { copy(isDailyReminderTimePickerOpen = true) }
        }
    }

    fun dismissDailyReminderTimePicker() {
        viewModelScope.launch {
            setState { copy(isDailyReminderTimePickerOpen = false) }
        }
    }

    fun setAutoforwardTasksEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setAutoforwardTasksInteractor(SetAutoforwardTasksInteractor.Params(isEnabled)).collect()
        }

        atomAnalytics.track(SettingsChangeAutoforward(isEnabled))
    }

    private fun observeTheme() {
        viewModelScope.launch {
            getThemeUseCase(Unit)
            getThemeUseCase.flow
                .collectLatest { theme ->
                    setState { copy(theme = theme) }
                }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            setThemeUseCase(SetThemeUseCase.Params(theme)).collect()
        }
        atomAnalytics.track(SettingsChangeTheme(theme.asString()))
    }
}
