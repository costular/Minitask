package com.costular.atomtasks.agenda

import app.cash.turbine.test
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics
import com.costular.atomtasks.agenda.ui.AgendaViewModel
import com.costular.atomtasks.agenda.ui.TasksState
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.testing.MviViewModelTest
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.usecase.invoke
import com.costular.atomtasks.data.tutorial.ShouldShowOnboardingUseCase
import com.costular.atomtasks.data.tutorial.ShouldShowTaskOrderTutorialUseCase
import com.costular.atomtasks.data.tutorial.TaskOrderTutorialDismissedUseCase
import com.costular.atomtasks.review.usecase.ShouldAskReviewUseCase
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalTime
class AgendaViewModelTest : MviViewModelTest() {

    lateinit var sut: AgendaViewModel

    private val observeTasksUseCase: ObserveTasksUseCase = mockk()
    private val updateTaskIsDoneUseCase: UpdateTaskIsDoneUseCase = mockk(relaxUnitFun = true)
    private val removeTaskUseCase: RemoveTaskUseCase = mockk(relaxUnitFun = true)
    private val autoforwardManager: AutoforwardManager = mockk(relaxUnitFun = true)
    private val recurrenceScheduler: RecurrenceScheduler = mockk(relaxUnitFun = true)
    private val moveTaskUseCase: MoveTaskUseCase = mockk(relaxUnitFun = true)
    private val atomAnalytics: AtomAnalytics = mockk(relaxUnitFun = true)
    private val shouldShowTaskOrderTutorialUseCase: ShouldShowTaskOrderTutorialUseCase =
        mockk(relaxUnitFun = true)
    private val taskOrderTutorialDismissedUseCase: TaskOrderTutorialDismissedUseCase =
        mockk(relaxUnitFun = true)
    private val shouldAskReviewUseCase: ShouldAskReviewUseCase = mockk(relaxUnitFun = true)
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase = mockk()
    private val snackbarManager: SnackbarManager = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        givenOnboarding(false)
        initializeViewModel()
    }

    @Test
    fun `should expose correct state when land on screen`() = runTest {
        sut.state.test {
            val lastState = expectMostRecentItem()
            assertThat(lastState.selectedDay.date).isEqualTo(LocalDate.now())
        }
    }

    @Test
    fun `should expose tasks when load succeed`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())

        sut.loadTasks()

        sut.state.test {
            assertThat(awaitItem().tasks).isEqualTo(TasksState.Success(expected.toImmutableList()))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should expose today as selected day when tap on select today`() = runTest {
        sut.setSelectedDay(LocalDate.now().plusDays(10))
        sut.setSelectedDayToday()

        sut.state.test {
            assertThat(awaitItem().selectedDay.date).isEqualTo(LocalDate.now())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should load task accordingly when mark task as done`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())

        sut.loadTasks()
        sut.onMarkTask(expected.first().id, true)

        coVerify {
            updateTaskIsDoneUseCase(
                UpdateTaskIsDoneUseCase.Params(
                    taskId = expected.first().id,
                    isDone = true,
                ),
            )
        }
    }

    @Test
    fun `should show delete task action when tap on delete`() =
        runTest {
            val tasks = DEFAULT_TASKS
            val taskId = DEFAULT_TASKS.first().id
            coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(tasks.toResult())

            sut.loadTasks()
            sut.actionDelete(taskId)

            val state = sut.state.value
            assertThat(state.removeTaskConfirmationUiState)
                .isInstanceOf(RemoveTaskConfirmationUiState.Shown::class.java)
        }

    @Test
    fun `should set delete task action to hidden when dismiss dialog`() =
        runTest {
            val tasks = DEFAULT_TASKS
            val taskId = DEFAULT_TASKS.first().id
            coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(tasks.toResult())

            sut.loadTasks()
            sut.setStateWithRecurringTask(taskId)
            sut.actionDelete(taskId)
            sut.dismissDelete()

            sut.state.test {
                assertThat(expectMostRecentItem().removeTaskConfirmationUiState).isInstanceOf(
                    RemoveTaskConfirmationUiState.Hidden::class.java
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should load empty tasks when remove given there is only one existing task`() =
        runTest {
            val taskId = DEFAULT_TASKS.first().id
            sut.deleteTask(taskId)

            coVerify { removeTaskUseCase(RemoveTaskUseCase.Params(taskId)) }
        }

    @Test
    fun `should show completed snackbar when mark task as done`() = runTest {
        sut.onMarkTask(DEFAULT_TASKS.first().id, true)

        verify {
            snackbarManager.showMessage(
                AppSnackbarMessage(messageRes = R.string.task_feedback_completed)
            )
        }
    }

    @Test
    fun `should show deleted snackbar when delete succeeds`() = runTest {
        val taskId = DEFAULT_TASKS.first().id

        sut.deleteTask(taskId)

        coVerify { removeTaskUseCase(RemoveTaskUseCase.Params(taskId)) }
        verify {
            snackbarManager.showMessage(
                AppSnackbarMessage(messageRes = R.string.task_feedback_deleted)
            )
        }
    }

    @Test
    fun `should collapse header when select a day`() = runTest {
        sut.toggleHeader()
        sut.setSelectedDay(LocalDate.now().plusDays(1))

        sut.state.test {
            assertThat(expectMostRecentItem().isHeaderExpanded).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should update state when drag task`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())
        val from = 0
        val to = 1

        sut.loadTasks()
        sut.onDragTask(ItemPosition(from, TASK1_ID), ItemPosition(to, TASK2ID))

        val state = sut.state.value
        val tasks = (state.tasks as TasksState.Success).data
        assertThat(tasks.first().id).isEqualTo(TASK2ID)
        assertThat(tasks.last().id).isEqualTo(TASK1_ID)
        coVerify(exactly = 0) { moveTaskUseCase(any()) }
    }

    @Test
    fun `should call usecase when moving a task given tasks were loaded successfully`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())
        val from = 0
        val to = 1

        sut.loadTasks()
        sut.onDragTask(
            ItemPosition(from, DEFAULT_TASKS.first().id),
            ItemPosition(to, DEFAULT_TASKS[1].id)
        )
        sut.onDragStopped()

        coVerify(exactly = 1) {
            moveTaskUseCase(
                MoveTaskUseCase.Params(
                    LocalDate.now(),
                    DEFAULT_TASKS.first().position,
                    DEFAULT_TASKS[1].position
                )
            )
        }
    }

    @Test
    fun `should NOT call usecase when moving a task given the drag task failed`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())
        val from = 0
        val to = 1

        sut.loadTasks()
        sut.onDragStopped()

        coVerify(exactly = 0) {
            moveTaskUseCase(any())
        }
    }

    @Test
    fun `should call autoforward usecase when land on the screen`() = runTest {
        coVerify(exactly = 1) {
            autoforwardManager.scheduleOrCancelAutoforwardTasks()
        }
    }

    @Test
    fun `should track event when expand header`() = runTest {
        sut.toggleHeader()

        verify {
            atomAnalytics.track(AgendaAnalytics.ExpandCalendar)
        }
    }

    @Test
    fun `should track event when collapse header`() = runTest {
        sut.toggleHeader()
        sut.toggleHeader()

        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.CollapseCalendar)
        }
    }

    @Test
    fun `should track event when moving a task`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())
        val from = 0
        val to = 1

        sut.loadTasks()
        sut.onDragStopped()

        verify { atomAnalytics.track(AgendaAnalytics.OrderTask) }
    }

    @Test
    fun `should track event when mark task as done`() = runTest {
        val expected = DEFAULT_TASKS
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())

        sut.loadTasks()
        sut.onMarkTask(expected.first().id, true)

        verify {
            atomAnalytics.track(AgendaAnalytics.MarkTaskAsDone)
        }
    }

    @Test
    fun `should track event when mark task as not done`() = runTest {
        val expected = DEFAULT_TASKS

        every {
            observeTasksUseCase.invoke(any())
        } returns flowOf(expected.toResult())

        givenOrderTasksTutorial(true)

        initializeViewModel()
        sut.loadTasks()
        sut.onMarkTask(expected.last().id, false)

        verify {
            atomAnalytics.track(AgendaAnalytics.MarkTaskAsNotDone)
        }
    }

    @Test
    fun `should track show confirm delete when tap on delete`() = runTest {
        val tasks = DEFAULT_TASKS
        val taskId = DEFAULT_TASKS.first().id
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(tasks.toResult())

        sut.setStateWithRecurringTask(taskId)
        sut.loadTasks()
        sut.actionDelete(taskId)

        verify {
            atomAnalytics.track(AgendaAnalytics.ShowConfirmDeleteDialog)
        }
    }

    @Test
    fun `should track confirm delete when confirm dialog`() = runTest {
        val taskId = DEFAULT_TASKS.first().id
        sut.deleteTask(taskId)

        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.ConfirmDelete)
        }
    }

    @Test
    fun `should track navigate to day when select a new day`() = runTest {
        every { observeTasksUseCase.invoke(any()) } returns flowOf(emptyList<Task>().toResult())
        val day = LocalDate.of(2023, 9, 16)

        sut.setSelectedDay(day)

        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.NavigateToDay(day.toString()))
        }
    }

    @Test
    fun `should track cancel delete when dismiss confirm delete dialog`() = runTest {
        val tasks = DEFAULT_TASKS
        val taskId = DEFAULT_TASKS.first().id
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(tasks.toResult())

        sut.setStateWithRecurringTask(taskId)
        sut.loadTasks()
        sut.actionDelete(taskId)
        sut.dismissDelete()

        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.CancelDelete)
        }
    }

    @Test
    fun `should show order task when land on screen given the tutorial hasn't been shown for the user yet`() =
        runTest {
            givenOrderTasksTutorial(true)

            initializeViewModel()

            assertThat(sut.state.value.shouldShowCardOrderTutorial).isTrue()
        }

    @Test
    fun `should call taskOrderTutorialUseCase when order task tutorial is dismissed`() = runTest {
        sut.orderTaskTutorialDismissed()

        coVerify(exactly = 1) {
            taskOrderTutorialDismissedUseCase.invoke(Unit)
        }
    }

    @Test
    fun `should expose ask review when mark task as done given usecase returns true`() = runTest {
        coEvery { shouldAskReviewUseCase() } returns Either.Result(true)
        coEvery { updateTaskIsDoneUseCase.invoke(any()) } returns Unit.toResult()
        givenSuccessTasks()

        sut.onMarkTask(DEFAULT_TASKS.first().id, true)

        assertThat(sut.state.value.shouldShowReviewDialog).isTrue()
    }

    @Test
    fun `should not expose ask review when mark task as NOT done given usecase returns true`() =
        runTest {
            coEvery { shouldAskReviewUseCase() } returns Either.Result(true)
            givenSuccessTasks()

            sut.onMarkTask(DEFAULT_TASKS.first().id, false)

            assertThat(sut.state.value.shouldShowReviewDialog).isFalse()
        }

    @Test
    fun `should not expose ask review when finish review given the review was being shown`() =
        runTest {
            coEvery { shouldAskReviewUseCase() } returns Either.Result(true)
            givenSuccessTasks()

            sut.onMarkTask(DEFAULT_TASKS.first().id, true)
            sut.onReviewFinished()

            assertThat(sut.state.value.shouldShowReviewDialog).isFalse()
        }

    private fun givenSuccessTasks() {
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(DEFAULT_TASKS.toResult())
    }

    private fun AgendaViewModel.setStateWithRecurringTask(taskId: Long) {
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(
            DEFAULT_TASKS.map { task ->
                if (task.id == taskId) {
                    task.copy(isRecurring = true)
                } else {
                    task
                }
            }.toResult()
        )
        loadTasks()
    }

    private fun givenOrderTasksTutorial(isEnabled: Boolean) {
        coEvery { shouldShowTaskOrderTutorialUseCase.invoke(Unit) } returns flowOf(isEnabled)
    }

    companion object {
        const val TASK1_ID = 1L
        const val TASK2ID = 2L
        val DEFAULT_TASKS = listOf(
            Task(
                id = TASK1_ID,
                name = "Task 1",
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                reminder = null,
                isDone = false,
                position = 1,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
            Task(
                id = TASK2ID,
                name = "Task 2",
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                reminder = null,
                isDone = true,
                position = 2,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
        )
    }

    private fun givenOnboarding(shouldBeShown: Boolean) {
        coEvery {
            shouldShowOnboardingUseCase.invoke(Unit)
        } returns flowOf(shouldBeShown).toResult()
    }

    private fun initializeViewModel() {
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(emptyList<Task>().toResult())
        coEvery { updateTaskIsDoneUseCase.invoke(any()) } returns Unit.toResult()
        coEvery { removeTaskUseCase.invoke(any()) } returns Unit.toResult()
        coEvery { shouldAskReviewUseCase.invoke(Unit) } returns false.toResult()
        givenOrderTasksTutorial(true)

        sut = AgendaViewModel(
            observeTasksUseCase = observeTasksUseCase,
            updateTaskIsDoneUseCase = updateTaskIsDoneUseCase,
            removeTaskUseCase = removeTaskUseCase,
            autoforwardManager = autoforwardManager,
            moveTaskUseCase = moveTaskUseCase,
            atomAnalytics = atomAnalytics,
            shouldShowTaskOrderTutorialUseCase = shouldShowTaskOrderTutorialUseCase,
            taskOrderTutorialDismissedUseCase = taskOrderTutorialDismissedUseCase,
            shouldShowAskReviewUseCase = shouldAskReviewUseCase,
            recurrenceScheduler = recurrenceScheduler,
            shouldShowOnboardingUseCase = shouldShowOnboardingUseCase,
            snackbarManager = snackbarManager,
        )
    }
}
