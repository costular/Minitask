package com.costular.atomtasks.core.ui.tasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.testing.ui.resources
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class TaskListSectionsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val pending = TaskToday.copy(id = 1L, name = "Pending task", isDone = false)
    private val done = TaskToday.copy(id = 2L, name = "Done task", isDone = true)

    @Test
    fun givenInterleavedTasksWhenSectionsRenderThenPendingPrecedesCompleted() {
        showList(listOf(done, pending))

        assertBefore(pending.name, done.name)
    }

    @Test
    fun givenPendingTasksWhenSectionsRenderThenPendingHeaderIsVisible() {
        showList(listOf(done, pending))

        composeTestRule.onNodeWithText(header(completed = false, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenCompletedSectionWhenCollapsedThenStateDescriptionIsCollapsed() {
        showList(listOf(pending, done))

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 1))
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                composeTestRule.getString(R.string.task_list_collapsed),
            ))
    }

    @Test
    fun givenCompletedSectionWhenCollapsedThenCompletedTaskIsHidden() {
        showList(listOf(pending, done))

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(done.name).assertDoesNotExist()
    }

    @Test
    fun givenCompletedSectionWhenCollapsedThenHeaderRemainsVisible() {
        showList(listOf(pending, done))

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenCompletedSectionWhenCollapsedThenExpandActionIsAvailable() {
        showList(listOf(pending, done))

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assert(hasExpandAction())
    }

    @Test
    fun givenCollapsedSectionWhenExpandedThenCompletedTaskIsVisible() {
        showList(listOf(pending, done))
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onNodeWithText(done.name).assertIsDisplayed()
    }

    @Test
    fun givenCollapsedSectionWhenStateRestoresThenCompletedTaskStaysHidden() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { ListContent(listOf(pending, done)) }
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        restoration.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithText(done.name).assertDoesNotExist()
    }

    @Test
    fun givenCollapsedSectionWhenStateRestoresThenHeaderStaysVisible() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { ListContent(listOf(pending, done)) }
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        restoration.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenCollapsedSectionWhenStateRestoresThenExpandActionIsAvailable() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { ListContent(listOf(pending, done)) }
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        restoration.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assert(hasExpandAction())
    }

    @Test
    fun givenDisabledSectionsWhenRenderedThenOriginalOrderIsKept() {
        showList(listOf(done, pending), sectionsEnabled = false)

        assertBefore(done.name, pending.name)
    }

    @Test
    fun givenDisabledSectionsWhenRenderedThenPendingHeaderIsHidden() {
        showList(listOf(done, pending), sectionsEnabled = false)

        composeTestRule.onNodeWithText(header(completed = false, count = 1)).assertDoesNotExist()
    }

    @Test
    fun givenDisabledSectionsWhenRenderedThenCompletedHeaderIsHidden() {
        showList(listOf(done, pending), sectionsEnabled = false)

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertDoesNotExist()
    }

    @Test
    fun givenSectionsWhenEnabledAtRuntimeThenHeaderIsVisible() {
        var sectionsEnabled by mutableStateOf(false)
        composeTestRule.setContent {
            ListContent(listOf(pending, done), sectionsEnabled)
        }

        composeTestRule.runOnIdle { sectionsEnabled = true }

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenSectionsEnabledAtRuntimeWhenDraggingAcrossStatusesThenMoveIsBlocked() {
        var sectionsEnabled by mutableStateOf(false)
        var moves = 0
        composeTestRule.setContent {
            ListContent(listOf(pending, done), sectionsEnabled, onMove = { _, _ -> moves++ })
        }
        composeTestRule.runOnIdle { sectionsEnabled = true }

        drag(pending.name, done.name)

        composeTestRule.runOnIdle { assertThat(moves).isEqualTo(0) }
    }

    @Test
    fun givenSectionsWhenDisabledAtRuntimeThenHeaderIsHidden() {
        var sectionsEnabled by mutableStateOf(true)
        composeTestRule.setContent {
            ListContent(listOf(pending, done), sectionsEnabled)
        }

        composeTestRule.runOnIdle { sectionsEnabled = false }

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertDoesNotExist()
    }

    @Test
    fun givenSectionsDisabledAtRuntimeWhenDraggingAcrossStatusesThenMoveIsForwarded() {
        var sectionsEnabled by mutableStateOf(true)
        var moves = 0
        composeTestRule.setContent {
            ListContent(listOf(pending, done), sectionsEnabled, onMove = { _, _ -> moves++ })
        }
        composeTestRule.runOnIdle { sectionsEnabled = false }

        drag(pending.name, done.name)

        composeTestRule.runOnIdle { assertThat(moves).isGreaterThan(0) }
    }

    @Test
    fun givenSectionsDisabledAtRuntimeWhenDraggingAcrossStatusesThenOrderChanges() {
        var sectionsEnabled by mutableStateOf(true)
        var moves = 0
        composeTestRule.setContent {
            ListContent(listOf(pending, done), sectionsEnabled, onMove = { _, _ -> moves++ })
        }
        composeTestRule.runOnIdle { sectionsEnabled = false }

        drag(pending.name, done.name)

        assertBefore(done.name, pending.name)
    }

    @Test
    fun givenNoPendingTasksWhenRenderedThenPendingHeaderShowsZero() {
        showList(listOf(done))

        composeTestRule.onNodeWithText(header(completed = false, count = 0)).assertIsDisplayed()
    }

    @Test
    fun givenNoPendingTasksWhenRenderedThenHelpfulMessageIsVisible() {
        showList(listOf(done))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.task_list_no_pending)).assertIsDisplayed()
    }

    @Test
    fun givenNoPendingTasksWhenRenderedThenCompletedHeaderRemainsVisible() {
        showList(listOf(done))

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenCompletionWhenAnimationIsRunningThenCountWaitsForAnimation() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.onNodeWithText(header(completed = false, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenCompletionWhenAnimationIsRunningThenMembershipWaitsForAnimation() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        assertBefore(pending.name, header(completed = true, count = 1))
    }

    @Test
    fun givenCompletionWhenAnimationFinishesThenCountUpdates() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.mainClock.advanceTimeBy(1_000)

        composeTestRule.onNodeWithText(header(completed = false, count = 0)).assertIsDisplayed()
    }

    @Test
    fun givenCompletionWhenAnimationFinishesThenTaskChangesSection() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.mainClock.advanceTimeBy(1_000)

        assertBefore(header(completed = true, count = 2), pending.name)
    }

    @Test
    fun givenReopeningWhenAnimationIsRunningThenCountWaitsForAnimation() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onAllNodesWithTag("Markable")[1].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.onNodeWithText(header(completed = true, count = 1)).assertIsDisplayed()
    }

    @Test
    fun givenReopeningWhenAnimationIsRunningThenMembershipWaitsForAnimation() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onAllNodesWithTag("Markable")[1].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        assertBefore(header(completed = true, count = 1), done.name)
    }

    @Test
    fun givenReopeningWhenAnimationFinishesThenCountUpdates() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onAllNodesWithTag("Markable")[1].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.mainClock.advanceTimeBy(1_000)

        composeTestRule.onNodeWithText(header(completed = false, count = 2)).assertIsDisplayed()
    }

    @Test
    fun givenReopeningWhenAnimationFinishesThenTaskChangesSection() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onAllNodesWithTag("Markable")[1].performClick()
        composeTestRule.mainClock.advanceTimeBy(80)

        composeTestRule.mainClock.advanceTimeBy(1_000)

        assertBefore(done.name, header(completed = true, count = 0))
    }

    @Test
    fun givenAnimatedCompletionWhenUndoneThenTaskReturnsToPending() {
        showList(listOf(pending, done))
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(1_000)

        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()
        composeTestRule.mainClock.advanceTimeBy(1_000)

        assertBefore(pending.name, header(completed = true, count = 1))
    }

    @Test
    fun givenCollapsedCompletedSectionWhenTaskCompletesThenCountUpdates() {
        showList(listOf(pending, done))
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 2)).assertIsDisplayed()
    }

    @Test
    fun givenCollapsedCompletedSectionWhenTaskCompletesThenTaskIsHidden() {
        showList(listOf(pending, done))
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()

        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()

        composeTestRule.onNodeWithText(pending.name).assertDoesNotExist()
    }

    @Test
    fun givenCompletedTaskInCollapsedSectionWhenExpandedThenTaskIsVisible() {
        showList(listOf(pending, done))
        composeTestRule.onNodeWithText(header(completed = true, count = 1)).performClick()
        composeTestRule.onAllNodesWithTag("Markable")[0].performClick()

        composeTestRule.onNodeWithText(header(completed = true, count = 2)).performClick()

        composeTestRule.onNodeWithText(pending.name).assertIsDisplayed()
    }

    @Test
    fun givenSectionsWhenDraggingAcrossSectionsThenMoveIsNotForwarded() {
        var moves = 0
        composeTestRule.setContent {
            ListContent(listOf(pending, done), onMove = { _, _ -> moves++ })
        }

        drag(pending.name, done.name)

        composeTestRule.runOnIdle { assertThat(moves).isEqualTo(0) }
    }

    @Test
    fun givenSectionsWhenDraggingAcrossSectionsThenCompletionIsNotForwarded() {
        var marks = 0
        composeTestRule.setContent {
            ListContent(listOf(pending, done), onMark = { _, _ -> marks++ })
        }

        drag(pending.name, done.name)

        composeTestRule.runOnIdle { assertThat(marks).isEqualTo(0) }
    }

    @Test
    fun givenSectionsWhenDraggingAcrossSectionsThenOrderIsUnchanged() {
        composeTestRule.setContent {
            ListContent(listOf(pending, done))
        }

        drag(pending.name, done.name)

        assertBefore(pending.name, done.name)
    }

    @Test
    fun givenInterleavedTasksWhenDraggingWithinSectionThenOriginalIndicesAreForwarded() {
        val second = pending.copy(id = 3L, name = "Second pending task")
        val moves = mutableListOf<Pair<ItemPosition, ItemPosition>>()
        composeTestRule.setContent {
            ListContent(listOf(pending, done, second), onMove = { from, to -> moves += from to to })
        }

        drag(pending.name, second.name)

        composeTestRule.runOnIdle {
            assertThat(moves).contains(ItemPosition(0, pending.id) to ItemPosition(2, second.id))
        }
    }

    @Test
    fun givenInterleavedTasksWhenDraggingWithinSectionThenTaskOrderChanges() {
        val second = pending.copy(id = 3L, name = "Second pending task")
        val moves = mutableListOf<Pair<ItemPosition, ItemPosition>>()
        composeTestRule.setContent {
            ListContent(listOf(pending, done, second), onMove = { from, to -> moves += from to to })
        }

        drag(pending.name, second.name)

        assertBefore(second.name, pending.name)
    }
    private fun hasExpandAction(): SemanticsMatcher {
        val actionLabel = composeTestRule.getString(R.string.task_list_expand_completed)
        return SemanticsMatcher("Has expand completed tasks action") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label == actionLabel
        }
    }

    private fun header(completed: Boolean, count: Int): String = composeTestRule.resources.getString(
        R.string.task_list_section_count,
        composeTestRule.getString(if (completed) R.string.task_list_completed else R.string.task_list_pending),
        count,
    )

    private fun showList(tasks: List<Task>, sectionsEnabled: Boolean = true) {
        composeTestRule.setContent { ListContent(tasks, sectionsEnabled) }
        composeTestRule.waitForIdle()
    }

    private fun assertBefore(first: String, second: String) {
        val firstY = composeTestRule.onNodeWithText(first).fetchSemanticsNode().boundsInRoot.top
        val secondY = composeTestRule.onNodeWithText(second).fetchSemanticsNode().boundsInRoot.top
        assertThat(firstY).isLessThan(secondY)
    }

    private fun drag(from: String, to: String) {
        val fromY = composeTestRule.onNodeWithText(from).fetchSemanticsNode().boundsInRoot.center.y
        val toY = composeTestRule.onNodeWithText(to).fetchSemanticsNode().boundsInRoot.center.y
        composeTestRule.onNodeWithText(from).performTouchInput {
            down(center)
            advanceEventTime(600)
            moveBy(Offset(0f, toY - fromY))
            advanceEventTime(200)
            up()
        }
    }

    @Composable
    private fun ListContent(
        initialTasks: List<Task>,
        sectionsEnabled: Boolean = true,
        onMove: (ItemPosition, ItemPosition) -> Unit = { _, _ -> },
        onMark: (Long, Boolean) -> Unit = { _, _ -> },
    ) {
        var tasks by remember { mutableStateOf(initialTasks) }
        AtomTheme {
            TaskList(
                tasks = tasks,
                onClick = {},
                onClickMore = {},
                onDeleteTask = {},
                onMarkTask = { id, isDone ->
                    onMark(id, isDone)
                    tasks = tasks.map { if (it.id == id) it.copy(isDone = isDone) else it }
                },
                onMove = { from, to ->
                    onMove(from, to)
                    tasks = tasks.toMutableList().apply { add(to.index, removeAt(from.index)) }
                },
                onDragStopped = {},
                modifier = Modifier.fillMaxSize(),
                sectionsEnabled = sectionsEnabled,
            )
        }
    }
}
