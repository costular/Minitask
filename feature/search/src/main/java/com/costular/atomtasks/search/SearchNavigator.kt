package com.costular.atomtasks.search

interface SearchNavigator {
    fun navigateUp()
    fun navigateToDetailScreenToEdit(taskId: Long)
    fun openTaskActions(taskId: Long, taskName: String, isDone: Boolean)
}
