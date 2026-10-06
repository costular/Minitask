package com.costular.atomtasks.data.settings

import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import com.costular.atomtasks.data.settings.dailyreminder.asDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject

@Suppress("TooManyFunctions")
class SettingsRepositoryImpl @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource,
) : SettingsRepository {

    override fun observeDefaultTab(): Flow<DefaultTab> =
        settingsLocalDataSource.observeDefaultTab().map(DefaultTab::fromString)

    override suspend fun setDefaultTab(defaultTab: DefaultTab) {
        settingsLocalDataSource.setDefaultTab(defaultTab.preferenceValue)
    }

    override fun observePastTasksUndoneOnlyEnabled(): Flow<Boolean> =
        settingsLocalDataSource.observePastTasksUndoneOnlyEnabled()

    override suspend fun setPastTasksUndoneOnlyEnabled(isEnabled: Boolean) {
        settingsLocalDataSource.setPastTasksUndoneOnlyEnabled(isEnabled)
    }

    override fun observeTaskListSectionsEnabled(): Flow<Boolean> =
        settingsLocalDataSource.observeTaskListSectionsEnabled()

    override suspend fun setTaskListSectionsEnabled(isEnabled: Boolean) {
        settingsLocalDataSource.setTaskListSectionsEnabled(isEnabled)
    }

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
