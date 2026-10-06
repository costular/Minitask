package com.costular.atomtasks.verticaltasks

interface VerticalTasksNavigator {
    fun navigateToCompletedTasks()
    fun navigateToSearch()
    fun navigateToDetailScreenForCreateTask(date: String)
    fun navigateToDetailScreenToEdit(taskId: Long)
    fun openTaskActions(taskId: Long, taskName: String, isDone: Boolean)
}
