package com.costular.atomtasks.core.ui.tasks

import androidx.compose.runtime.Composable
import com.costular.atomtasks.core.ui.tasks.actions.TaskActionsResult
import com.costular.atomtasks.review.ui.ReviewHandler
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiHandler
import com.ramcosta.composedestinations.generated.taskactions.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient

@Suppress("LongParameterList")
@Composable
fun TaskInteractionEffects(
    state: TaskInteractionState,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
    onEdit: (Long) -> Unit,
    onAskDelete: (Long) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    onDismissDelete: () -> Unit,
    onDelete: (Long) -> Unit,
    onDeleteRecurring: (Long, RecurringRemovalStrategy) -> Unit,
    onReviewFinished: () -> Unit,
) {
    resultRecipient.onNavResult { result ->
        if (result is NavResult.Value) {
            when (val action = result.value) {
                is TaskActionsResult.Edit -> onEdit(action.taskId)
                is TaskActionsResult.Remove -> onAskDelete(action.taskId)
                is TaskActionsResult.MarkAsDone -> onMarkTask(action.taskId, true)
                is TaskActionsResult.MarkAsNotDone -> onMarkTask(action.taskId, false)
            }
        }
    }
    RemoveTaskConfirmationUiHandler(
        uiState = state.removal,
        onDismiss = onDismissDelete,
        onDelete = onDelete,
        onDeleteRecurring = onDeleteRecurring,
    )
    ReviewHandler(state.shouldShowReview, onReviewFinished)
}
