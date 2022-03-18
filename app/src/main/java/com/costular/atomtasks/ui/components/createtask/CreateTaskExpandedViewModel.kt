package com.costular.atomtasks.ui.components.createtask

import com.costular.atomtasks.ui.mvi.MviViewModel
import java.time.LocalDate
import java.time.LocalTime

class CreateTaskExpandedViewModel :
    MviViewModel<CreateTaskExpandedState>(CreateTaskExpandedState.Empty) {

    fun setName(name: String) {
        setState {
            copy(name = name)
        }
    }

    fun setDate(localDate: LocalDate) {
        setState {
            copy(date = localDate)
        }
    }

    fun setReminder(localTime: LocalTime?) {
        setState {
            copy(reminder = localTime)
        }
    }

    fun selectTaskData(taskDataSelection: TaskDataSelection) {
        val computedSelection = if (state.value.taskDataSelection == taskDataSelection) {
            TaskDataSelection.None
        } else {
            taskDataSelection
        }

        setState {
            copy(taskDataSelection = computedSelection)
        }
    }

    fun requestSave() {
        sendEvent(
            CreateTaskUiEvents.SaveTask(
                state.value.asCreateTaskResult()
            )
        )
        setState {
            CreateTaskExpandedState.Empty
        }
    }

    private fun CreateTaskExpandedState.asCreateTaskResult(): CreateTaskResult =
        CreateTaskResult(
            name = name,
            date = date,
            reminder = reminder
        )
}