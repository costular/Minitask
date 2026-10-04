package com.costular.atomtasks.verticaltasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.testing.MainCoroutineRule
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.core.ui.tasks.TaskInteractionState
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksInRangeUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class VerticalTasksViewModelTest {
    @get:Rule
    val main = MainCoroutineRule(StandardTestDispatcher())
    private val interactionState = MutableStateFlow(TaskInteractionState())
    private val interactions = mockk<TaskInteractionStateHolder>(relaxed = true) {
        every { state } returns interactionState
    }
    private val interactionFactory = mockk<TaskInteractionStateHolder.Factory> {
        every { create(any()) } returns interactions
    }

    private val today = LocalDate.of(2026, 10, 3)
    private val clock = Clock.fixed(Instant.parse("2026-10-03T12:00:00Z"), ZoneOffset.UTC)
    private val repository = mockk<TasksRepository>()
    private val move = mockk<MoveTaskUseCase>(relaxUnitFun = true)
    private val snackbar = mockk<SnackbarManager>(relaxUnitFun = true)
    private val tasks = MutableStateFlow<List<Task>>(emptyList())

    @Test
    fun `given an empty task list when the screen starts then today is selected`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertThat(viewModel.state.value.selectedDay).isEqualTo(today)
    }

    @Test
    fun `given an empty task list when the screen starts then today is the scroll target`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertThat(viewModel.state.value.scrollTarget?.day).isEqualTo(today)
    }

    @Test
    fun `given an empty task list when the screen starts then all 61 days have headers`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Header>()).hasSize(61)
    }

    @Test
    fun `given an empty task list when the screen starts then all 61 days have empty rows`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Empty>()).hasSize(61)
    }

    @Test
    fun `given an empty task list when the screen starts then rows start 30 days before today`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertThat(viewModel.state.value.rows.first().day).isEqualTo(today.minusDays(30))
    }

    @Test
    fun `given a loaded window when scrolling toward the future then the retained window is capped`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)

        repeat(4) {
            val window = viewModel.state.value.window
            viewModel.onVisibleRange(window.end.minusDays(2), window.end)
            runCurrent()
        }

        assertThat(viewModel.state.value.window.dayCount).isEqualTo(121)
    }

    @Test
    fun `given a loaded window when scrolling toward the future then the boundary advances by 30 days per request`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)

        repeat(4) {
            val window = viewModel.state.value.window
            viewModel.onVisibleRange(window.end.minusDays(2), window.end)
            runCurrent()
        }

        assertThat(viewModel.state.value.window.end).isEqualTo(today.plusDays(150))
    }

    @Test
    fun `given a loaded window when scrolling toward the past then the retained window is capped`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)

        repeat(4) {
            val window = viewModel.state.value.window
            viewModel.onVisibleRange(window.start, window.start.plusDays(1))
            runCurrent()
        }

        assertThat(viewModel.state.value.window.dayCount).isEqualTo(121)
    }

    @Test
    fun `given a loaded window when scrolling toward the past then the boundary advances by 30 days per request`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)

        repeat(4) {
            val window = viewModel.state.value.window
            viewModel.onVisibleRange(window.start, window.start.plusDays(1))
            runCurrent()
        }

        assertThat(viewModel.state.value.window.start).isEqualTo(today.minusDays(150))
    }

    @Test
    fun `given a distant date when selected then the window recenters`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        val destination = today.plusYears(2)

        viewModel.selectDay(destination)
        runCurrent()

        assertThat(viewModel.state.value.window).isEqualTo(DateWindow.around(destination))
    }

    @Test
    fun `given a distant date when selected then the scroll target changes`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        val destination = today.plusYears(2)

        viewModel.selectDay(destination)
        runCurrent()

        assertThat(viewModel.state.value.scrollTarget?.day).isEqualTo(destination)
    }

    @Test
    fun `given a distant selected date when returning to today then the clock date is selected`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.selectDay(today.plusYears(2))
        runCurrent()

        viewModel.selectToday()
        runCurrent()

        assertThat(viewModel.state.value.selectedDay).isEqualTo(today)
    }

    @Test
    fun `given a distant selected date when returning to today then the window recenters on today`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.selectDay(today.plusYears(2))
        runCurrent()

        viewModel.selectToday()
        runCurrent()

        assertThat(viewModel.state.value.window).isEqualTo(DateWindow.around(today))
    }

    @Test
    fun `given a settled scroll when a sticky anchor changes then its day is selected`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)

        viewModel.onAnchor(today.plusDays(3), 17L, 28)

        assertThat(viewModel.state.value.selectedDay).isEqualTo(today.plusDays(3))
    }

    @Test
    fun `given a saved scroll anchor when recreated then its task and offset are restored`() = runTest(main.testDispatcher) {
        val saved = SavedStateHandle()
        val viewModel = loadedViewModel(saved)
        viewModel.onScrollHandled(0)
        viewModel.onAnchor(today.plusDays(3), 17L, 28)

        val restored = viewModel(saved)

        assertThat(restored.state.value.scrollTarget).isEqualTo(ScrollTarget(today.plusDays(3), 17L, 28))
    }

    @Test
    fun `given a pending date jump when the old anchor arrives then the destination stays selected`() = runTest(main.testDispatcher) {
        val saved = SavedStateHandle()
        val viewModel = loadedViewModel(saved)
        viewModel.onScrollHandled(0)
        viewModel.onAnchor(today, null, 0)
        val destination = today.plusYears(1)
        viewModel.selectDay(destination)

        viewModel.onAnchor(today, 17L, 28)

        assertThat(viewModel.state.value.selectedDay).isEqualTo(destination)
    }

    @Test
    fun `given a pending date jump and a stale anchor when recreated then the destination is restored`() = runTest(main.testDispatcher) {
        val saved = SavedStateHandle()
        val viewModel = loadedViewModel(saved)
        viewModel.onScrollHandled(0)
        viewModel.onAnchor(today, null, 0)
        val destination = today.plusYears(1)
        viewModel.selectDay(destination)
        viewModel.onAnchor(today, 17L, 28)

        val restored = viewModel(saved)

        assertThat(restored.state.value.scrollTarget).isEqualTo(ScrollTarget(destination))
    }

    @Test
    fun `given same-day tasks when dragged then task keys determine row order`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()

        viewModel.onMove(1, 2)

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Item>().map { it.task.id })
            .containsExactly(2L, 1L).inOrder()
    }

    @Test
    fun `given reordered same-day tasks when dragging ends then original positions are persisted`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()
        viewModel.onMove(1, 2)

        viewModel.onDragStopped()
        runCurrent()

        coVerify(exactly = 1) { move(MoveTaskUseCase.Params(today, 2, 4)) }
    }

    @Test
    fun `given task rows when dragged to another day then row order is unchanged`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()
        val original = viewModel.state.value.rows

        viewModel.onMove(1, 2)

        assertThat(viewModel.state.value.rows).isEqualTo(original)
    }

    @Test
    fun `given task rows when dragged to another day then dragging never starts`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()

        viewModel.onMove(1, 2)

        assertThat(viewModel.state.value.isDragging).isFalse()
    }

    @Test
    fun `given task rows when dragged to another day then no move is persisted`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()

        viewModel.onMove(1, 2)
        viewModel.onDragStopped()
        runCurrent()

        coVerify(exactly = 0) { move(any()) }
    }

    @Test
    fun `given task rows when dragged to a header key then row order is unchanged`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()
        val original = viewModel.state.value.rows

        viewModel.onMove(1, -1)

        assertThat(viewModel.state.value.rows).isEqualTo(original)
    }

    @Test
    fun `given task rows when dragged to a header key then dragging never starts`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()

        viewModel.onMove(1, -1)

        assertThat(viewModel.state.value.isDragging).isFalse()
    }

    @Test
    fun `given task rows when dragged to a header key then no move is persisted`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today), TaskToday.copy(id = 2, day = today.plusDays(1)))
        val viewModel = loadedViewModel()

        viewModel.onMove(1, -1)
        viewModel.onDragStopped()
        runCurrent()

        coVerify(exactly = 0) { move(any()) }
    }

    @Test
    fun `given an active drag when reaching the window edge then range changes are blocked`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()
        viewModel.onScrollHandled(0)
        val window = viewModel.state.value.window
        viewModel.onMove(1, 2)

        viewModel.onVisibleRange(window.start, window.start)
        runCurrent()

        assertThat(viewModel.state.value.window).isEqualTo(window)
    }

    @Test
    fun `given a failed move when dragging ends then database order is restored`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()
        coEvery { move(any()) } throws IllegalStateException("storage unavailable")
        val original = viewModel.state.value.rows
        viewModel.onMove(1, 2)

        viewModel.onDragStopped()
        runCurrent()

        assertThat(viewModel.state.value.rows).isEqualTo(original)
    }

    @Test
    fun `given a failed move when dragging ends then dragging ends`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()
        coEvery { move(any()) } throws IllegalStateException("storage unavailable")
        viewModel.onMove(1, 2)

        viewModel.onDragStopped()
        runCurrent()

        assertThat(viewModel.state.value.isDragging).isFalse()
    }

    @Test
    fun `given a failed move when dragging ends then error feedback is shown`() = runTest(main.testDispatcher) {
        tasks.value = listOf(
            TaskToday.copy(id = 1, day = today, position = 2),
            TaskToday.copy(id = 2, day = today, position = 4),
        )
        val viewModel = loadedViewModel()
        coEvery { move(any()) } throws IllegalStateException("storage unavailable")
        viewModel.onMove(1, 2)

        viewModel.onDragStopped()
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.error_generic)) }
    }

    @Test
    fun `given an empty list when a task is inserted then a task row appears`() = runTest(main.testDispatcher) {
        val viewModel = loadedViewModel()

        tasks.value = listOf(TaskToday.copy(id = 1, day = today))
        runCurrent()

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Item>()).hasSize(1)
    }

    @Test
    fun `given a populated list when its task is deleted then day headers remain`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today))
        val viewModel = loadedViewModel()

        tasks.value = emptyList()
        runCurrent()

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Header>()).hasSize(61)
    }

    @Test
    fun `given a populated list when its task is deleted then empty day rows return`() = runTest(main.testDispatcher) {
        tasks.value = listOf(TaskToday.copy(id = 1, day = today))
        val viewModel = loadedViewModel()

        tasks.value = emptyList()
        runCurrent()

        assertThat(viewModel.state.value.rows.filterIsInstance<VerticalTaskRow.Empty>()).hasSize(61)
    }

    @Test
    fun `given a screen when created then interactions use its lifecycle scope`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        verify(exactly = 1) { interactionFactory.create(viewModel.viewModelScope) }
    }

    @Test
    fun `given an interaction holder when the screen is created then its state is exposed`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        assertThat(viewModel.taskInteractionState).isSameInstanceAs(interactionState)
    }

    @Test
    fun `given a screen when handling completion then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onMarkTask(1, true)

        verify(exactly = 1) { interactions.onMarkTask(1, true) }
    }

    @Test
    fun `given a screen when handling deletion requests then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.askDelete(2)

        verify(exactly = 1) { interactions.askDelete(2) }
    }

    @Test
    fun `given a screen when handling deletion dismissal then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.dismissDelete()

        verify(exactly = 1) { interactions.dismissDelete() }
    }

    @Test
    fun `given a screen when handling ordinary deletion then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.deleteTask(3)

        verify(exactly = 1) { interactions.deleteTask(3) }
    }

    @Test
    fun `given a screen when handling recurring deletion then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.deleteRecurringTask(4, RecurringRemovalStrategy.ALL)

        verify(exactly = 1) { interactions.deleteRecurringTask(4, RecurringRemovalStrategy.ALL) }
    }

    @Test
    fun `given a screen when handling review completion then the action is delegated`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onReviewFinished()

        verify(exactly = 1) { interactions.onReviewFinished() }
    }

    private fun loadedViewModel(saved: SavedStateHandle = SavedStateHandle()): VerticalTasksViewModel =
        viewModel(saved).also { main.testDispatcher.scheduler.runCurrent() }

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()): VerticalTasksViewModel {
        every { repository.observeTasksInRange(any(), any()) } answers {
            val start = firstArg<LocalDate>()
            val end = secondArg<LocalDate>()
            tasks.map { values ->
                values.filter { it.day in start..end }
                    .sortedWith(compareBy<Task> { it.day }.thenBy { it.position })
            }
        }
        return VerticalTasksViewModel(
            observeTasks = ObserveTasksInRangeUseCase(repository),
            moveTask = move,
            savedState = saved,
            clock = clock,
            snackbar = snackbar,
            taskInteractionFactory = interactionFactory,
        )
    }
}
