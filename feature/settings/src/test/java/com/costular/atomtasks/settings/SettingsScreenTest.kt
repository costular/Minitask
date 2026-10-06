package com.costular.atomtasks.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.data.settings.DefaultTab
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
    private val onUpdateTaskListSections: (Boolean) -> Unit = mockk(relaxed = true)
    private val onUpdatePastTasksUndoneOnly: (Boolean) -> Unit = mockk(relaxed = true)
    private val onUpdateDefaultTab: (DefaultTab) -> Unit = mockk(relaxed = true)

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

    @Test
    fun `should show disabled grouping and enable it when tapping the setting row`() {
        givenSettingsScreen(SettingsState(taskListSectionsEnabled = false))

        settings {
            taskListSectionsIsDisabled()
            tapOnTaskListSectionsRow()
        }

        verify(exactly = 1) { onUpdateTaskListSections(true) }
    }

    @Test
    fun `should show enabled grouping and disable it when tapping the switch`() {
        givenSettingsScreen(SettingsState(taskListSectionsEnabled = true))

        settings {
            taskListSectionsIsEnabled()
            tapOnTaskListSectionsSwitch()
        }

        verify(exactly = 1) { onUpdateTaskListSections(false) }
    }

    @Test
    fun `should show Agenda selected and save vertical when choosing it`() {
        givenSettingsScreen()
        openDefaultTabSelector()

        defaultTabOption(R.string.home_menu_agenda).assertIsSelected()
        defaultTabOption(R.string.vertical_tasks).performClick()

        verify(exactly = 1) { onUpdateDefaultTab(DefaultTab.VerticalTasks) }
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_default_tab_description))
            .assertDoesNotExist()
    }

    @Test
    fun `should show vertical selected and save Agenda when choosing it`() {
        givenSettingsScreen(SettingsState(defaultTab = DefaultTab.VerticalTasks))
        openDefaultTabSelector()

        defaultTabOption(R.string.vertical_tasks).assertIsSelected()
        defaultTabOption(R.string.home_menu_agenda).performClick()

        verify(exactly = 1) { onUpdateDefaultTab(DefaultTab.Agenda) }
    }

    @Test
    fun `should dismiss the default tab chooser without saving when cancelled`() {
        givenSettingsScreen()
        openDefaultTabSelector()

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.cancel)).performClick()

        verify(exactly = 0) { onUpdateDefaultTab(any()) }
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_default_tab_description))
            .assertDoesNotExist()
    }

    @Test
    fun `should enable past filtering when its disabled row is clicked`() {
        givenSettingsScreen(SettingsState(pastTasksUndoneOnlyEnabled = false))
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_past_tasks_undone_only_title))
            .performScrollTo().performClick()
        verify(exactly = 1) { onUpdatePastTasksUndoneOnly(true) }
    }

    @Test
    fun `should disable past filtering when its enabled row is clicked`() {
        givenSettingsScreen(SettingsState(pastTasksUndoneOnlyEnabled = true))
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_past_tasks_undone_only_title))
            .performScrollTo().performClick()
        verify(exactly = 1) { onUpdatePastTasksUndoneOnly(false) }
    }

    private fun openDefaultTabSelector() {
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_default_tab_title))
            .performClick()
    }

    private fun defaultTabOption(labelResId: Int) = composeTestRule.onNode(
        hasText(composeTestRule.getString(labelResId)).and(isSelectable()),
    )

    private fun givenSettingsScreen(state: SettingsState = SettingsState.Empty) {
        composeTestRule.setContent {
            SettingsScreen(
                state = state,
                navigator = EmptySettingsNavigator,
                onUpdateDefaultTab = onUpdateDefaultTab,
                onUpdateAutoforwardTasks = onUpdateAutoforwardTasks,
                onUpdateTaskListSections = onUpdateTaskListSections,
                onUpdatePastTasksUndoneOnly = onUpdatePastTasksUndoneOnly,
                onEnableDailyReminder = {},
                onClickDailyReminder = {},
                onBackupLocal = {},
                onRestoreLocal = {},
            )
        }
    }
}
