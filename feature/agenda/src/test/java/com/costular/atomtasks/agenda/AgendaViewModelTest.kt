package com.costular.atomtasks.agenda

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics
import com.costular.atomtasks.agenda.ui.AgendaViewModel
import com.costular.atomtasks.agenda.ui.TasksState
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.testing.MviViewModelTest
import com.costular.atomtasks.core.ui.tasks.TaskInteractionState
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.usecase.invoke
import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.data.tutorial.ShouldShowOnboardingUseCase
import com.costular.atomtasks.data.tutorial.ShouldShowTaskOrderTutorialUseCase
import com.costular.atomtasks.data.tutorial.TaskOrderTutorialDismissedUseCase
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalTime
class AgendaViewModelTest : MviViewModelTest() {

    lateinit var sut: AgendaViewModel

    private val interactionState = MutableStateFlow(TaskInteractionState())
    private val interactions = mockk<TaskInteractionStateHolder>(relaxed = true) {
        every { state } returns interactionState
    }
    private val interactionFactory = mockk<TaskInteractionStateHolder.Factory> {
        every { create(any()) } returns interactions
    }

    private val observeTasksUseCase: ObserveTasksUseCase = mockk()
    private val moveTaskUseCase: MoveTaskUseCase = mockk(relaxUnitFun = true)
    private val atomAnalytics: AtomAnalytics = mockk(relaxUnitFun = true)
    private val shouldShowTaskOrderTutorialUseCase: ShouldShowTaskOrderTutorialUseCase =
        mockk(relaxUnitFun = true)
    private val taskOrderTutorialDismissedUseCase: TaskOrderTutorialDismissedUseCase =
        mockk(relaxUnitFun = true)
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val taskListSectionsEnabled = MutableStateFlow(false)

    @Before
    fun setUp() {
        givenOnboarding(false)
        initializeViewModel()
    }

    @Test
    fun `should react to task list section flag changes from settings`() = runTest {
        assertThat(sut.state.value.taskListSectionsEnabled).isFalse()

        taskListSectionsEnabled.value = true
        assertThat(sut.state.value.taskListSectionsEnabled).isTrue()

        taskListSectionsEnabled.value = false
        assertThat(sut.state.value.taskListSectionsEnabled).isFalse()
    }

    @Test
    fun `should load an enabled task list flag from settings`() = runTest {
        taskListSectionsEnabled.value = true
        initializeViewModel()

        assertThat(sut.state.value.taskListSectionsEnabled).isTrue()
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
    fun `should keep header expanded and load tasks when select a day`() = runTest {
        val selectedDate = LocalDate.now().plusDays(1)
        sut.toggleHeader()
        sut.setSelectedDay(selectedDate)

        sut.state.test {
            val state = expectMostRecentItem()
            assertThat(state.isHeaderExpanded).isTrue()
            assertThat(state.selectedDay.date).isEqualTo(selectedDate)
            assertThat(state.tasks).isEqualTo(TasksState.Success(emptyList<Task>().toImmutableList()))
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { observeTasksUseCase.invoke(ObserveTasksUseCase.Params(day = selectedDate)) }
        verify(exactly = 1) { atomAnalytics.track(AgendaAnalytics.ExpandCalendar) }
        verify(exactly = 0) { atomAnalytics.track(AgendaAnalytics.CollapseCalendar) }
        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.NavigateToDay(selectedDate.toString()))
        }
    }

    @Test
    fun `should keep header collapsed when select a day`() = runTest {
        val selectedDate = LocalDate.now().plusDays(1)
        sut.setSelectedDay(selectedDate)

        assertThat(sut.state.value.isHeaderExpanded).isFalse()
        assertThat(sut.state.value.selectedDay.date).isEqualTo(selectedDate)
        coVerify { observeTasksUseCase.invoke(ObserveTasksUseCase.Params(day = selectedDate)) }
    }

    @Test
    fun `should keep header expanded when select today`() = runTest {
        sut.setSelectedDay(LocalDate.now().plusMonths(1))
        sut.toggleHeader()

        sut.setSelectedDayToday()

        assertThat(sut.state.value.isHeaderExpanded).isTrue()
        assertThat(sut.state.value.selectedDay.date).isEqualTo(LocalDate.now())
        verify(exactly = 1) { atomAnalytics.track(AgendaAnalytics.SelectToday) }
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
    fun `should move dragged task to target index without changing completion status`() = runTest {
        val third = DEFAULT_TASKS.first().copy(id = 3L, position = 3)
        val expected = DEFAULT_TASKS + third
        coEvery { observeTasksUseCase.invoke(any()) } returns flowOf(expected.toResult())
        sut.loadTasks()

        sut.onDragTask(ItemPosition(0, TASK1_ID), ItemPosition(2, third.id))

        val forward = (sut.state.value.tasks as TasksState.Success).data
        assertThat(forward.map { it.id }).containsExactly(TASK2ID, third.id, TASK1_ID).inOrder()
        assertThat(forward.associate { it.id to it.isDone })
            .isEqualTo(expected.associate { it.id to it.isDone })

        sut.onDragTask(ItemPosition(2, TASK1_ID), ItemPosition(0, TASK2ID))

        assertThat((sut.state.value.tasks as TasksState.Success).data).containsExactlyElementsIn(expected).inOrder()
        sut.onDragStopped()
        coVerify {
            moveTaskUseCase(
                MoveTaskUseCase.Params(LocalDate.now(), expected.first().position, expected.first().position),
            )
        }
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
    fun `should track navigate to day when select a new day`() = runTest {
        every { observeTasksUseCase.invoke(any()) } returns flowOf(emptyList<Task>().toResult())
        val day = LocalDate.of(2023, 9, 16)

        sut.setSelectedDay(day)

        verify(exactly = 1) {
            atomAnalytics.track(AgendaAnalytics.NavigateToDay(day.toString()))
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
        givenOrderTasksTutorial(true)
        every { settingsRepository.observeTaskListSectionsEnabled() } returns taskListSectionsEnabled

        sut = AgendaViewModel(
            observeTasksUseCase = observeTasksUseCase,
            moveTaskUseCase = moveTaskUseCase,
            atomAnalytics = atomAnalytics,
            shouldShowTaskOrderTutorialUseCase = shouldShowTaskOrderTutorialUseCase,
            taskOrderTutorialDismissedUseCase = taskOrderTutorialDismissedUseCase,
            shouldShowOnboardingUseCase = shouldShowOnboardingUseCase,
            settingsRepository = settingsRepository,
            taskInteractionFactory = interactionFactory,
        )
    }
    @Test
    fun `task interactions use the screen scope expose holder state and delegate actions`() = runTest {
        val viewModel = sut
        verify { interactionFactory.create(viewModel.viewModelScope) }
        assertThat(viewModel.taskInteractionState).isSameInstanceAs(interactionState)
        viewModel.onMarkTask(1, true)
        viewModel.askDelete(2)
        viewModel.dismissDelete()
        viewModel.deleteTask(3)
        viewModel.deleteRecurringTask(4, RecurringRemovalStrategy.ALL)
        viewModel.onReviewFinished()
        verify { interactions.onMarkTask(1, true) }
        verify { interactions.askDelete(2) }
        verify { interactions.dismissDelete() }
        verify { interactions.deleteTask(3) }
        verify { interactions.deleteRecurringTask(4, RecurringRemovalStrategy.ALL) }
        verify { interactions.onReviewFinished() }
    }

}
