package com.costular.atomtasks.agenda.ui

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.CancelDelete
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.CollapseCalendar
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.ConfirmDelete
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.ExpandCalendar
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.MarkTaskAsDone
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.MarkTaskAsNotDone
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.NavigateToDay
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.OrderTask
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.SelectToday
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.ShowConfirmDeleteDialog
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.usecase.EmptyParams
import com.costular.atomtasks.data.tutorial.ShouldShowOnboardingUseCase
import com.costular.atomtasks.data.tutorial.ShouldShowTaskOrderTutorialUseCase
import com.costular.atomtasks.data.tutorial.TaskOrderTutorialDismissedUseCase
import com.costular.atomtasks.review.usecase.ShouldAskReviewUseCase
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val updateTaskIsDoneUseCase: UpdateTaskIsDoneUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
    private val autoforwardManager: AutoforwardManager,
    private val moveTaskUseCase: MoveTaskUseCase,
    private val atomAnalytics: AtomAnalytics,
    private val shouldShowTaskOrderTutorialUseCase: ShouldShowTaskOrderTutorialUseCase,
    private val taskOrderTutorialDismissedUseCase: TaskOrderTutorialDismissedUseCase,
    private val shouldShowAskReviewUseCase: ShouldAskReviewUseCase,
    private val recurrenceScheduler: RecurrenceScheduler,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
    private val snackbarManager: SnackbarManager,
) : MviViewModel<AgendaState>(AgendaState()) {
    init {
        shouldShowOnboarding()
        loadTasks()
        scheduleAutoforwardTasks()
        initializeRecurrenceScheduler()
        retrieveTutorials()
    }

    private fun initializeRecurrenceScheduler() {
        recurrenceScheduler.initialize()
    }

    private fun shouldShowOnboarding() {
        viewModelScope.launch {
            shouldShowOnboardingUseCase.invoke(EmptyParams).tap { result ->
                result.collectLatest {
                    if (it) {
                        sendEvent(AgendaUiEvents.OpenOnboarding)
                    }
                }
            }
        }
    }

    private fun retrieveTutorials() {
        viewModelScope.launch {
            shouldShowTaskOrderTutorialUseCase(Unit)
                .collect {
                    setState { copy(shouldShowCardOrderTutorial = it) }
                }
        }
    }

    private fun scheduleAutoforwardTasks() {
        viewModelScope.launch {
            autoforwardManager.scheduleOrCancelAutoforwardTasks()
        }
    }

    fun setSelectedDayToday() {
        setSelectedDay(LocalDate.now())
        atomAnalytics.track(SelectToday)
    }

    fun setSelectedDay(localDate: LocalDate) = viewModelScope.launch {
        setState { copy(selectedDay = localDate.asDay(), isHeaderExpanded = false) }
        loadTasks()
        atomAnalytics.track(NavigateToDay(localDate.toString()))
    }

    private var loadTasksJob: Job? = null

    fun loadTasks() {
        loadTasksJob?.cancel()
        loadTasksJob = viewModelScope.launch {
            observeTasksUseCase.invoke(ObserveTasksUseCase.Params(day = state.value.selectedDay.date))
                .onStart { setState { copy(tasks = TasksState.Loading) } }
                .collect {
                    it.fold(
                        ifError = {
                            setState { copy(tasks = TasksState.Failure) }
                        },
                        ifResult = { tasks ->
                            setState { copy(tasks = TasksState.Success(tasks.toImmutableList())) }
                        }
                    )
                }
        }
    }

    fun onMarkTask(taskId: Long, isDone: Boolean) = viewModelScope.launch {
        updateTaskIsDoneUseCase(UpdateTaskIsDoneUseCase.Params(taskId, isDone)).fold(
            ifError = {
                sendGenericError()
            },
            ifResult = {
                checkIfReviewShouldBeShown(isDone)
                showSnackbar(
                    if (isDone) {
                        R.string.task_feedback_completed
                    } else {
                        R.string.task_feedback_reopened
                    }
                )

                val event = if (isDone) {
                    MarkTaskAsDone
                } else {
                    MarkTaskAsNotDone
                }
                atomAnalytics.track(event)
            }
        )
    }

    private suspend fun checkIfReviewShouldBeShown(isDone: Boolean) {
        if (isDone) {
            val result = shouldShowAskReviewUseCase(Unit)
            result.fold(
                ifError = {
                    Unit
                },
                ifResult = {
                    setState { copy(shouldShowReviewDialog = it) }
                }
            )
        }
    }

    fun onReviewFinished() {
        setState { copy(shouldShowReviewDialog = false) }
    }

    fun actionDelete(id: Long) {
        val tasks = state.value.tasks

        if (tasks !is TasksState.Success) {
            return
        }

        val task = tasks.data.find { it.id == id }
        setState {
            copy(
                removeTaskConfirmationUiState = RemoveTaskConfirmationUiState.Shown(
                    taskId = id,
                    isRecurring = task?.isRecurring == true,
                )
            )
        }

        atomAnalytics.track(ShowConfirmDeleteDialog)
    }

    fun deleteTask(id: Long) {
        deleteTaskConfirmed(taskId = id, strategy = null)
    }

    fun deleteRecurringTask(id: Long, recurringRemovalStrategy: RecurringRemovalStrategy) {
        deleteTaskConfirmed(taskId = id, strategy = recurringRemovalStrategy)
    }

    fun onDragTask(from: ItemPosition, to: ItemPosition) {
        val data = state.value
        val tasks = data.tasks

        if (tasks is TasksState.Success) {
            val toTask = tasks.data.first { it.id == to.key }
            val fromTask = tasks.data.first { it.id == from.key }

            if (from.index < 0 || to.index < 0) return

            setState {
                copy(
                    fromToPositions = Pair(fromTask.position, toTask.position),
                    tasks = TasksState.Success(
                        tasks.data.toMutableList().apply {
                            add(from.index, removeAt(to.index))
                        }.toImmutableList(),
                    ),
                )
            }
        }
    }

    fun onDragStopped() {
        viewModelScope.launch {
            val currentState = state.value

            if (currentState.tasks is TasksState.Success && currentState.fromToPositions != null) {
                moveTaskUseCase(
                    MoveTaskUseCase.Params(
                        day = currentState.selectedDay.date,
                        fromPosition = currentState.fromToPositions.first,
                        toPosition = currentState.fromToPositions.second,
                    ),
                )
            }

            setState {
                copy(fromToPositions = null)
            }
            atomAnalytics.track(OrderTask)
        }
    }

    fun dismissDelete() {
        hideAskDelete()
        atomAnalytics.track(CancelDelete)
    }

    fun toggleHeader() {
        val currentState = state.value

        setState {
            copy(isHeaderExpanded = !isHeaderExpanded)
        }

        if (currentState.isHeaderExpanded) {
            atomAnalytics.track(CollapseCalendar)
        } else {
            atomAnalytics.track(ExpandCalendar)
        }
    }

    fun onEditTask(taskId: Long) {
        viewModelScope.launch {
            atomAnalytics.track(AgendaAnalytics.EditTask)
            sendEvent(
                AgendaUiEvents.GoToEditScreen(
                    taskId = taskId,
                )
            )
        }
    }

    fun onOpenTaskActions() {
        atomAnalytics.track(AgendaAnalytics.OpenTaskActions)
    }

    private fun hideAskDelete() {
        setState { copy(removeTaskConfirmationUiState = RemoveTaskConfirmationUiState.Hidden) }
    }

    private fun deleteTaskConfirmed(
        taskId: Long,
        strategy: RecurringRemovalStrategy?,
    ) {
        hideAskDelete()
        viewModelScope.launch {
            removeTaskUseCase(
                RemoveTaskUseCase.Params(
                    taskId = taskId,
                    strategy = strategy,
                )
            ).fold(
                ifError = {
                    sendGenericError()
                },
                ifResult = {
                    showSnackbar(deleteMessageFor(strategy))
                    atomAnalytics.track(ConfirmDelete)
                }
            )
        }
    }

    private fun deleteMessageFor(strategy: RecurringRemovalStrategy?): Int = when (strategy) {
        RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES -> R.string.task_feedback_deleted_future
        RecurringRemovalStrategy.FUTURE_ONES -> R.string.task_feedback_deleted_future
        RecurringRemovalStrategy.ALL -> R.string.task_feedback_deleted_all
        RecurringRemovalStrategy.SINGLE, null -> R.string.task_feedback_deleted
    }

    private fun sendGenericError() {
        showSnackbar(R.string.error_generic)
    }

    private fun showSnackbar(messageRes: Int) {
        snackbarManager.showMessage(
            AppSnackbarMessage(
                messageRes = messageRes,
            )
        )
    }

    fun onCreateTask() {
        viewModelScope.launch {
            atomAnalytics.track(AgendaAnalytics.CreateNewTask)

            sendEvent(
                AgendaUiEvents.GoToNewTaskScreen(
                    date = state.value.selectedDay.date,
                )
            )
        }
    }

    fun orderTaskTutorialDismissed() {
        viewModelScope.launch {
            taskOrderTutorialDismissedUseCase(Unit)
        }
    }

    fun openCalendarView() {
        viewModelScope.launch {
            setState { copy(shouldShowCalendarView = true) }
        }
    }

    fun dismissCalendarView() {
        viewModelScope.launch {
            setState { copy(shouldShowCalendarView = false) }
        }
    }
}
