package com.costular.atomtasks.agenda

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core_ui.mvi.MviViewModel
import com.costular.atomtasks.data.tasks.GetTasksInteractor
import com.costular.atomtasks.data.tasks.RemoveTaskInteractor
import com.costular.atomtasks.data.tasks.Task
import com.costular.atomtasks.data.tasks.UpdateTaskIsDoneInteractor
import com.costular.core.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val getTasksInteractor: GetTasksInteractor,
    private val updateTaskIsDoneInteractor: UpdateTaskIsDoneInteractor,
    private val removeTaskInteractor: RemoveTaskInteractor,
) : MviViewModel<AgendaState>(AgendaState()) {

    init {
        loadTasks()
    }

    fun setSelectedDay(localDate: LocalDate) = viewModelScope.launch {
        setState { copy(selectedDay = localDate) }
        loadTasks()
    }

    fun loadTasks() = viewModelScope.launch {
        getTasksInteractor(GetTasksInteractor.Params(day = state.value.selectedDay))
        getTasksInteractor.flow
            .onStart { setState { copy(tasks = Async.Loading) } }
            .catch { setState { copy(tasks = Async.Failure(it)) } }
            .collect { setState { copy(tasks = Async.Success(it)) } }
    }

    fun onMarkTask(taskId: Long, isDone: Boolean) = viewModelScope.launch {
        dismissTaskAction()
        updateTaskIsDoneInteractor(UpdateTaskIsDoneInteractor.Params(taskId, isDone)).collect()
    }

    fun openTaskAction(task: Task) {
        setState {
            copy(taskAction = task)
        }
    }

    fun dismissTaskAction() {
        setState {
            copy(taskAction = null)
        }
    }

    fun actionDelete(id: Long) {
        setState {
            copy(
                taskAction = null,
                deleteTaskAction = DeleteTaskAction.Shown(id),
            )
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            removeTaskInteractor(RemoveTaskInteractor.Params(id))
                .onStart {
                    hideAskDelete()
                }
                .collect()
        }
    }

    fun dismissDelete() {
        hideAskDelete()
    }

    private fun hideAskDelete() {
        setState { copy(deleteTaskAction = DeleteTaskAction.Hidden) }
    }

    companion object {
        const val DaysBefore = 30
        const val DaysAfter = 90
    }
}
