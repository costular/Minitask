package com.costular.atomtasks.completedtasks

interface CompletedTasksNavigator {
    fun navigateUp()
    fun navigateToDetailScreenToEdit(taskId: Long)
    fun openTaskActions(taskId: Long, taskName: String, isDone: Boolean)
}
