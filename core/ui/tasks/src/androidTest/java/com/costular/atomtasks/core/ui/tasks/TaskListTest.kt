package com.costular.atomtasks.core.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.costular.atomtasks.core.testing.ui.AndroidTest
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.removal.RemoveTaskDialog
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.LocalDate
import org.junit.Test

@HiltAndroidTest
class TaskListTest : AndroidTest() {

    @Test
    fun shouldAllowSwipingAgain_whenDeleteDialogIsCancelled() {
        var deleteRequests = 0
        val task = Task(
            id = 1L,
            name = "Task pending delete",
            createdAt = LocalDate.now(),
            day = LocalDate.now(),
            reminder = null,
            isDone = false,
            position = 0,
            isRecurring = false,
            recurrenceEndDate = null,
            recurrenceType = null,
            parentId = null,
        )
        val deleteMessage = composeTestRule.getString(com.costular.atomtasks.core.ui.R.string.remove_task_message)
        val cancel = composeTestRule.getString(com.costular.atomtasks.core.ui.R.string.cancel)

        composeTestRule.setContent {
            var showDeleteConfirmation by mutableStateOf(false)

            if (showDeleteConfirmation) {
                RemoveTaskDialog(
                    onAccept = {},
                    onCancel = { showDeleteConfirmation = false },
                )
            }

            TaskList(
                tasks = listOf(task),
                onClick = {},
                onClickMore = {},
                onDeleteTask = {
                    deleteRequests++
                    showDeleteConfirmation = true
                },
                onMarkTask = { _, _ -> },
                onMove = { _, _ -> },
                onDragStopped = {},
            )
        }

        composeTestRule.onNodeWithText(task.name)
            .performTouchInput { swipeLeft() }

        composeTestRule.onNodeWithText(deleteMessage)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(cancel)
            .performClick()

        composeTestRule.onAllNodesWithText(deleteMessage)
            .assertCountEquals(0)

        composeTestRule.onNodeWithText(task.name)
            .performTouchInput { swipeLeft() }

        composeTestRule.onNodeWithText(deleteMessage)
            .assertIsDisplayed()

        composeTestRule.runOnIdle {
            assertThat(deleteRequests).isEqualTo(2)
        }
    }
}
