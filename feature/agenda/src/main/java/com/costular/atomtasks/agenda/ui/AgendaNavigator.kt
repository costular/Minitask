package com.costular.atomtasks.agenda.ui

interface AgendaNavigator {
    fun navigateToDetailScreenForCreateTask(
        date: String,
    )

    fun navigateToDetailScreenToEdit(
        taskId: Long,
    )

    fun openTaskActions(
        taskId: Long,
        taskName: String,
        isDone: Boolean,
    )

    fun navigateToSearch()

    fun navigateToOnboarding()
}
