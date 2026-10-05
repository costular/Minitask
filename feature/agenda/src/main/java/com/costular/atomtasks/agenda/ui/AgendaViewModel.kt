package com.costular.atomtasks.agenda.ui

import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.CollapseCalendar
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.ExpandCalendar
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.NavigateToDay
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.OrderTask
import com.costular.atomtasks.agenda.analytics.AgendaAnalytics.SelectToday
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.usecase.EmptyParams
import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.data.tutorial.ShouldShowOnboardingUseCase
import com.costular.atomtasks.data.tutorial.ShouldShowTaskOrderTutorialUseCase
import com.costular.atomtasks.data.tutorial.TaskOrderTutorialDismissedUseCase
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
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
    private val moveTaskUseCase: MoveTaskUseCase,
    private val atomAnalytics: AtomAnalytics,
    private val shouldShowTaskOrderTutorialUseCase: ShouldShowTaskOrderTutorialUseCase,
    private val taskOrderTutorialDismissedUseCase: TaskOrderTutorialDismissedUseCase,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
    private val settingsRepository: SettingsRepository,
    taskInteractionFactory: TaskInteractionStateHolder.Factory,
) : MviViewModel<AgendaState>(AgendaState()) {
    private val taskInteractions = taskInteractionFactory.create(viewModelScope)
    val taskInteractionState = taskInteractions.state

    fun onMarkTask(taskId: Long, isDone: Boolean) = taskInteractions.onMarkTask(taskId, isDone)
    fun askDelete(taskId: Long) = taskInteractions.askDelete(taskId)
    fun dismissDelete() = taskInteractions.dismissDelete()
    fun deleteTask(taskId: Long) = taskInteractions.deleteTask(taskId)
    fun deleteRecurringTask(taskId: Long, strategy: RecurringRemovalStrategy) =
        taskInteractions.deleteRecurringTask(taskId, strategy)
    fun onReviewFinished() = taskInteractions.onReviewFinished()

    init {
        shouldShowOnboarding()
        loadTasks()
        retrieveTutorials()
        observeTaskListSections()
    }

    private fun observeTaskListSections() {
        viewModelScope.launch {
            settingsRepository.observeTaskListSectionsEnabled().collect { enabled ->
                setState { copy(taskListSectionsEnabled = enabled) }
            }
        }
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

    fun setSelectedDayToday() {
        setSelectedDay(LocalDate.now())
        atomAnalytics.track(SelectToday)
    }

    fun setSelectedDay(localDate: LocalDate) = viewModelScope.launch {
        setState { copy(selectedDay = localDate.asDay()) }
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

    fun onDragTask(from: ItemPosition, to: ItemPosition) {
        val data = state.value
        val tasks = data.tasks

        if (tasks is TasksState.Success) {
            val fromIndex = tasks.data.indexOfFirst { it.id == from.key }
            val toIndex = tasks.data.indexOfFirst { it.id == to.key }
            if (fromIndex < 0 || toIndex < 0) return
            val fromTask = tasks.data[fromIndex]
            // Positions are persistent slots, while list indices change throughout a drag.
            val targetPosition = tasks.data.map { it.position }.sorted()[toIndex]

            setState {
                copy(
                    fromToPositions = Pair(fromTask.position, targetPosition),
                    tasks = TasksState.Success(
                        tasks.data.toMutableList().apply {
                            add(toIndex, removeAt(fromIndex))
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
}
