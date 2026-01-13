package com.costular.atomtasks.settings

import android.content.Context
import android.net.Uri
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
import com.costular.atomtasks.tasks.usecase.AreExactRemindersAvailable
import com.costular.atomtasks.data.backup.ExportBackupUseCase
import com.costular.atomtasks.data.backup.HasDataUseCase
import com.costular.atomtasks.data.backup.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@Suppress("LongParameterList", "TooManyFunctions")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val isAutoforwardTasksSettingEnabledUseCase: IsAutoforwardTasksSettingEnabledUseCase,
    private val setAutoforwardTasksInteractor: SetAutoforwardTasksInteractor,
    private val getDailyReminderUseCase: ObserveDailyReminderUseCase,
    private val updateDailyReminderUseCase: UpdateDailyReminderUseCase,
    private val atomAnalytics: AtomAnalytics,
    private val areExactRemindersAvailable: AreExactRemindersAvailable,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val hasDataUseCase: HasDataUseCase,
    @ApplicationContext private val context: Context,
) : MviViewModel<SettingsState>(SettingsState.Empty) {

    init {
        observeTheme()
        observeAutoforwardTasks()
        observeDailyReminder()
        checkExactAlarmPermission()
    }

    fun backupToLocal(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            setState { copy(backupProcessState = BackupProcessState.Loading) }
            exportBackupUseCase(Unit).fold(
                ifError = { error ->
                    setState { copy(backupProcessState = BackupProcessState.Error(error.toString())) }
                },
                ifResult = { json ->
                    try {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray())
                        }
                        setState { copy(backupProcessState = BackupProcessState.Success(BackupOperationType.BACKUP)) }
                    } catch (e: Exception) {
                        setState { copy(backupProcessState = BackupProcessState.Error(e.message)) }
                    }
                }
            )
        }
    }

    fun requestImportLocal(uri: Uri) {
        viewModelScope.launch {
            if (hasDataUseCase(Unit)) {
                setState { copy(isImportConfirmationVisible = true, pendingImportUri = uri) }
            } else {
                importFromLocal(uri)
            }
        }
    }

    fun confirmImport() {
        state.value.pendingImportUri?.let { importFromLocal(it) }
        setState { copy(isImportConfirmationVisible = false, pendingImportUri = null) }
    }

    fun cancelImport() {
        setState { copy(isImportConfirmationVisible = false, pendingImportUri = null) }
    }

    private fun importFromLocal(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            setState { copy(backupProcessState = BackupProcessState.Loading) }
            try {
                val json = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                if (json != null) {
                    importBackupUseCase(json).fold(
                        ifError = { error ->
                            setState { copy(backupProcessState = BackupProcessState.Error(error.toString())) }
                        },
                        ifResult = {
                            setState {
                                copy(
                                    backupProcessState = BackupProcessState.Success(
                                        BackupOperationType.RESTORE
                                    )
                                )
                            }
                        }

                    )
                } else {
                    setState { copy(backupProcessState = BackupProcessState.Error("Could not read file")) }
                }
            } catch (e: Exception) {
                setState { copy(backupProcessState = BackupProcessState.Error(e.message)) }
            }
        }
    }

    fun dismissBackupResult() {
        setState { copy(backupProcessState = BackupProcessState.Idle) }
    }

    private fun checkExactAlarmPermission() {
        viewModelScope.launch {
            val isAvailable = areExactRemindersAvailable(Unit)
            setState {
                copy(
                    shouldShowExactAlarmRationale = !isAvailable &&
                            state.value.dailyReminder?.isEnabled == true
                )
            }
        }
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
            if (isEnabled) {
                val isAvailable = areExactRemindersAvailable(Unit)
                if (!isAvailable) {
                    setState { copy(shouldShowExactAlarmRationale = true) }
                    return@launch
                }
            }

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
            val isAvailable = areExactRemindersAvailable(Unit)
            if (!isAvailable) {
                setState { copy(shouldShowExactAlarmRationale = true) }
            }

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
            val isAvailable = areExactRemindersAvailable(Unit)
            if (!isAvailable) {
                setState { copy(shouldShowExactAlarmRationale = true) }
                return@launch
            }
            setState { copy(isDailyReminderTimePickerOpen = true) }
        }
    }

    fun dismissDailyReminderTimePicker() {
        viewModelScope.launch {
            setState { copy(isDailyReminderTimePickerOpen = false) }
        }
    }

    fun dismissExactAlarmRationale() {
        setState { copy(shouldShowExactAlarmRationale = false) }
    }

    fun exactAlarmPermissionChanged() {
        // Re-check permission logic if needed or just dismiss
        viewModelScope.launch {
            val isAvailable = areExactRemindersAvailable(Unit)
            if (isAvailable) {
                setState { copy(shouldShowExactAlarmRationale = false) }
            }
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
