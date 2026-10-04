package com.costular.atomtasks.data.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalTime
class SettingsRepositoryImplTest {

    lateinit var sut: SettingsRepository

    private val settingsLocalDataSource: SettingsLocalDataSource = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        sut = SettingsRepositoryImpl(settingsLocalDataSource)
    }

    @Test
    fun `should return light theme when local data source stores light as string`() = runTest {
        every { settingsLocalDataSource.observeTheme() } returns flowOf(Theme.LIGHT)

        sut.observeTheme().test {
            assertThat(awaitItem()).isInstanceOf(Theme.Light::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should return dark theme when local data source stores dark as string`() = runTest {
        every { settingsLocalDataSource.observeTheme() } returns flowOf(Theme.DARK)

        sut.observeTheme().test {
            assertThat(awaitItem()).isInstanceOf(Theme.Dark::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should return system theme when local data source stores system as string`() = runTest {
        every { settingsLocalDataSource.observeTheme() } returns flowOf(Theme.SYSTEM)

        sut.observeTheme().test {
            assertThat(awaitItem()).isInstanceOf(Theme.System::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should set dark theme successfully`() = runTest {
        val theme = Theme.Dark

        sut.setTheme(theme)

        coVerify { settingsLocalDataSource.setTheme(Theme.DARK) }
    }

    @Test
    fun `should set light theme successfully`() = runTest {
        val theme = Theme.Light

        sut.setTheme(theme)

        coVerify { settingsLocalDataSource.setTheme(Theme.LIGHT) }
    }

    @Test
    fun `should set system theme successfully`() = runTest {
        val theme = Theme.System

        sut.setTheme(theme)

        coVerify { settingsLocalDataSource.setTheme(Theme.SYSTEM) }
    }

    @Test
    fun `should return true when local data source has move undone tasks enabled`() = runTest {
        givenMoveUndoneTasksEnabled()

        sut.observeMoveUndoneTaskTomorrow().test {
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should return false when local data source has move undone tasks disabled`() = runTest {
        givenMoveUndoneTasksDisabled()

        sut.observeMoveUndoneTaskTomorrow().test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should set move undone tasks enabled with true input`() = runTest {
        val expected = true

        sut.setMoveUndoneTaskTomorrow(expected)

        coVerify { settingsLocalDataSource.setMoveUndoneTaskTomorrow(expected) }
    }

    @Test
    fun `should set move undone tasks disabled with false input`() = runTest {
        val expected = false

        sut.setMoveUndoneTaskTomorrow(expected)

        coVerify { settingsLocalDataSource.setMoveUndoneTaskTomorrow(expected) }
    }

    @Test
    fun `should expose live task list section flag changes`() = runTest {
        val enabled = MutableStateFlow(false)
        every { settingsLocalDataSource.observeTaskListSectionsEnabled() } returns enabled

        sut.observeTaskListSectionsEnabled().test {
            assertThat(awaitItem()).isFalse()
            enabled.value = true
            assertThat(awaitItem()).isTrue()
            enabled.value = false
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should save enabled and disabled task list section flags`() = runTest {
        sut.setTaskListSectionsEnabled(true)
        coVerify { settingsLocalDataSource.setTaskListSectionsEnabled(true) }

        sut.setTaskListSectionsEnabled(false)
        coVerify { settingsLocalDataSource.setTaskListSectionsEnabled(false) }
    }

    private fun givenMoveUndoneTasksEnabled() {
        every {
            settingsLocalDataSource.observeMoveUndoneTaskTomorrow()
        } returns flowOf(true)
    }

    private fun givenMoveUndoneTasksDisabled() {
        every {
            settingsLocalDataSource.observeMoveUndoneTaskTomorrow()
        } returns flowOf(false)
    }
}
