package com.costular.atomtasks.agenda.ui

import com.costular.atomtasks.core.ui.date.Day
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList

data class AgendaState(
    val selectedDay: Day = LocalDate.now().asDay(),
    val tasks: TasksState = TasksState.Uninitialized,
    val removeTaskConfirmationUiState: RemoveTaskConfirmationUiState = RemoveTaskConfirmationUiState.Hidden,
    val isHeaderExpanded: Boolean = false,
    val fromToPositions: Pair<Int, Int>? = null,
    val shouldShowCardOrderTutorial: Boolean = false,
    val shouldShowReviewDialog: Boolean = false,
    val shouldShowCalendarView: Boolean = false,
) {
    companion object {
        val Empty = AgendaState()
    }
}

sealed interface TasksState {

    data object Uninitialized : TasksState

    data object Loading : TasksState

    data object Failure : TasksState

    data class Success(
        val data: ImmutableList<Task>
    ): TasksState

}
