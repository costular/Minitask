package com.costular.atomtasks.completedtasks

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.usecase.ObserveCompletedTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompletedTasksViewModel @Inject constructor(
    private val observeCompletedTasks: ObserveCompletedTasksUseCase,
    taskInteractionFactory: TaskInteractionStateHolder.Factory,
) : MviViewModel<CompletedTasksState>(CompletedTasksState.Loading) {
    private val taskInteractions = taskInteractionFactory.create(viewModelScope)
    val taskInteractionState = taskInteractions.state
    private val requests = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            requests.flatMapLatest { observeCompletedTasks(Unit) }.collect { result ->
                val next = result.fold(
                    ifError = { CompletedTasksState.Failure },
                    ifResult = { CompletedTasksState.Success(it.toImmutableList()) },
                )
                setState { next }
            }
        }
    }

    fun retry() {
        setState { CompletedTasksState.Loading }
        requests.value += 1
    }

    fun onMarkTask(taskId: Long, isDone: Boolean) = taskInteractions.onMarkTask(taskId, isDone)
    fun askDelete(taskId: Long) = taskInteractions.askDelete(taskId)
    fun dismissDelete() = taskInteractions.dismissDelete()
    fun deleteTask(taskId: Long) = taskInteractions.deleteTask(taskId)
    fun deleteRecurringTask(taskId: Long, strategy: RecurringRemovalStrategy) =
        taskInteractions.deleteRecurringTask(taskId, strategy)
    fun onReviewFinished() = taskInteractions.onReviewFinished()
}
