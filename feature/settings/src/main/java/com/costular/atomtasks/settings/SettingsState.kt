package com.costular.atomtasks.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import com.costular.atomtasks.data.settings.Theme
import java.time.LocalTime

private const val Nine = 21

data class SettingsState(
    val theme: Theme = Theme.System,
    val moveUndoneTasksTomorrowAutomatically: Boolean = true,
    val dailyReminder: DailyReminder? = null,
    val isDailyReminderTimePickerOpen: Boolean = false,
    val shouldShowExactAlarmRationale: Boolean = false,
) {
    companion object {
        val Empty = SettingsState()
    }
}
