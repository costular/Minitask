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
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
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
                        ifResult = {
                            setState { copy(tasks = TasksState.Success(it.toImmutableList())) }
                        })
                }
        }
    }

    fun onMarkTask(taskId: Long, isDone: Boolean) = viewModelScope.launch {
        updateTaskIsDoneUseCase(UpdateTaskIsDoneUseCase.Params(taskId, isDone))
        checkIfReviewShouldBeShown(isDone)

        val event = if (isDone) {
            MarkTaskAsDone
        } else {
            MarkTaskAsNotDone
        }
        atomAnalytics.track(event)
    }

    private suspend fun checkIfReviewShouldBeShown(isDone: Boolean) {
        if (isDone) {
            val result = shouldShowAskReviewUseCase(Unit)
            result.fold(
                ifError = {
                    // do nothing for now
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
            copy(removeTaskConfirmationUiState = RemoveTaskConfirmationUiState.Shown(id, task?.isRecurring ?: false))
        }

        atomAnalytics.track(ShowConfirmDeleteDialog)
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            hideAskDelete()
            removeTaskUseCase(RemoveTaskUseCase.Params(id))
        }

        atomAnalytics.track(ConfirmDelete)
    }

    fun deleteRecurringTask(id: Long, recurringRemovalStrategy: RecurringRemovalStrategy) {
        viewModelScope.launch {
            removeTaskUseCase(RemoveTaskUseCase.Params(id, recurringRemovalStrategy))
            hideAskDelete()
        }
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
            val state = state.value

            if (state.tasks is TasksState.Success && state.fromToPositions != null) {
                moveTaskUseCase(
                    MoveTaskUseCase.Params(
                        day = state.selectedDay.date,
                        fromPosition = state.fromToPositions.first,
                        toPosition = state.fromToPositions.second
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
        val state = state.value

        setState {
            copy(isHeaderExpanded = !isHeaderExpanded)
        }

        if (state.isHeaderExpanded) {
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