package com.costular.atomtasks.settings

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.costular.atomtasks.core.testing.ui.ComposeProvider
import com.costular.atomtasks.core.testing.ui.Robot
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R

fun ComposeProvider.settings(func: SettingsRobot.() -> Unit) =
    SettingsRobot(composeTestRule).apply(func)

class SettingsRobot(composeTestRule: ComposeTestRule) : Robot(composeTestRule) {

    private val settingsTitle by lazy {
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings))
    }
    private val autoForwardSwitch by lazy {
        composeTestRule.onNode(
            hasParent(
                hasText(
                    composeTestRule.getString(R.string.settings_tasks_autoforward_tasks_title),
                ),
            ).and(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
            ),
        )
    }

    private val taskListSectionsRow by lazy {
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.settings_tasks_sections_title))
    }
    private val taskListSectionsSwitch by lazy {
        composeTestRule.onNode(
            hasParent(hasText(composeTestRule.getString(R.string.settings_tasks_sections_title)))
                .and(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)),
        )
    }

    init {
        settingsTitle.assertIsDisplayed()
    }

    fun tapOnAutoforwardTasks() {
        autoForwardSwitch.performClick()
    }

    fun autoforwardTasksIsEnabled() {
        autoForwardSwitch.assertIsOn()
    }

    fun autoforwardTasksIsDisabled() {
        autoForwardSwitch.assertIsOff()
    }

    fun tapOnTaskListSectionsRow() {
        taskListSectionsRow.performScrollTo().performClick()
    }

    fun tapOnTaskListSectionsSwitch() {
        taskListSectionsSwitch.performScrollTo().performClick()
    }

    fun taskListSectionsIsEnabled() {
        taskListSectionsSwitch.assertIsOn()
    }

    fun taskListSectionsIsDisabled() {
        taskListSectionsSwitch.assertIsOff()
    }
}
