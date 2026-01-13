package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import com.costular.atomtasks.data.settings.dailyreminder.asDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource,
) : SettingsRepository {

    override fun observeTheme(): Flow<Theme> =
        settingsLocalDataSource.observeTheme().map { Theme.fromString(it) }

    override suspend fun setTheme(theme: Theme) {
        settingsLocalDataSource.setTheme(theme.asString())
    }

    override fun observeMoveUndoneTaskTomorrow(): Flow<Boolean> =
        settingsLocalDataSource.observeMoveUndoneTaskTomorrow()

    override suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean) {
        settingsLocalDataSource.setMoveUndoneTaskTomorrow(isEnabled)
    }

    override fun getDailyReminderConfiguration(): Flow<DailyReminder> =
        settingsLocalDataSource.getDailyReminder().map { it.asDomain() }

    override suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime) {
        settingsLocalDataSource.updateDailyReminder(isEnabled, time)
    }

    override fun observeHasUserCreatedTask(): Flow<Boolean> =
        settingsLocalDataSource.observeHasUserCreatedTask()

    override suspend fun setHasUserCreatedTask(hasCreated: Boolean) {
        settingsLocalDataSource.setHasUserCreatedTask(hasCreated)
    }
}
