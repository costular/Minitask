package com.costular.atomtasks.core.ui.tasks

import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.analytics.TrackingEvent
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.review.usecase.ShouldAskReviewUseCase
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskInteractionState(
    val removal: RemoveTaskConfirmationUiState = RemoveTaskConfirmationUiState.Hidden,
    val shouldShowReview: Boolean = false,
)

@Suppress("LongParameterList")
class TaskInteractionStateHolder @AssistedInject constructor(
    private val updateTask: UpdateTaskIsDoneUseCase,
    private val removeTask: RemoveTaskUseCase,
    private val getTask: GetTaskByIdUseCase,
    private val shouldAskReview: ShouldAskReviewUseCase,
    private val snackbar: SnackbarManager,
    private val analytics: AtomAnalytics,
    @Assisted private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(TaskInteractionState())
    val state: StateFlow<TaskInteractionState> = mutableState.asStateFlow()

    @AssistedFactory
    interface Factory {
        fun create(scope: CoroutineScope): TaskInteractionStateHolder
    }

    private var removalLookup: Job? = null
    private var removalRequest = 0L

    fun onMarkTask(taskId: Long, isDone: Boolean) = scope.launch {
        updateTask(UpdateTaskIsDoneUseCase.Params(taskId, isDone)).fold(
            ifError = { feedback(R.string.error_generic) },
            ifResult = {
                if (isDone) {
                    shouldAskReview(Unit).tap { shouldShowReview ->
                        mutableState.update { it.copy(shouldShowReview = shouldShowReview) }
                    }
                }
                feedback(if (isDone) R.string.task_feedback_completed else R.string.task_feedback_reopened)
                track(if (isDone) "agenda_mark_task_as_done" else "agenda_mark_task_as_not_done")
            },
        )
    }

    fun askDelete(taskId: Long) {
        cancelRemovalLookup()
        val request = removalRequest
        removalLookup = scope.launch {
            try {
                val task = getTask(GetTaskByIdUseCase.Params(taskId)).first()
                currentCoroutineContext().ensureActive()
                if (request != removalRequest) return@launch
                if (task == null) {
                    feedback(R.string.error_generic)
                } else {
                    mutableState.update {
                        it.copy(removal = RemoveTaskConfirmationUiState.Shown(task.id, task.isRecurring))
                    }
                    track("agenda_show_confirm_delete_dialog")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (request == removalRequest) feedback(R.string.error_generic)
            }
        }
    }

    private fun cancelRemovalLookup() {
        removalRequest++
        removalLookup?.cancel()
        removalLookup = null
    }

    fun dismissDelete() {
        cancelRemovalLookup()
        mutableState.update { it.copy(removal = RemoveTaskConfirmationUiState.Hidden) }
        track("agenda_cancel_delete_task")
    }

    fun deleteTask(taskId: Long) = delete(taskId, null)

    fun deleteRecurringTask(taskId: Long, strategy: RecurringRemovalStrategy) = delete(taskId, strategy)

    private fun delete(taskId: Long, strategy: RecurringRemovalStrategy?) {
        cancelRemovalLookup()
        mutableState.update { it.copy(removal = RemoveTaskConfirmationUiState.Hidden) }
        scope.launch {
            removeTask(RemoveTaskUseCase.Params(taskId, strategy)).fold(
                ifError = { feedback(R.string.error_generic) },
                ifResult = {
                    val message = when (strategy) {
                        RecurringRemovalStrategy.ALL -> R.string.task_feedback_deleted_all
                        RecurringRemovalStrategy.FUTURE_ONES,
                        RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES -> R.string.task_feedback_deleted_future
                        else -> R.string.task_feedback_deleted
                    }
                    feedback(message)
                    track("agenda_confirm_delete_task")
                },
            )
        }
    }

    fun onReviewFinished() = mutableState.update { it.copy(shouldShowReview = false) }

    private fun feedback(messageRes: Int) = snackbar.showMessage(AppSnackbarMessage(messageRes))

    private fun track(name: String) = analytics.track(TrackingEvent(name))
}
