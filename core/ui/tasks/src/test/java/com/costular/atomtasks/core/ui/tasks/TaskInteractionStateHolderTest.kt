package com.costular.atomtasks.core.ui.tasks

import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.review.usecase.ShouldAskReviewUseCase
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.model.UpdateTaskIsDoneError
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
class TaskInteractionStateHolderTest {
    private val update = mockk<UpdateTaskIsDoneUseCase>()
    private val remove = mockk<RemoveTaskUseCase>()
    private val getTaskUseCase = mockk<GetTaskByIdUseCase>()
    private val review = mockk<ShouldAskReviewUseCase>()
    private val snackbar = mockk<SnackbarManager>(relaxUnitFun = true)
    private val analytics = mockk<AtomAnalytics>(relaxUnitFun = true)

    @Test
    fun `given a task when completed then its completion is persisted`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        coEvery { review(Unit) } returns true.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, true)
        runCurrent()

        coVerify(exactly = 1) { update(UpdateTaskIsDoneUseCase.Params(1, true)) }
    }

    @Test
    fun `given review eligibility when a task completes then review is requested`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        coEvery { review(Unit) } returns true.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, true)
        runCurrent()

        assertThat(holder.state.value.shouldShowReview).isTrue()
    }

    @Test
    fun `given a task when completed then completion feedback is shown`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        coEvery { review(Unit) } returns true.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, true)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_completed)) }
    }

    @Test
    fun `given a task when completed then completion analytics are tracked`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        coEvery { review(Unit) } returns true.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, true)
        runCurrent()

        verify(exactly = 1) { analytics.track(match { it.name == "agenda_mark_task_as_done" }) }
    }

    @Test
    fun `given a pending review when review finishes then the request is cleared`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        coEvery { review(Unit) } returns true.toResult()
        val holder = holder(backgroundScope)
        holder.onMarkTask(1, true)
        runCurrent()

        holder.onReviewFinished()

        assertThat(holder.state.value.shouldShowReview).isFalse()
    }

    @Test
    fun `given a completed task when reopened then review is skipped`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, false)
        runCurrent()

        coVerify(exactly = 0) { review(Unit) }
    }

    @Test
    fun `given a completed task when reopened then reopening feedback is shown`() = runTest {
        coEvery { update(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, false)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_reopened)) }
    }

    @Test
    fun `given a completion failure when marking a task then error feedback is shown`() = runTest {
        coEvery { update(any()) } returns Either.Error(UpdateTaskIsDoneError.UnknownError)
        val holder = holder(backgroundScope)

        holder.onMarkTask(1, true)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.error_generic)) }
    }

    @Test
    fun `given a recurring task when deletion is requested then metadata is loaded by id`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns
            flowOf(TaskToday.copy(id = 1, isRecurring = true))
        val holder = holder(backgroundScope)

        holder.askDelete(1)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Shown(1, true))
    }

    @Test
    fun `given a deletion dialog when dismissed then the dialog is hidden`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns
            flowOf(TaskToday.copy(id = 1, isRecurring = true))
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.dismissDelete()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a deletion dialog when dismissed then cancellation is tracked`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns
            flowOf(TaskToday.copy(id = 1, isRecurring = true))
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.dismissDelete()

        verify(exactly = 1) { analytics.track(match { it.name == "agenda_cancel_delete_task" }) }
    }

    @Test
    fun `given a missing task when deletion is requested then no dialog is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns flowOf(null)
        val holder = holder(backgroundScope)

        holder.askDelete(1)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a missing task when deletion is requested then error feedback is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns flowOf(null)
        val holder = holder(backgroundScope)

        holder.askDelete(1)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.error_generic)) }
    }

    @Test
    fun `given recurring deletion of all occurrences when confirmed then the selected strategy is forwarded`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.ALL)
        runCurrent()

        coVerify(exactly = 1) { remove(RemoveTaskUseCase.Params(1, RecurringRemovalStrategy.ALL)) }
    }

    @Test
    fun `given recurring deletion of all occurrences when confirmed then matching feedback is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.ALL)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_deleted_all)) }
    }

    @Test
    fun `given recurring deletion of future occurrences when confirmed then the selected strategy is forwarded`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.FUTURE_ONES)
        runCurrent()

        coVerify(exactly = 1) { remove(RemoveTaskUseCase.Params(1, RecurringRemovalStrategy.FUTURE_ONES)) }
    }

    @Test
    fun `given recurring deletion of future occurrences when confirmed then matching feedback is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.FUTURE_ONES)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_deleted_future)) }
    }

    @Test
    fun `given recurring deletion of this and future occurrences when confirmed then the selected strategy is forwarded`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES)
        runCurrent()

        coVerify(exactly = 1) { remove(RemoveTaskUseCase.Params(1, RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES)) }
    }

    @Test
    fun `given recurring deletion of this and future occurrences when confirmed then matching feedback is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_deleted_future)) }
    }

    @Test
    fun `given ordinary deletion when confirmed then no recurring strategy is forwarded`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteTask(1)
        runCurrent()

        coVerify(exactly = 1) { remove(RemoveTaskUseCase.Params(1, null)) }
    }

    @Test
    fun `given ordinary deletion when confirmed then deletion feedback is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteTask(1)
        runCurrent()

        verify(exactly = 1) { snackbar.showMessage(AppSnackbarMessage(R.string.task_feedback_deleted)) }
    }

    @Test
    fun `given ordinary deletion when confirmed then confirmation is tracked`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        val holder = holder(backgroundScope)

        holder.deleteTask(1)
        runCurrent()

        verify(exactly = 1) { analytics.track(match { it.name == "agenda_confirm_delete_task" }) }
    }

    @Test
    fun `given a pending lookup when dismissed then no late dialog is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.dismissDelete()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a pending lookup when dismissed then no dialog analytics are tracked`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.dismissDelete()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { analytics.track(match { it.name == "agenda_show_confirm_delete_dialog" }) }
    }

    @Test
    fun `given a pending lookup when dismissed then no error feedback is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.dismissDelete()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given a pending lookup when ordinary deletion is confirmed then no late dialog is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.deleteTask(1)
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a pending lookup when recurring deletion is confirmed then no late dialog is shown`() = runTest {
        coEvery { remove(any()) } returns Unit.toResult()
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.deleteRecurringTask(1, RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES)
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a pending lookup when another deletion is requested then the latest task owns the dialog`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns lateLookup(null)
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(2)) } returns flowOf(TaskToday.copy(id = 2))
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.askDelete(2)
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Shown(2, false))
    }

    @Test
    fun `given a pending lookup when another deletion is requested then only the latest dialog is tracked`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns lateLookup(null)
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(2)) } returns flowOf(TaskToday.copy(id = 2))
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.askDelete(2)
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 1) { analytics.track(match { it.name == "agenda_show_confirm_delete_dialog" }) }
    }

    @Test
    fun `given a pending lookup when another deletion is requested then the cancelled missing task produces no feedback`() = runTest {
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(1)) } returns lateLookup(null)
        coEvery { getTaskUseCase(GetTaskByIdUseCase.Params(2)) } returns flowOf(TaskToday.copy(id = 2))
        val holder = holder(backgroundScope)
        holder.askDelete(1)
        runCurrent()

        holder.askDelete(2)
        runCurrent()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given pending completion when the owner is cancelled then state remains unchanged`() = runTest {
        coEvery { update(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.onMarkTask(1, true)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value).isEqualTo(TaskInteractionState())
    }

    @Test
    fun `given pending completion when the owner is cancelled then no feedback is shown`() = runTest {
        coEvery { update(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.onMarkTask(1, true)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given pending completion when the owner is cancelled then no analytics are tracked`() = runTest {
        coEvery { update(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.onMarkTask(1, true)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { analytics.track(any()) }
    }

    @Test
    fun `given pending deletion when the owner is cancelled then state remains unchanged`() = runTest {
        coEvery { remove(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.deleteTask(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value).isEqualTo(TaskInteractionState())
    }

    @Test
    fun `given pending deletion when the owner is cancelled then no feedback is shown`() = runTest {
        coEvery { remove(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.deleteTask(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given pending deletion when the owner is cancelled then no analytics are tracked`() = runTest {
        coEvery { remove(any()) } coAnswers { delay(100); Unit.toResult() }
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.deleteTask(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { analytics.track(any()) }
    }

    @Test
    fun `given pending lookup when the owner is cancelled then state remains unchanged`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.askDelete(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertThat(holder.state.value).isEqualTo(TaskInteractionState())
    }

    @Test
    fun `given pending lookup when the owner is cancelled then no feedback is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.askDelete(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given pending lookup when the owner is cancelled then no analytics are tracked`() = runTest {
        coEvery { getTaskUseCase(any()) } returns lateLookup(TaskToday)
        val owner = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
        val holder = holder(owner)
        holder.askDelete(1)
        runCurrent()

        owner.cancel()
        advanceTimeBy(100)
        runCurrent()

        verify(exactly = 0) { analytics.track(any()) }
    }

    @Test
    fun `given a cancelled lookup when deletion is requested then no dialog is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns flow { throw CancellationException("Cancelled lookup") }
        val holder = holder(backgroundScope)

        holder.askDelete(1)
        runCurrent()

        assertThat(holder.state.value.removal).isEqualTo(RemoveTaskConfirmationUiState.Hidden)
    }

    @Test
    fun `given a cancelled lookup when deletion is requested then no error feedback is shown`() = runTest {
        coEvery { getTaskUseCase(any()) } returns flow { throw CancellationException("Cancelled lookup") }
        val holder = holder(backgroundScope)

        holder.askDelete(1)
        runCurrent()

        verify(exactly = 0) { snackbar.showMessage(any()) }
    }

    @Test
    fun `given two holders when one requests deletion then the other keeps its own state`() = runTest {
        coEvery { getTaskUseCase(any()) } returns flowOf(TaskToday.copy(id = 1))
        val first = holder(backgroundScope)
        val second = holder(backgroundScope)

        first.askDelete(1)
        runCurrent()

        assertThat(second.state.value).isEqualTo(TaskInteractionState())
    }

    private fun holder(scope: CoroutineScope) = TaskInteractionStateHolder(
        updateTask = update,
        removeTask = remove,
        getTask = getTaskUseCase,
        shouldAskReview = review,
        snackbar = snackbar,
        analytics = analytics,
        scope = scope,
    )

    // Deliberately bypass Flow's cancellation checks to model a non-cooperative dependency.
    private fun lateLookup(task: Task?): Flow<Task?> = object : Flow<Task?> {
        override suspend fun collect(collector: FlowCollector<Task?>) {
            withContext(NonCancellable) {
                delay(100)
                collector.emit(task)
            }
        }
    }
}
