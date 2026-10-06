package com.costular.atomtasks.completedtasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.costular.atomtasks.core.testing.ui.AndroidTest
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

@HiltAndroidTest
class CompletedTasksScreenTest : AndroidTest() {
    private val day = LocalDate.now()
    private val task = TaskToday.copy(id = 1, name = "Finished task", day = day, isDone = true)
    private val heading = SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit)

    @Test
    fun emptyStateHasNoDayHeadingsAndBackWorks() {
        var backs = 0
        showScreen(mutableStateOf(CompletedTasksState.Success(persistentListOf())), onBack = { backs++ })
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.completed_tasks_empty)).assertIsDisplayed()
        composeTestRule.onNode(heading).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.navigate_back)).performClick()
        assertThat(backs).isEqualTo(1)
    }

    @Test
    fun failedStateOffersRetry() {
        var retries = 0
        showScreen(mutableStateOf(CompletedTasksState.Failure), onRetry = { retries++ })
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.tasks_load_error)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.tasks_retry)).performClick()
        assertThat(retries).isEqualTo(1)
    }

    @Test
    fun tasksHaveDayHeadingsAndTheirUsualActions() {
        var opened: Long? = null
        var more: Long? = null
        var mark: Pair<Long, Boolean>? = null
        showScreen(
            state = mutableStateOf(CompletedTasksState.Success(persistentListOf(task))),
            onOpenTask = { opened = it.id },
            onMore = { more = it.id },
            onMark = { id, done -> mark = id to done },
        )
        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_today)) and heading).assertIsDisplayed()
        composeTestRule.onNodeWithText(task.name).performClick()
        assertThat(opened).isEqualTo(task.id)
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.task_more_actions)).performClick()
        assertThat(more).isEqualTo(task.id)
        composeTestRule.onNodeWithTag("Markable", useUnmergedTree = true).performClick()
        assertThat(mark).isEqualTo(task.id to false)
    }

    @Test
    fun removingTheLastTaskForADayRemovesItsHeading() {
        val yesterdayTask = task.copy(id = 2, day = day.minusDays(1), name = "Older finished task")
        val state = mutableStateOf<CompletedTasksState>(CompletedTasksState.Success(persistentListOf(task, yesterdayTask)))
        showScreen(state)
        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_yesterday)) and heading).assertIsDisplayed()
        composeTestRule.runOnIdle { state.value = CompletedTasksState.Success(persistentListOf(task)) }
        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_yesterday)) and heading).assertDoesNotExist()
    }

    @Test
    fun loadingStateHasNoEmptyMessageOrTasks() {
        showScreen(mutableStateOf(CompletedTasksState.Loading))
        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.completed_tasks_empty)).assertDoesNotExist()
        composeTestRule.onNodeWithText(task.name).assertDoesNotExist()
    }

    private fun showScreen(
        state: State<CompletedTasksState>,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpenTask: (Task) -> Unit = {},
        onMore: (Task) -> Unit = {},
        onMark: (Long, Boolean) -> Unit = { _, _ -> },
    ) {
        composeTestRule.setContent {
            AtomTheme {
                CompletedTasksScreen(
                    state = state.value,
                    onBack = onBack,
                    onRetry = onRetry,
                    onOpenTask = onOpenTask,
                    onMore = onMore,
                    onDelete = {},
                    onMark = onMark,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
