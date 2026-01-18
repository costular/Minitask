package com.costular.atomtasks.settings

import app.cash.turbine.test
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.testing.MviViewModelTest
import com.costular.atomtasks.data.settings.GetThemeUseCase
import com.costular.atomtasks.data.settings.IsAutoforwardTasksSettingEnabledUseCase
import com.costular.atomtasks.data.settings.SetAutoforwardTasksInteractor
import com.costular.atomtasks.data.settings.SetThemeUseCase
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.data.settings.dailyreminder.ObserveDailyReminderUseCase
import com.costular.atomtasks.data.settings.dailyreminder.UpdateDailyReminderUseCase
import com.costular.atomtasks.tasks.usecase.AreExactRemindersAvailable
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

import com.costular.atomtasks.data.backup.ExportBackupUseCase
import com.costular.atomtasks.data.backup.HasDataUseCase
import com.costular.atomtasks.data.backup.ImportBackupUseCase
import android.content.Context

@ExperimentalTime
class SettingsViewModelTest : MviViewModelTest() {

    lateinit var sut: SettingsViewModel

    private val getThemeUseCase: GetThemeUseCase = mockk(relaxed = true)
    private val setThemeUseCase: SetThemeUseCase = mockk(relaxed = true)
    private val isAutoforwardTasksInteractor: IsAutoforwardTasksSettingEnabledUseCase =
        mockk(relaxed = true)
    private val setAutoforwardTasksInteractor: SetAutoforwardTasksInteractor = mockk(relaxed = true)
    private val atomAnalytics: AtomAnalytics = mockk(relaxed = true)
    private val getDailyReminderUseCase: ObserveDailyReminderUseCase = mockk(relaxed = true)
    private val updateDailyReminder: UpdateDailyReminderUseCase = mockk(relaxed = true)
    private val areExactRemindersAvailable: AreExactRemindersAvailable = mockk(relaxed = true)
    private val exportBackupUseCase: ExportBackupUseCase = mockk(relaxed = true)
    private val importBackupUseCase: ImportBackupUseCase = mockk(relaxed = true)
    private val hasDataUseCase: HasDataUseCase = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        initialize()
    }

    private fun initialize() {
        coEvery { areExactRemindersAvailable(Unit) } returns true
        sut = SettingsViewModel(
            getThemeUseCase = getThemeUseCase,
            setThemeUseCase = setThemeUseCase,
            isAutoforwardTasksSettingEnabledUseCase = isAutoforwardTasksInteractor,
            setAutoforwardTasksInteractor = setAutoforwardTasksInteractor,
            atomAnalytics = atomAnalytics,
            getDailyReminderUseCase = getDailyReminderUseCase,
            updateDailyReminderUseCase = updateDailyReminder,
            areExactRemindersAvailable = areExactRemindersAvailable,
            exportBackupUseCase = exportBackupUseCase,
            importBackupUseCase = importBackupUseCase,
            hasDataUseCase = hasDataUseCase,
            context = context,
        )
    }

    @Test
    fun `should expose light theme in state when land on screen given light theme is set`() =
        runTest {
            val theme = Theme.Light
            coEvery { getThemeUseCase.flow } returns flowOf(theme)

            initialize()

            sut.state.test {
                assertThat(expectMostRecentItem().theme).isEqualTo(theme)
            }
        }

    @Test
    fun `should expose dark theme in state when land on screen given dark theme is set`() =
        runTest {
            val theme = Theme.Dark
            coEvery { getThemeUseCase.flow } returns flowOf(theme)

            initialize()

            sut.state.test {
                assertThat(expectMostRecentItem().theme).isEqualTo(theme)
            }
        }

    @Test
    fun `should update theme when select theme`() = runTest {
        val theme = Theme.Light

        sut.setTheme(theme)

        coVerify(exactly = 1) { setThemeUseCase(SetThemeUseCase.Params(theme)) }
    }

    @Test
    fun `Should expose true when land on screen given autoforward tasks is enabled`() =
        runTest {
            givenAutoforward(true)

            sut.state.test {
                assertThat(expectMostRecentItem().moveUndoneTasksTomorrowAutomatically).isTrue()
            }
        }

    @Test
    fun `should update autoforward setting when set the new value`() = runTest {
        val isEnabled = false

        sut.setAutoforwardTasksEnabled(isEnabled)

        coVerify(exactly = 1) {
            setAutoforwardTasksInteractor(SetAutoforwardTasksInteractor.Params(isEnabled))
        }
    }

    private fun givenAutoforward(isEnabled: Boolean) {
        coEvery { isAutoforwardTasksInteractor.invoke(Unit) } returns flowOf(isEnabled)
    }
}
