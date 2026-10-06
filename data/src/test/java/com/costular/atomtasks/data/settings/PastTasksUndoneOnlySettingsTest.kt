package com.costular.atomtasks.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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

class PastTasksUndoneOnlySettingsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun repository(scope: CoroutineScope): SettingsRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        return SettingsRepositoryImpl(SettingsLocalDataSourceImpl(store, Json))
    }

    @Test
    fun `past tasks unfinished-only filter default to disabled`() = runTest {
        assertThat(repository(backgroundScope).observePastTasksUndoneOnlyEnabled().first()).isFalse()
    }

    @Test
    fun `stored flag changes are observable in both directions`() = runTest {
        val settings = repository(backgroundScope)

        settings.observePastTasksUndoneOnlyEnabled().test {
            assertThat(awaitItem()).isFalse()
            settings.setPastTasksUndoneOnlyEnabled(true)
            assertThat(awaitItem()).isTrue()
            settings.setPastTasksUndoneOnlyEnabled(false)
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `flag persists after the data store and repository are recreated`() = runTest {
        val storeJob = Job(backgroundScope.coroutineContext[Job])
        val settings = repository(CoroutineScope(backgroundScope.coroutineContext + storeJob))
        settings.setPastTasksUndoneOnlyEnabled(true)
        storeJob.cancelAndJoin()

        val recreated = repository(backgroundScope)
        assertThat(recreated.observePastTasksUndoneOnlyEnabled().first()).isTrue()
    }
}
