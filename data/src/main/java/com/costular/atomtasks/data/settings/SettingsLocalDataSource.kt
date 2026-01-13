package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminderDto
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface SettingsLocalDataSource {
    fun observeTheme(): Flow<String>
    suspend fun setTheme(theme: String)
        fun observeMoveUndoneTaskTomorrow(): Flow<Boolean>
        suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean)
    
                fun getDailyReminder(): Flow<DailyReminderDto>
                suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime)
            
                fun observeHasUserCreatedTask(): Flow<Boolean>
                suspend fun setHasUserCreatedTask(hasCreated: Boolean)
            }
