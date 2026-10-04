package com.costular.atomtasks.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.testing.MainCoroutineRule
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.core.ui.tasks.TaskInteractionState
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.ObserveTasksError
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.usecase.ObserveSearchTasksUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SearchViewModelTest {
    @get:Rule
    val main = MainCoroutineRule(StandardTestDispatcher())
    private val interactionState = MutableStateFlow(TaskInteractionState())
    private val interactions = mockk<TaskInteractionStateHolder>(relaxed = true) {
        every { state } returns interactionState
    }
    private val interactionFactory = mockk<TaskInteractionStateHolder.Factory> {
        every { create(any()) } returns interactions
    }

    private val search = mockk<ObserveSearchTasksUseCase>()

    @Test
    fun `given short input when the query changes then results stay idle`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onQueryChange(" a ")
        runCurrent()

        assertThat(viewModel.state.value.results).isEqualTo(SearchResults.Idle)
    }

    @Test
    fun `given short input when the query changes then no search starts`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onQueryChange(" a ")
        runCurrent()

        verify(exactly = 0) { search(any()) }
    }

    @Test
    fun `given blank input when the query changes then results stay idle`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onQueryChange("  ")
        runCurrent()

        assertThat(viewModel.state.value.results).isEqualTo(SearchResults.Idle)
    }

    @Test
    fun `given blank input when the query changes then no search starts`() = runTest(main.testDispatcher) {
        val viewModel = viewModel()

        viewModel.onQueryChange("  ")
        runCurrent()

        verify(exactly = 0) { search(any()) }
    }

    @Test
    fun `given a changed query when the debounce has not elapsed then no search starts`() = runTest(main.testDispatcher) {
        every { search(any()) } returns flowOf(emptyList<Task>().toResult())
        val viewModel = viewModel()
        viewModel.onQueryChange("gy")
        runCurrent()
        advanceTimeBy(200)
        viewModel.onQueryChange("gym")
        runCurrent()

        advanceTimeBy(299)
        runCurrent()

        verify(exactly = 0) { search(any()) }
    }

    @Test
    fun `given rapid query changes when the debounce elapses then only the final query is searched`() = runTest(main.testDispatcher) {
        every { search(any()) } returns flowOf(emptyList<Task>().toResult())
        val viewModel = viewModel()
        viewModel.onQueryChange("gy")
        runCurrent()
        advanceTimeBy(200)
        viewModel.onQueryChange("gym")
        runCurrent()

        advanceTimeBy(300)
        runCurrent()

        verify(exactly = 1) { search(any()) }
        verify(exactly = 1) { search("gym") }
    }

    @Test
    fun `given rapid query changes when the debounce elapses then an empty result is published`() = runTest(main.testDispatcher) {
        every { search(any()) } returns flowOf(emptyList<Task>().toResult())
        val viewModel = viewModel()
        viewModel.onQueryChange("gy")
        runCurrent()
        advanceTimeBy(200)
        viewModel.onQueryChange("gym")
        runCurrent()

        advanceTimeBy(300)
        runCurrent()

        assertThat((viewModel.state.value.results as SearchResults.Success).tasks).isEmpty()
    }

    @Test
    fun `given an active search when cleared then results become idle immediately`() = runTest(main.testDispatcher) {
        every { search("gym") } returns flow { awaitCancellation() }
        val viewModel = viewModel()
        viewModel.onQueryChange("gym")
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onQueryChange("")

        assertThat(viewModel.state.value.results).isEqualTo(SearchResults.Idle)
    }

    @Test
    fun `given an active search when cleared then its observation is cancelled`() = runTest(main.testDispatcher) {
        var cancelled = false
        every { search("gym") } returns flow {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val viewModel = viewModel()
        viewModel.onQueryChange("gym")
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onQueryChange("")
        runCurrent()

        assertThat(cancelled).isTrue()
    }

    @Test
    fun `given a delayed old result when a new query is debouncing then the old result is ignored`() = runTest(main.testDispatcher) {
        every { search("old") } returns flow { delay(100); emit(listOf(TaskToday).toResult()) }
        every { search("new") } returns flowOf(emptyList<Task>().toResult())
        val viewModel = viewModel()
        viewModel.onQueryChange("old")
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onQueryChange("new")
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        assertThat(viewModel.state.value.results).isEqualTo(SearchResults.Loading)
    }

    @Test
    fun `given a delayed old result when the new query completes then only new results appear`() = runTest(main.testDispatcher) {
        every { search("old") } returns flow { delay(100); emit(listOf(TaskToday).toResult()) }
        every { search("new") } returns flowOf(emptyList<Task>().toResult())
        val viewModel = viewModel()
        viewModel.onQueryChange("old")
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onQueryChange("new")
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertThat((viewModel.state.value.results as SearchResults.Success).tasks).isEmpty()
    }

    @Test
    fun `given a restored query when observation fails then failure is published`() = runTest(main.testDispatcher) {
        every { search("gym") } returns flowOf(Either.Error(ObserveTasksError.UnknownError))
        val viewModel = viewModel(SavedStateHandle(mapOf("search_query" to "gym")))

        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertThat(viewModel.state.value.results).isEqualTo(SearchResults.Failure)
    }

    @Test
    fun `given a failed search when retried successfully then matching tasks are published`() = runTest(main.testDispatcher) {
        every { search("gym") } returns flowOf(Either.Error(ObserveTasksError.UnknownError))
        val viewModel = viewModel(SavedStateHandle(mapOf("search_query" to "gym")))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        every { search("gym") } returns flowOf(listOf(TaskToday).toResult())

        viewModel.retry()
        runCurrent()

        assertThat((viewModel.state.value.results as SearchResults.Success).tasks).containsExactly(TaskToday)
    }

    @Test
    fun `given a failed search when retried successfully then the restored query is retained`() = runTest(main.testDispatcher) {
        every { search("gym") } returns flowOf(Either.Error(ObserveTasksError.UnknownError))
        val viewModel = viewModel(SavedStateHandle(mapOf("search_query" to "gym")))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        every { search("gym") } returns flowOf(listOf(TaskToday).toResult())

        viewModel.retry()
        runCurrent()

        assertThat(viewModel.state.value.query).isEqualTo("gym")
    }

    @Test
    fun `given observed tasks when the database updates then results update`() = runTest(main.testDispatcher) {
        val updates = MutableStateFlow(listOf(TaskToday).toResult())
        every { search("gym") } returns updates
        val viewModel = viewModel(SavedStateHandle(mapOf("search_query" to "gym")))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        updates.value = listOf(TaskToday.copy(isDone = true)).toResult()
        runCurrent()

        assertThat((viewModel.state.value.results as SearchResults.Success).tasks)
            .containsExactly(TaskToday.copy(isDone = true))
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

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()) = SearchViewModel(
        searchTasks = search,
        savedState = saved,
        taskInteractionFactory = interactionFactory,
    )
}
