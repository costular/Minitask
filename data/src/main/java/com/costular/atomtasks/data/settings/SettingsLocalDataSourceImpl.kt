package com.costular.atomtasks.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.costular.atomtasks.data.settings.dailyreminder.DailyReminderDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalTime
import javax.inject.Inject

@Suppress("TooManyFunctions")
class SettingsLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : SettingsLocalDataSource {

    private val preferenceDefaultTab = stringPreferencesKey("default_tab")

    override fun observeDefaultTab(): Flow<String> = dataStore.data.map { preferences ->
        preferences[preferenceDefaultTab] ?: DefaultTab.Agenda.preferenceValue
    }

    override suspend fun setDefaultTab(defaultTab: String) {
        dataStore.edit { settings ->
            settings[preferenceDefaultTab] = defaultTab
        }
    }

    private val preferencePastTasksUndoneOnly = booleanPreferencesKey("past_tasks_undone_only_enabled")

    override fun observePastTasksUndoneOnlyEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[preferencePastTasksUndoneOnly] ?: false
    }

    override suspend fun setPastTasksUndoneOnlyEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[preferencePastTasksUndoneOnly] = isEnabled
        }
    }

    private val preferenceTaskListSections = booleanPreferencesKey("task_list_sections_enabled")

    override fun observeTaskListSectionsEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[preferenceTaskListSections] ?: false
    }

    override suspend fun setTaskListSectionsEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[preferenceTaskListSections] = isEnabled
        }
    }

    private val preferenceTheme = stringPreferencesKey("theme")
    val theme: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[preferenceTheme] ?: Theme.SYSTEM
        }

    private val preferenceMoveUndoneTasksTomorrow = booleanPreferencesKey("tasks_autoforward")
    val moveUndoneTasksTomorrow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[preferenceMoveUndoneTasksTomorrow] ?: DefaultMoveUndoneTasks
    }

    private val preferenceDailyReminder = stringPreferencesKey("daily_reminder")
    private val dailyReminder: Flow<DailyReminderDto> = dataStore.data.map { preferences ->
        preferences[preferenceDailyReminder]?.let<String, DailyReminderDto>(json::decodeFromString)
            ?: return@map DailyReminderDto(true, "08:00")
    }

    override fun observeTheme(): Flow<String> = theme

    override suspend fun setTheme(theme: String) {
        dataStore.edit { settings ->
            settings[preferenceTheme] = theme
        }
    }

    override fun observeMoveUndoneTaskTomorrow(): Flow<Boolean> = moveUndoneTasksTomorrow

    override suspend fun setMoveUndoneTaskTomorrow(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[preferenceMoveUndoneTasksTomorrow] = isEnabled
        }
    }

    override fun getDailyReminder(): Flow<DailyReminderDto> = dailyReminder

    override suspend fun updateDailyReminder(isEnabled: Boolean, time: LocalTime) {
        dataStore.edit { settings ->
            settings[preferenceDailyReminder] =
                json.encodeToString(DailyReminderDto(isEnabled, time.toString()))
        }
    }

    private val preferenceHasUserCreatedTask = booleanPreferencesKey("user_created_task")

    override fun observeHasUserCreatedTask(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[preferenceHasUserCreatedTask] ?: false
    }

    override suspend fun setHasUserCreatedTask(hasCreated: Boolean) {
        dataStore.edit { settings ->
            settings[preferenceHasUserCreatedTask] = hasCreated
        }
    }

    private companion object {
        const val DefaultMoveUndoneTasks = false
    }
}
