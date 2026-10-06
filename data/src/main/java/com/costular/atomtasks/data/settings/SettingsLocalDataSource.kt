package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminderDto
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface SettingsLocalDataSource {
    fun observeDefaultTab(): Flow<String>
    suspend fun setDefaultTab(defaultTab: String)

    fun observePastTasksUndoneOnlyEnabled(): Flow<Boolean>
    suspend fun setPastTasksUndoneOnlyEnabled(isEnabled: Boolean)

    fun observeTaskListSectionsEnabled(): Flow<Boolean>
    suspend fun setTaskListSectionsEnabled(isEnabled: Boolean)

    fun observeTheme(): Flow<String>
    suspend fun setTheme(theme: String)
    fun observeMoveUndoneTaskTomorrow(): Flow<Boolean>
    suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean)
    fun getDailyReminder(): Flow<DailyReminderDto>
    suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime)

    fun observeHasUserCreatedTask(): Flow<Boolean>
    suspend fun setHasUserCreatedTask(hasCreated: Boolean)
}
