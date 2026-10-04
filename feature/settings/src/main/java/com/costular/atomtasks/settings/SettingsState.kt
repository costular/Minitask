package com.costular.atomtasks.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.data.settings.DefaultTab
import android.net.Uri

data class SettingsState(
    val theme: Theme = Theme.System,
    val defaultTab: DefaultTab = DefaultTab.Agenda,
    val moveUndoneTasksTomorrowAutomatically: Boolean = true,
    val taskListSectionsEnabled: Boolean = false,
    val dailyReminder: DailyReminder? = null,
    val isDailyReminderTimePickerOpen: Boolean = false,
    val shouldShowExactAlarmRationale: Boolean = false,
    val isImportConfirmationVisible: Boolean = false,
    val pendingImportUri: Uri? = null,
    val backupProcessState: BackupProcessState = BackupProcessState.Idle,
) {
    companion object {
        val Empty = SettingsState()
    }
}

sealed interface BackupProcessState {
    data object Idle : BackupProcessState
    data object Loading : BackupProcessState
    data class Success(val type: BackupOperationType) : BackupProcessState
    data class Error(val message: String? = null) : BackupProcessState
}

enum class BackupOperationType {
    BACKUP,
    RESTORE,
}
