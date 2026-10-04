package com.costular.atomtasks.verticaltasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import com.costular.atomtasks.core.testing.ui.AndroidTest
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.LocalDate
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

@HiltAndroidTest
class VerticalTasksScreenTest : AndroidTest() {
    private val today = LocalDate.now()
    private val heading = SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit)

    @Test
    fun givenTasksOnDifferentDaysWhenDraggedAcrossDaysThenMoveIsNotForwarded() {
        val first = TaskToday.copy(id = 1, day = today, name = "First day task")
        val second = first.copy(id = 2, day = today.plusDays(1), name = "Second day task")
        var moves = 0
        showScreen(state = stateFor(listOf(first, second)), onMove = { _, _ -> moves++ })

        drag(first.name, second.name)

        composeTestRule.runOnIdle { assertThat(moves).isEqualTo(0) }
    }

    @Test
    fun givenTasksOnDifferentDaysWhenDraggedAcrossDaysThenDragStartIsForwarded() {
        val first = TaskToday.copy(id = 1, day = today, name = "First day task")
        val second = first.copy(id = 2, day = today.plusDays(1), name = "Second day task")
        var starts = 0
        showScreen(state = stateFor(listOf(first, second)), onDragStarted = { starts++ })

        drag(first.name, second.name)

        composeTestRule.runOnIdle { assertThat(starts).isEqualTo(1) }
    }

    @Test
    fun givenDateControlsWhenSearchClickedThenCallbackRuns() {
        var searches = 0
        showScreen(onSearch = { searches++ })

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.search_tasks)).performClick()

        assertThat(searches).isEqualTo(1)
    }

    @Test
    fun givenDateControlsWhenCalendarClickedThenCallbackRuns() {
        var calendars = 0
        showScreen(onCalendar = { calendars++ })

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.home_menu_calendar)).performClick()

        assertThat(calendars).isEqualTo(1)
    }

    @Test
    fun givenPopulatedDaysWhenScrolledBeforeTodayThenTodayBubbleIsHidden() {
        showScreen(state = populatedState(includeYesterday = true))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)

        composeTestRule.onNode(todayBubble()).assertDoesNotExist()
    }

    @Test
    fun givenPopulatedDaysWhenScrolledAtTodayHeaderThenTodayBubbleIsHidden() {
        showScreen(state = populatedState(includeYesterday = true))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(2)

        composeTestRule.onNode(todayBubble()).assertDoesNotExist()
    }

    @Test
    fun givenPopulatedDaysWhenScrolledWithinTodayTasksThenTodayBubbleIsVisible() {
        showScreen(state = populatedState(includeYesterday = true))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(12)

        composeTestRule.onNode(todayBubble()).assertIsDisplayed()
    }

    @Test
    fun givenPopulatedDaysWhenScrolledAtTomorrowHeaderThenTodayBubbleIsVisible() {
        showScreen(state = populatedState(includeYesterday = true))

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(33)

        composeTestRule.onNode(todayBubble()).assertIsDisplayed()
    }

    @Test
    fun givenTodayBubbleWhenScrollingBackToTodayHeaderThenBubbleIsHidden() {
        showScreen(state = populatedState(includeYesterday = true))
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(12)

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(2)

        composeTestRule.onNode(todayBubble()).assertDoesNotExist()
    }

    @Test
    fun givenTodayBubbleWhenClickedThenBubbleIsHidden() {
        showTodayReturnScreen()
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)

        composeTestRule.onNode(todayBubble()).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(todayBubble()).assertDoesNotExist()
    }

    @Test
    fun givenTodayBubbleWhenClickedThenTodayHeaderIsVisible() {
        showTodayReturnScreen()
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)

        composeTestRule.onNode(todayBubble()).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_today)) and heading).assertIsDisplayed()
    }

    @Test
    fun givenTodayBubbleWhenClickedThenTodayCallbackRunsOnce() {
        var returns = 0
        showTodayReturnScreen(onToday = { returns++ })
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)

        composeTestRule.onNode(todayBubble()).performClick()
        composeTestRule.waitForIdle()

        assertThat(returns).isEqualTo(1)
    }

    @Test
    fun givenTodayBubbleWhenClickedThenScrollRequestIsConsumed() {
        val state = showTodayReturnScreen()
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)

        composeTestRule.onNode(todayBubble()).performClick()
        composeTestRule.waitForIdle()

        assertThat(state.value.scrollTarget).isNull()
    }

    @Test
    fun givenFutureWindowWithoutTodayWhenRenderedThenTodayBubbleIsVisible() {
        val day = today.plusDays(100)
        val rows = listOf(VerticalTaskRow.Header(day), VerticalTaskRow.Empty(day)).toImmutableList()

        showScreen(state = VerticalTasksState(day, DateWindow.around(day), rows, isLoading = false))

        composeTestRule.onNode(todayBubble()).assertIsDisplayed()
    }

    @Test
    fun givenFutureWindowWithoutTodayWhenTodayClickedThenCallbackRuns() {
        val day = today.plusDays(100)
        val rows = listOf(VerticalTaskRow.Header(day), VerticalTaskRow.Empty(day)).toImmutableList()
        var returns = 0
        showScreen(
            state = VerticalTasksState(day, DateWindow.around(day), rows, isLoading = false),
            onToday = { returns++ },
        )

        composeTestRule.onNode(todayBubble()).performClick()

        assertThat(returns).isEqualTo(1)
    }

    @Test
    fun givenTasksAndEmptyDaysWhenRenderedThenTaskIsVisible() {
        val task = TaskToday.copy(id = 1, day = today, name = "Visible task")
        val rows = listOf(
            VerticalTaskRow.Header(today), VerticalTaskRow.Item(task),
            VerticalTaskRow.Header(today.plusDays(1)), VerticalTaskRow.Empty(today.plusDays(1)),
        ).toImmutableList()

        showScreen(state = VerticalTasksState(today, DateWindow.around(today), rows, isLoading = false))

        composeTestRule.onNodeWithText(task.name).assertIsDisplayed()
    }

    @Test
    fun givenTasksAndEmptyDaysWhenRenderedThenEmptyDayMessageIsVisible() {
        val task = TaskToday.copy(id = 1, day = today, name = "Visible task")
        val rows = listOf(
            VerticalTaskRow.Header(today), VerticalTaskRow.Item(task),
            VerticalTaskRow.Header(today.plusDays(1)), VerticalTaskRow.Empty(today.plusDays(1)),
        ).toImmutableList()

        showScreen(state = VerticalTasksState(today, DateWindow.around(today), rows, isLoading = false))

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.vertical_empty_day)).assertIsDisplayed()
    }

    @Test
    fun givenPopulatedDaysWhenScrollingTodayThenHeaderStaysVisible() {
        showScreen(state = populatedState())

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(12)

        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_today)) and heading).assertIsDisplayed()
    }

    @Test
    fun givenPopulatedDaysWhenScrollingTodayThenAnchorDayUpdates() {
        var anchoredDay: LocalDate? = null
        showScreen(state = populatedState(), onAnchor = { date, _, _ -> anchoredDay = date })

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(12)
        composeTestRule.waitForIdle()

        assertThat(anchoredDay).isEqualTo(today)
    }

    @Test
    fun givenPopulatedDaysWhenScrollingTomorrowThenHeaderStaysVisible() {
        showScreen(state = populatedState())

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(31)

        composeTestRule.onNode(hasText(composeTestRule.getString(R.string.day_tomorrow)) and heading).assertIsDisplayed()
    }

    @Test
    fun givenPopulatedDaysWhenScrollingTomorrowThenAnchorDayUpdates() {
        var anchoredDay: LocalDate? = null
        showScreen(state = populatedState(), onAnchor = { date, _, _ -> anchoredDay = date })

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(31)
        composeTestRule.waitForIdle()

        assertThat(anchoredDay).isEqualTo(today.plusDays(1))
    }

    private fun showTodayReturnScreen(onToday: () -> Unit = {}): State<VerticalTasksState> {
        val state = mutableStateOf(populatedState())
        composeTestRule.setContent {
            AtomTheme {
                VerticalTasksScreen(
                    state = state.value,
                    onToday = {
                        onToday()
                        state.value = state.value.copy(
                            selectedDay = today,
                            scrollTarget = ScrollTarget(today, requestId = 1),
                        )
                    },
                    onScrollHandled = { state.value = state.value.copy(scrollTarget = null) },
                    onSearch = {},
                    onCalendar = {},
                    onDismissCalendar = {},
                    onSelectDay = {},
                    onAnchor = { _, _, _ -> },
                    onVisibleRange = { _, _ -> },
                    onRetry = {},
                    onOpenTask = {},
                    onMore = {},
                    onDelete = {},
                    onMark = { _, _ -> },
                    onMove = { _, _ -> },
                    onDragStarted = {},
                    onDragStopped = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        return state
    }

    private fun todayBubble() = hasContentDescription(composeTestRule.getString(R.string.today))

    private fun stateFor(tasks: List<Task>): VerticalTasksState {
        val rows = tasks.groupBy { it.day }.flatMap { (day, tasksForDay) ->
            listOf(VerticalTaskRow.Header(day)) + tasksForDay.map { VerticalTaskRow.Item(it) }
        }.toImmutableList()
        return VerticalTasksState(today, DateWindow.around(today), rows, isLoading = false)
    }

    private fun populatedState(includeYesterday: Boolean = false): VerticalTasksState {
        val tasks = (1L..30L).map { TaskToday.copy(id = it, day = today, name = "Task $it") }
        val future = tasks.map { it.copy(id = it.id + 30, day = today.plusDays(1)) }
        val state = stateFor(tasks + future)
        return if (includeYesterday) {
            state.copy(rows = (listOf(
                VerticalTaskRow.Header(today.minusDays(1)),
                VerticalTaskRow.Empty(today.minusDays(1)),
            ) + state.rows).toImmutableList())
        } else {
            state
        }
    }

    private fun drag(from: String, to: String) {
        val start = composeTestRule.onNodeWithText(from).fetchSemanticsNode().boundsInRoot.center
        val end = composeTestRule.onNodeWithText(to).fetchSemanticsNode().boundsInRoot.center
        composeTestRule.onRoot().performTouchInput {
            down(start)
            advanceEventTime(700)
            moveTo(end, delayMillis = 500)
            up()
        }
    }

    private fun showScreen(
        state: VerticalTasksState = VerticalTasksState(today, DateWindow.around(today), isLoading = false),
        onToday: () -> Unit = {},
        onSearch: () -> Unit = {},
        onCalendar: () -> Unit = {},
        onAnchor: (LocalDate, Long?, Int) -> Unit = { _, _, _ -> },
        onMove: (Long, Long) -> Unit = { _, _ -> },
        onDragStarted: (Long) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AtomTheme {
                VerticalTasksScreen(
                    state = state,
                    onToday = onToday,
                    onSearch = onSearch,
                    onCalendar = onCalendar,
                    onDismissCalendar = {},
                    onSelectDay = {},
                    onScrollHandled = {},
                    onAnchor = onAnchor,
                    onVisibleRange = { _, _ -> },
                    onRetry = {},
                    onOpenTask = {},
                    onMore = {},
                    onDelete = {},
                    onMark = { _, _ -> },
                    onMove = onMove,
                    onDragStarted = onDragStarted,
                    onDragStopped = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
