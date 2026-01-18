package com.costular.atomtasks.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.costular.atomtasks.core.testing.ui.ComposeProvider
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class SettingsScreenTest : ComposeProvider {

    @get:Rule
    override val composeTestRule = createComposeRule()

    private val onUpdateAutoforwardTasks: (Boolean) -> Unit = mockk(relaxed = true)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `should show the screen title when the user lands on the screen`() {
        givenSettingsScreen()

        settings {
        }
    }

    @Ignore
    @Test
    fun `should call fast forward callback with true argument when enabling the fast forward switch`() {
        givenSettingsScreen(SettingsState(moveUndoneTasksTomorrowAutomatically = false))

        settings {
            tapOnAutoforwardTasks()

            verify(exactly = 1) { onUpdateAutoforwardTasks(true) }
        }
    }

    @Ignore
    @Test
    fun `should call fast forward callback with false argument when disabling the fast forward switch`() {
        givenSettingsScreen(SettingsState(moveUndoneTasksTomorrowAutomatically = true))

        settings {
            tapOnAutoforwardTasks()

            verify(exactly = 1) { onUpdateAutoforwardTasks(false) }
        }
    }

    @Test
    fun `should enable fast forward switch when setting is enabled`() {
        givenSettingsScreen(SettingsState(moveUndoneTasksTomorrowAutomatically = true))

        settings {
            autoforwardTasksIsEnabled()
        }
    }

    @Test
    fun `should disable fast forward switch when setting is disabled`() {
        givenSettingsScreen(SettingsState(moveUndoneTasksTomorrowAutomatically = false))

        composeTestRule.onRoot().printToLog("Settings")

        settings {
            autoforwardTasksIsDisabled()
        }
    }

    private fun givenSettingsScreen(state: SettingsState = SettingsState.Empty) {
        composeTestRule.setContent {
            SettingsScreen(
                state = state,
                navigator = EmptySettingsNavigator,
                snackbarHostState = androidx.compose.material3.SnackbarHostState(),
                onUpdateAutoforwardTasks = onUpdateAutoforwardTasks,
                onEnableDailyReminder = {},
                onClickDailyReminder = {},
                onBackupLocal = {},
                onRestoreLocal = {},
            )
        }
    }
}
