package com.costular.atomtasks.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DefaultTabSettingsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun repository(scope: CoroutineScope): SettingsRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        return SettingsRepositoryImpl(SettingsLocalDataSourceImpl(store, Json))
    }

    @Test
    fun `default tab is Agenda when no preference is stored`() = runTest {
        assertThat(repository(backgroundScope).observeDefaultTab().first()).isEqualTo(DefaultTab.Agenda)
    }

    @Test
    fun `default tab changes are observable in both directions`() = runTest {
        val settings = repository(backgroundScope)
        settings.observeDefaultTab().test {
            assertThat(awaitItem()).isEqualTo(DefaultTab.Agenda)
            settings.setDefaultTab(DefaultTab.VerticalTasks)
            assertThat(awaitItem()).isEqualTo(DefaultTab.VerticalTasks)
            settings.setDefaultTab(DefaultTab.Agenda)
            assertThat(awaitItem()).isEqualTo(DefaultTab.Agenda)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `default tab persists after recreating the data store`() = runTest {
        val storeJob = Job(backgroundScope.coroutineContext[Job])
        val settings = repository(CoroutineScope(backgroundScope.coroutineContext + storeJob))
        settings.setDefaultTab(DefaultTab.VerticalTasks)
        storeJob.cancelAndJoin()

        assertThat(repository(backgroundScope).observeDefaultTab().first())
            .isEqualTo(DefaultTab.VerticalTasks)
    }

    @Test
    fun `unknown stored tab falls back to Agenda`() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        store.edit { it[stringPreferencesKey("default_tab")] = "unknown_tab" }
        val settings = SettingsRepositoryImpl(SettingsLocalDataSourceImpl(store, Json))

        assertThat(settings.observeDefaultTab().first()).isEqualTo(DefaultTab.Agenda)
    }
}
