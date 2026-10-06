package com.costular.atomtasks.completedtasks

import com.costular.atomtasks.tasks.model.Task
import kotlinx.collections.immutable.ImmutableList

sealed interface CompletedTasksState {
    data object Loading : CompletedTasksState
    data object Failure : CompletedTasksState
    data class Success(val tasks: ImmutableList<Task>) : CompletedTasksState
}
