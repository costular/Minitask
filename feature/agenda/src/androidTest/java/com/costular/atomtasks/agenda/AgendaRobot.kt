package com.costular.atomtasks.agenda

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.costular.atomtasks.core.testing.ui.ComposeProvider
import com.costular.atomtasks.core.testing.ui.Robot
import com.costular.atomtasks.core.testing.ui.getString

fun ComposeProvider.agenda(func: AgendaRobot.() -> Unit) = AgendaRobot(composeTestRule).apply(func)

class AgendaRobot(composeTestRule: ComposeTestRule) : Robot(composeTestRule) {

    private val taskList get() = composeTestRule.onNodeWithTag("AgendaTaskList")
    private val createTask get() = composeTestRule.onNodeWithTag("AgendaCreateTask")

    private val goToday
        get() = composeTestRule.onNodeWithContentDescription(
            composeTestRule.getString(
                com.costular.atomtasks.core.ui.R.string.today,
            ),
        )

    fun assertDayText(text: String) {
        composeTestRule.onNodeWithText(text)
    }

    fun goToday() {
        goToday.performClick()
    }

    infix fun openCreateTask(func: CreateTaskRobot.() -> Unit): CreateTaskRobot {
        createTask
            .assertIsDisplayed()
            .performClick()

        return CreateTaskRobot(composeTestRule).apply(func)
    }

    fun clickOnTask(name: String, func: TaskActionRobot.() -> Unit): TaskActionRobot {
        composeTestRule.onNode(hasText(name))
            .assert(hasAnyAncestor(hasTestTag("AgendaTaskList")))
            .performClick()

        return TaskActionRobot(composeTestRule).apply(func)
    }

    fun taskHasText(text: String) {
        composeTestRule.onNodeWithText(text)
            .assert(hasAnyAncestor(hasTestTag("AgendaTaskList")))
            .assertIsDisplayed()
    }

    fun taskIsDone(taskName: String, isDone: Boolean) {
        val checkbox = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
        composeTestRule.onNode(checkbox)
            .assert(hasParent(hasText(taskName)))
            .run {
                if (isDone) {
                    assertIsSelected()
                } else {
                    assertIsNotSelected()
                }
            }
    }

    fun toggleTask(taskName: String) {
        val checkbox = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
        composeTestRule.onNode(checkbox)
            .assert(hasParent(hasText(taskName)))
            .performClick()
    }

    private fun taskAtIndex(index: Int): SemanticsNodeInteraction {
        return taskList.onChildAt(index)
    }

    fun moveTaskUp(index: Int) {
        taskAtIndex(index).performTouchInput {
            longClick(durationMillis = 3_000)
            swipeUp(endY = -20f)
        }
    }

    // TODO: remove task

    // TODO: mark task as donde/undone via task action
}
