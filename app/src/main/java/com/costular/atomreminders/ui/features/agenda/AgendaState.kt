package com.costular.atomreminders.ui.features.agenda

import com.costular.atomreminders.domain.Async
import com.costular.atomreminders.domain.model.Task
import com.costular.atomreminders.ui.features.agenda.AgendaViewModel.Companion.DaysAfter
import com.costular.atomreminders.ui.features.agenda.AgendaViewModel.Companion.DaysBefore
import java.time.LocalDate

data class AgendaState(
    val selectedDay: LocalDate = LocalDate.now(),
    val tasks: Async<List<Task>> = Async.Uninitialized,
    val taskAction: Task? = null,
    val deleteTaskAction: DeleteTaskAction = DeleteTaskAction.Hidden,
    val editTaskAction: EditTaskAction = EditTaskAction.Hidden,
) {
    val calendarFromDate: LocalDate = LocalDate.now().minusDays(DaysBefore.toLong())
    val calendarUntilDate: LocalDate = LocalDate.now().plusDays(DaysAfter.toLong())

    val isPreviousDaySelected get() = calendarFromDate.isBefore(selectedDay)
    val isNextDaySelected get() = calendarUntilDate.isAfter(selectedDay)

    fun makeActionIfPossible(body: (Task) -> Unit) {
        taskAction?.let(body)
    }

    companion object {
        val Empty = AgendaState()
    }

}

sealed class DeleteTaskAction {

    object Hidden : DeleteTaskAction()

    data class Shown(
        val taskId: Long,
    ) : DeleteTaskAction()

}


sealed class EditTaskAction {

    object Hidden : EditTaskAction()

    data class Shown(
        val taskId: Long,
    ) : EditTaskAction()

}