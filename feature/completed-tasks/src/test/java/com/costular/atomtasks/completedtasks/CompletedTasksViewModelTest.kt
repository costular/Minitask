package com.costular.atomtasks.completedtasks

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.testing.MainCoroutineRule
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.core.ui.tasks.TaskInteractionState
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.ObserveTasksError
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.usecase.ObserveCompletedTasksUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CompletedTasksViewModelTest {
    @get:Rule
    val main = MainCoroutineRule(StandardTestDispatcher())
    private val interactions = mockk<TaskInteractionStateHolder>(relaxed = true) {
        every { state } returns MutableStateFlow(TaskInteractionState())
    }
    private val factory = mockk<TaskInteractionStateHolder.Factory> {
        every { create(any()) } returns interactions
    }
    private val observe = mockk<ObserveCompletedTasksUseCase>()

    @Test
    fun `screen starts loading and observes live removal of completed tasks`() = runTest(main.testDispatcher) {
        val task = TaskToday.copy(isDone = true)
        val updates = MutableStateFlow(listOf(task).toResult())
        every { observe(Unit) } returns updates
        val viewModel = CompletedTasksViewModel(observe, factory)
        assertThat(viewModel.state.value).isEqualTo(CompletedTasksState.Loading)
        runCurrent()
        assertThat((viewModel.state.value as CompletedTasksState.Success).tasks).containsExactly(task)
        updates.value = emptyList<Task>().toResult()
        runCurrent()
        assertThat((viewModel.state.value as CompletedTasksState.Success).tasks).isEmpty()
    }

    @Test
    fun `failed observation can be retried successfully`() = runTest(main.testDispatcher) {
        every { observe(Unit) } returns flowOf(Either.Error(ObserveTasksError.UnknownError))
        val viewModel = CompletedTasksViewModel(observe, factory)
        runCurrent()
        assertThat(viewModel.state.value).isEqualTo(CompletedTasksState.Failure)
        every { observe(Unit) } returns flowOf(emptyList<Task>().toResult())
        viewModel.retry()
        assertThat(viewModel.state.value).isEqualTo(CompletedTasksState.Loading)
        runCurrent()
        assertThat((viewModel.state.value as CompletedTasksState.Success).tasks).isEmpty()
    }

    @Test
    fun `task actions are delegated to the shared interaction holder`() {
        val viewModel = CompletedTasksViewModel(observe, factory)
        viewModel.onMarkTask(1, false)
        viewModel.askDelete(2)
        viewModel.deleteTask(2)
        viewModel.dismissDelete()
        verify { interactions.onMarkTask(1, false) }
        verify { interactions.askDelete(2) }
        verify { interactions.deleteTask(2) }
        verify { interactions.dismissDelete() }
    }
}
