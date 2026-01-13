package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface SettingsRepository {
    fun observeTheme(): Flow<Theme>
    suspend fun setTheme(theme: Theme)
    fun observeMoveUndoneTaskTomorrow(): Flow<Boolean>
    suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean)
    fun getDailyReminderConfiguration(): Flow<DailyReminder>
    suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime)

    fun observeHasUserCreatedTask(): Flow<Boolean>
    suspend fun setHasUserCreatedTask(hasCreated: Boolean)
}
