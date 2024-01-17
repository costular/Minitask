package com.costular.atomtasks.tasks.model

sealed interface PopulateRecurringTasksError {
    data object NotRecurringTask : PopulateRecurringTasksError
    data object UnknownError : PopulateRecurringTasksError
}
