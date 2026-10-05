package com.costular.atomtasks.agenda.ui

import com.costular.atomtasks.core.ui.date.Day
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.tasks.model.Task
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList

data class AgendaState(
    val selectedDay: Day = LocalDate.now().asDay(),
    val tasks: TasksState = TasksState.Uninitialized,
    val taskListSectionsEnabled: Boolean = false,
    val isHeaderExpanded: Boolean = false,
    val fromToPositions: Pair<Int, Int>? = null,
    val shouldShowCardOrderTutorial: Boolean = false,
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
