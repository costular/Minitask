package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Suppress("TooManyFunctions")
interface SettingsRepository {
    fun observeDefaultTab(): Flow<DefaultTab>
    suspend fun setDefaultTab(defaultTab: DefaultTab)

    fun observePastTasksUndoneOnlyEnabled(): Flow<Boolean>
    suspend fun setPastTasksUndoneOnlyEnabled(isEnabled: Boolean)

    fun observeTaskListSectionsEnabled(): Flow<Boolean>
    suspend fun setTaskListSectionsEnabled(isEnabled: Boolean)

    fun observeTheme(): Flow<Theme>
    suspend fun setTheme(theme: Theme)
    fun observeMoveUndoneTaskTomorrow(): Flow<Boolean>
    suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean)
    fun getDailyReminderConfiguration(): Flow<DailyReminder>
    suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime)

    fun observeHasUserCreatedTask(): Flow<Boolean>
    suspend fun setHasUserCreatedTask(hasCreated: Boolean)
}
