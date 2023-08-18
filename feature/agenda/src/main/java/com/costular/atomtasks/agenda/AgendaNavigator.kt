package com.costular.atomtasks.agenda

interface AgendaNavigator {
    fun navigateToCreateTask(
        date: String,
        text: String?,
    )

    fun navigateToEditTask(
        taskId: Long,
    )
}