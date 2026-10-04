package com.costular.atomtasks.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
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
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

@HiltAndroidTest
class SearchScreenTest : AndroidTest() {
    private val task = TaskToday.copy(id = 47, name = "Gym session", isDone = false)
    private val today = LocalDate.now()



    @Test
    fun givenBlankIdleQueryWhenRenderedThenNoResultsMessageIsHidden() {
        showScreen(SearchState(""))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertDoesNotExist()
    }


    @Test
    fun givenShortIdleQueryWhenRenderedThenNoResultsMessageIsHidden() {
        showScreen(SearchState("g"))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertDoesNotExist()
    }

    @Test
    fun givenLoadingQueryWhenRenderedThenProgressIsVisible() {
        showScreen(SearchState("gym", SearchResults.Loading))

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun givenLoadingQueryWhenRenderedThenNoResultsMessageIsHidden() {
        showScreen(SearchState("gym", SearchResults.Loading))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertDoesNotExist()
    }

    @Test
    fun givenSearchWhenRenderedThenInputHasFocus() {
        showScreen()

        composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun givenSearchInputWhenTextEnteredThenQueryCallbackReceivesText() {
        var query = ""
        showScreen(onQueryChange = { query = it })

        composeTestRule.onNode(hasSetTextAction()).performTextInput("gym")

        assertThat(query).isEqualTo("gym")
    }

    @Test
    fun givenPopulatedSearchWhenClearedThenQueryIsEmpty() {
        var query = "gym"
        showScreen(SearchState(query), onQueryChange = { query = it })

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.search_clear)).performClick()

        assertThat(query).isEmpty()
    }

    @Test
    fun givenPopulatedSearchWhenClearedThenInputKeepsFocus() {
        var state by mutableStateOf(SearchState("gym"))
        composeTestRule.setContent {
            AtomTheme {
                SearchContent(state = state, onQueryChange = { state = state.copy(query = it) })
            }
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.search_clear)).performClick()

        composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun givenPopulatedSearchWhenClearedThenNoResultsMessageIsHidden() {
        var state by mutableStateOf(SearchState("gym"))
        composeTestRule.setContent {
            AtomTheme {
                SearchContent(state = state, onQueryChange = { state = state.copy(query = it) })
            }
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.search_clear)).performClick()

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertDoesNotExist()
    }

    @Test
    fun givenEmptyResultsWhenRenderedThenNoResultsMessageIsVisible() {
        showScreen(SearchState("gym", SearchResults.Success(persistentListOf())))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertIsDisplayed()
    }

    @Test
    fun givenSearchWhenBackClickedThenBackCallbackRuns() {
        var backs = 0
        showScreen(onBack = { backs++ })

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.navigate_back)).performClick()

        assertThat(backs).isEqualTo(1)
    }

    @Test
    fun givenFailureWhenRenderedThenErrorMessageIsVisible() {
        showScreen(SearchState("gym", SearchResults.Failure))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.tasks_load_error)).assertIsDisplayed()
    }

    @Test
    fun givenFailureWhenRetryClickedThenRetryCallbackRuns() {
        var retries = 0
        showScreen(SearchState("gym", SearchResults.Failure), onRetry = { retries++ })

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.tasks_retry)).performClick()

        assertThat(retries).isEqualTo(1)
    }

    @Test
    fun givenResultWhenClickedThenTaskOpens() {
        var opened: Long? = null
        showScreen(resultState(listOf(task)), onOpenTask = { opened = it.id })

        composeTestRule.onNodeWithText(task.name).performClick()

        assertThat(opened).isEqualTo(task.id)
    }

    @Test
    fun givenResultWhenMarkedThenCompletionIsForwarded() {
        var marked: Pair<Long, Boolean>? = null
        showScreen(resultState(listOf(task)), onMark = { id, done -> marked = id to done })

        composeTestRule.onNodeWithTag("Markable", useUnmergedTree = true).performClick()

        assertThat(marked).isEqualTo(task.id to true)
    }

    @Test
    fun givenResultWhenMenuClickedThenTaskMenuOpens() {
        var menu: Long? = null
        showScreen(resultState(listOf(task)), onMore = { menu = it.id })

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.task_more_actions)).performClick()

        assertThat(menu).isEqualTo(task.id)
    }

    @Test
    fun givenResultWhenSwipedThenDeletionIsForwarded() {
        var deleted: Long? = null
        showScreen(resultState(listOf(task)), onDelete = { deleted = it.id })

        composeTestRule.onNodeWithText(task.name).performTouchInput { swipeLeft() }

        composeTestRule.runOnIdle { assertThat(deleted).isEqualTo(task.id) }
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenTodayHasOneHeading() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        composeTestRule.onAllNodes(hasText(composeTestRule.getString(R.string.day_today)) and heading)
            .assertCountEquals(1)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenTomorrowHasOneHeading() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        composeTestRule.onAllNodes(hasText(composeTestRule.getString(R.string.day_tomorrow)) and heading)
            .assertCountEquals(1)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenFirstTaskTextHasNoDate() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        composeTestRule.onNodeWithText(first.name).assertTextEquals(first.name)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenSecondTaskTextHasNoDate() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        composeTestRule.onNodeWithText(second.name).assertTextEquals(second.name)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenThirdTaskTextHasNoDate() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        composeTestRule.onNodeWithText(third.name).assertTextEquals(third.name)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenTomorrowHeadingPrecedesItsTask() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        assertBefore(composeTestRule.getString(R.string.day_tomorrow), first.name)
    }

    @Test
    fun givenGroupedResultsWhenRenderedThenTodayHeadingFollowsTomorrowTasks() {
        val first = task.copy(id = 1, name = "Tomorrow gym", day = today.plusDays(1), reminder = null, recurrenceType = null)
        val second = first.copy(id = 2, name = "Today gym", day = today)
        val third = second.copy(id = 3, name = "Another gym task")

        showScreen(resultState(listOf(first, second, third)))

        assertBefore(first.name, composeTestRule.getString(R.string.day_today))
    }

    @Test
    fun givenLongResultsWhenScrollingTomorrowTasksThenTheirHeadingStaysVisible() {
        showScreen(resultState(longResults()))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(12)

        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_tomorrow)) and heading).assertIsDisplayed()
    }

    @Test
    fun givenLongResultsWhenScrollingTodayTasksThenTheirHeadingStaysVisible() {
        showScreen(resultState(longResults()))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(34)

        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_today)) and heading).assertIsDisplayed()
    }

    private val heading = SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit)

    private fun resultState(tasks: List<Task>) = SearchState("gym", SearchResults.Success(tasks.toImmutableList()))

    private fun longResults(): List<Task> {
        val newest = (1L..20L).map {
            task.copy(id = it, day = today.plusDays(1), name = "Tomorrow task $it")
        }
        val older = newest.map { it.copy(id = it.id + 20, day = today, name = "Today task ${it.id}") }
        return newest + older
    }

    private fun assertBefore(first: String, second: String) {
        val firstY = composeTestRule.onNodeWithText(first).fetchSemanticsNode().boundsInRoot.top
        val secondY = composeTestRule.onNodeWithText(second).fetchSemanticsNode().boundsInRoot.top
        assertThat(firstY).isLessThan(secondY)
    }

    private fun showScreen(
        state: SearchState = SearchState(),
        onQueryChange: (String) -> Unit = {},
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpenTask: (Task) -> Unit = {},
        onMore: (Task) -> Unit = {},
        onMark: (Long, Boolean) -> Unit = { _, _ -> },
        onDelete: (Task) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AtomTheme {
                SearchContent(state, onQueryChange, onBack, onRetry, onOpenTask, onMore, onMark, onDelete)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SearchContent(
        state: SearchState,
        onQueryChange: (String) -> Unit = {},
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpenTask: (Task) -> Unit = {},
        onMore: (Task) -> Unit = {},
        onMark: (Long, Boolean) -> Unit = { _, _ -> },
        onDelete: (Task) -> Unit = {},
    ) {
        SearchScreen(
            state = state,
            onQueryChange = onQueryChange,
            onBack = onBack,
            onRetry = onRetry,
            onOpenTask = onOpenTask,
            onMore = onMore,
            onMark = onMark,
            onDelete = onDelete,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
