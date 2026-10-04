package com.costular.atomtasks.verticaltasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.ui.AppSnackbarMessage
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.usecase.MoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.ObserveTasksInRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private fun initialState(savedState: SavedStateHandle, clock: Clock): VerticalTasksState {
    val day = savedState.get<String>("anchor_day")?.let(LocalDate::parse) ?: LocalDate.now(clock)
    return VerticalTasksState(
        selectedDay = day,
        window = DateWindow.around(day),
        scrollTarget = ScrollTarget(day, savedState["anchor_task"], savedState["anchor_offset"] ?: 0),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList", "TooManyFunctions")
@HiltViewModel
class VerticalTasksViewModel @Inject constructor(
    private val observeTasks: ObserveTasksInRangeUseCase,
    private val moveTask: MoveTaskUseCase,
    private val savedState: SavedStateHandle,
    private val clock: Clock,
    private val snackbar: SnackbarManager,
    taskInteractionFactory: TaskInteractionStateHolder.Factory,
) : MviViewModel<VerticalTasksState>(initialState(savedState, clock)) {
    private val taskInteractions = taskInteractionFactory.create(viewModelScope)
    val taskInteractionState = taskInteractions.state
    val today: LocalDate get() = LocalDate.now(clock)

    fun onMarkTask(taskId: Long, isDone: Boolean) = taskInteractions.onMarkTask(taskId, isDone)
    fun askDelete(taskId: Long) = taskInteractions.askDelete(taskId)
    fun dismissDelete() = taskInteractions.dismissDelete()
    fun deleteTask(taskId: Long) = taskInteractions.deleteTask(taskId)
    fun deleteRecurringTask(taskId: Long, strategy: RecurringRemovalStrategy) =
        taskInteractions.deleteRecurringTask(taskId, strategy)
    fun onReviewFinished() = taskInteractions.onReviewFinished()

    private data class RangeRequest(val window: DateWindow, val version: Long = 0)
    private data class DragSession(val taskId: Long, val day: LocalDate, val positions: List<Int>, val from: Int)

    private val ranges = MutableStateFlow(RangeRequest(state.value.window))
    private var databaseTasks: List<Task> = emptyList()
    private var drag: DragSession? = null
    private var persistingDrag = false
    private var scrollRequestId = 0L

    init {
        viewModelScope.launch {
            ranges.flatMapLatest { request ->
                observeTasks(ObserveTasksInRangeUseCase.Params(request.window.start, request.window.end))
                    .map { request to it }
            }.collect { (request, result) ->
                if (request == ranges.value) {
                    result.fold(
                        ifError = { setState { copy(isLoading = false, hasError = true) } },
                        ifResult = { tasks ->
                            databaseTasks = tasks
                            if (drag == null) {
                                val rows = buildRows(request.window, tasks)
                                if (request == ranges.value && drag == null) {
                                    setState {
                                        copy(rows = rows, window = request.window, isLoading = false, hasError = false)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    fun onAnchor(day: LocalDate, taskId: Long?, offset: Int) {
        if (state.value.scrollTarget != null) return
        saveAnchor(day, taskId, offset)
        if (state.value.selectedDay != day) {
            setState { copy(selectedDay = day) }
        }
    }

    private fun saveAnchor(day: LocalDate, taskId: Long?, offset: Int) {
        savedState["anchor_day"] = day.toString()
        savedState["anchor_task"] = taskId
        savedState["anchor_offset"] = offset
    }

    fun onVisibleRange(firstDay: LocalDate, lastDay: LocalDate) {
        val current = state.value
        if (current.isDragging || current.scrollTarget != null) return
        if (current.isLoading || current.hasError) return
        val window = ranges.value.window
        val next = when {
            firstDay <= window.start.plusDays(DateWindow.PrefetchDays) -> window.extendPast()
            lastDay >= window.end.minusDays(DateWindow.PrefetchDays) -> window.extendFuture()
            else -> window
        }
        if (next != window) requestWindow(next)
    }

    fun selectDay(day: LocalDate) {
        saveAnchor(day, null, 0)
        val target = ScrollTarget(day, requestId = ++scrollRequestId)
        setState { copy(selectedDay = day, shouldShowCalendar = false, scrollTarget = target) }
        if (!ranges.value.window.contains(day)) requestWindow(DateWindow.around(day))
    }

    fun selectToday() = selectDay(LocalDate.now(clock))
    fun openCalendar() = setState { copy(shouldShowCalendar = true) }
    fun dismissCalendar() = setState { copy(shouldShowCalendar = false) }

    fun onScrollHandled(requestId: Long) {
        if (state.value.scrollTarget?.requestId == requestId) setState { copy(scrollTarget = null) }
    }

    fun retry() {
        setState { copy(isLoading = true, hasError = false) }
        ranges.value = ranges.value.copy(version = ranges.value.version + 1)
    }

    private fun requestWindow(window: DateWindow) {
        setState { copy(isLoading = true, hasError = false) }
        ranges.value = RangeRequest(window, ranges.value.version + 1)
    }

    fun onDragStarted(taskId: Long) {
        if (drag != null) return
        val items = state.value.rows.filterIsInstance<VerticalTaskRow.Item>().map { it.task }
        val task = items.find { it.id == taskId } ?: return
        val group = items.filter { it.day == task.day }
        drag = DragSession(task.id, task.day, group.map { it.position }, task.position)
        setState { copy(isDragging = true) }
    }

    fun onMove(fromId: Long, toId: Long) {
        val rows = state.value.rows
        val from = rows.indexOfFirst { it.key == fromId }
        val to = rows.indexOfFirst { it.key == toId }
        if (from < 0 || to < 0 || persistingDrag) return
        if (rows[from].day != rows[to].day) return
        onDragStarted(fromId)
        if (drag?.taskId == fromId) {
            val reordered = rows.toMutableList().apply { add(to, removeAt(from)) }.toImmutableList()
            setState { copy(rows = reordered) }
        }
    }

    fun onDragStopped() {
        if (persistingDrag) return
        val session = drag ?: return
        val group = state.value.rows.filterIsInstance<VerticalTaskRow.Item>().filter { it.day == session.day }
        val finalIndex = group.indexOfFirst { it.task.id == session.taskId }
        val targetPosition = session.positions.getOrNull(finalIndex) ?: session.from
        persistingDrag = true
        viewModelScope.launch {
            var succeeded = false
            try {
                if (targetPosition != session.from) {
                    moveTask(MoveTaskUseCase.Params(session.day, session.from, targetPosition))
                }
                succeeded = true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                snackbar.showMessage(AppSnackbarMessage(messageRes = R.string.error_generic))
            } finally {
                drag = null
                persistingDrag = false
                setState {
                    copy(isDragging = false, rows = if (succeeded) rows else buildRows(window, databaseTasks))
                }
                retry()
            }
        }
    }

    private fun buildRows(window: DateWindow, tasks: List<Task>) = buildList {
        val grouped = tasks.groupBy { it.day }
        var day = window.start
        while (day <= window.end) {
            add(VerticalTaskRow.Header(day))
            val dailyTasks = grouped[day].orEmpty()
            if (dailyTasks.isEmpty()) add(VerticalTaskRow.Empty(day))
            else dailyTasks.forEach { add(VerticalTaskRow.Item(it)) }
            day = day.plusDays(1)
        }
    }.toImmutableList()
}
