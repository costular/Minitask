package com.costular.atomtasks.verticaltasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.tasks.ReorderableTaskRow
import com.costular.atomtasks.core.ui.tasks.TaskDayHeader
import com.costular.atomtasks.core.ui.tasks.TaskInteractionEffects
import com.costular.atomtasks.core.ui.tasks.actions.TaskActionsResult
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.components.CircularLoadingIndicator
import com.costular.designsystem.dialogs.DatePickerDialog
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.util.supportWideScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.taskactions.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.ResultRecipient
import java.time.LocalDate
import kotlinx.coroutines.flow.distinctUntilChanged
import sh.calvin.reorderable.rememberReorderableLazyListState

@Destination<VerticalTasksGraph>(start = true)
@Composable
fun VerticalTasksScreen(
    navigator: VerticalTasksNavigator,
    setFabOnClick: (() -> Unit) -> Unit,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
) {
    val viewModel: VerticalTasksViewModel = hiltViewModel()
    val interactionState by viewModel.taskInteractionState.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TaskInteractionEffects(
        state = interactionState,
        resultRecipient = resultRecipient,
        onEdit = navigator::navigateToDetailScreenToEdit,
        onAskDelete = viewModel::askDelete,
        onMarkTask = viewModel::onMarkTask,
        onDismissDelete = viewModel::dismissDelete,
        onDelete = viewModel::deleteTask,
        onDeleteRecurring = viewModel::deleteRecurringTask,
        onReviewFinished = viewModel::onReviewFinished,
    )
    LaunchedEffect(viewModel, navigator, setFabOnClick) {
        setFabOnClick {
            navigator.navigateToDetailScreenForCreateTask(viewModel.state.value.selectedDay.toString())
        }
    }
    VerticalTasksScreen(
        state = state,
        onToday = viewModel::selectToday,
        onSearch = navigator::navigateToSearch,
        onCalendar = viewModel::openCalendar,
        onDismissCalendar = viewModel::dismissCalendar,
        onSelectDay = viewModel::selectDay,
        onScrollHandled = viewModel::onScrollHandled,
        onAnchor = viewModel::onAnchor,
        onVisibleRange = viewModel::onVisibleRange,
        onRetry = viewModel::retry,
        onOpenTask = { navigator.navigateToDetailScreenToEdit(it.id) },
        onMore = { navigator.openTaskActions(it.id, it.name, it.isDone) },
        onDelete = { viewModel.askDelete(it.id) },
        onMark = { id, done -> viewModel.onMarkTask(id, done) },
        onMove = viewModel::onMove,
        onDragStarted = viewModel::onDragStarted,
        onDragStopped = viewModel::onDragStopped,
        today = viewModel.today,
    )
}

private data class VisibleAnchor(val key: Any, val offset: Int, val lastKey: Any, val pendingScrollId: Long?)
private data class ScrollPosition(val index: Int, val offset: Int)

private fun resolveScrollPosition(rows: List<VerticalTaskRow>, target: ScrollTarget): ScrollPosition? {
    val taskIndex = rows.indexOfFirst { target.taskId != null && it.key == target.taskId }
    val index = if (taskIndex >= 0) taskIndex else rows.indexOfFirst {
        it is VerticalTaskRow.Header && it.day == target.day
    }
    if (index < 0) return null
    val offset = if (target.taskId != null && taskIndex < 0) 0 else target.offset
    return ScrollPosition(index, offset)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
fun VerticalTasksScreen(
    state: VerticalTasksState,
    onToday: () -> Unit,
    onSearch: () -> Unit,
    onCalendar: () -> Unit,
    onDismissCalendar: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onScrollHandled: (Long) -> Unit,
    onAnchor: (LocalDate, Long?, Int) -> Unit,
    onVisibleRange: (LocalDate, LocalDate) -> Unit,
    onRetry: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onMore: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMark: (Long, Boolean) -> Unit,
    onMove: (Long, Long) -> Unit,
    onDragStarted: (Long) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val listState = rememberLazyListState()
    VerticalTasksScrollEffects(state, listState, onAnchor, onVisibleRange, onScrollHandled)

    if (state.shouldShowCalendar) {
        DatePickerDialog(onDismissCalendar, state.selectedDay, onSelectDay)
    }
    Column(modifier = modifier) {
        VerticalTasksHeader(onCalendar, onSearch, Modifier.fillMaxWidth())
        if (state.hasError) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.tasks_load_error), modifier = Modifier.weight(1f))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.tasks_retry)) }
            }
        }
        if (state.rows.isEmpty() && state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularLoadingIndicator()
            }
        } else {
            Box(modifier = Modifier.weight(1f).supportWideScreen()) {
                VerticalTasksList(
                    state = state,
                    listState = listState,
                    onOpenTask = onOpenTask,
                    onMore = onMore,
                    onDelete = onDelete,
                    onMark = onMark,
                    onMove = onMove,
                    onDragStarted = onDragStarted,
                    onDragStopped = onDragStopped,
                    modifier = Modifier.fillMaxSize(),
                )
                TodayBubble(
                    rows = state.rows,
                    listState = listState,
                    today = today,
                    enabled = state.scrollTarget == null && !state.isDragging,
                    onToday = onToday,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = AppTheme.dimens.spacingSmall, end = AppTheme.dimens.contentMargin),
                )
            }
        }
    }
}

@Composable
private fun VerticalTasksHeader(
    onCalendar: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, stringResource(R.string.search_tasks)) }
                IconButton(onClick = onCalendar) {
                    Icon(Icons.Outlined.CalendarMonth, stringResource(R.string.home_menu_calendar))
                }
            }
        }
    }
}

@Composable
private fun TodayBubble(
    rows: List<VerticalTaskRow>,
    listState: LazyListState,
    today: LocalDate,
    enabled: Boolean,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayLabel = stringResource(R.string.today)
    val isPastTodayHeader by remember(rows, listState, today) {
        derivedStateOf {
            val first = rows.getOrNull(listState.firstVisibleItemIndex)
            first != null && (first.day > today || first.day == today && first !is VerticalTaskRow.Header)
        }
    }
    AnimatedVisibility(
        visible = enabled && isPastTodayHeader,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
    ) {
        Surface(
            onClick = onToday,
            modifier = Modifier.semantics { contentDescription = todayLabel },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(todayLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private const val FabPadding = 90

@Composable
private fun VerticalTasksScrollEffects(
    state: VerticalTasksState,
    listState: LazyListState,
    onAnchor: (LocalDate, Long?, Int) -> Unit,
    onVisibleRange: (LocalDate, LocalDate) -> Unit,
    onScrollHandled: (Long) -> Unit,
) {
    val latestRows by rememberUpdatedState(state.rows)
    val latestAnchor by rememberUpdatedState(onAnchor)
    val latestVisibleRange by rememberUpdatedState(onVisibleRange)
    val latestScrollId by rememberUpdatedState(state.scrollTarget?.requestId)

    LaunchedEffect(listState) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            val first = visible.firstOrNull { it.index == listState.firstVisibleItemIndex }
            val last = visible.maxByOrNull { it.index }
            if (first != null && last != null) {
                VisibleAnchor(first.key, listState.firstVisibleItemScrollOffset, last.key, latestScrollId)
            }
            else null
        }.distinctUntilChanged().collect { anchor ->
            if (anchor != null && anchor.pendingScrollId == null) {
                val first = latestRows.find { it.key == anchor.key }
                val last = latestRows.find { it.key == anchor.lastKey }
                if (first != null && last != null) {
                    latestAnchor(first.day, (first as? VerticalTaskRow.Item)?.task?.id, anchor.offset)
                    latestVisibleRange(first.day, last.day)
                }
            }
        }
    }
    LaunchedEffect(state.scrollTarget, state.isLoading) {
        val target = state.scrollTarget
        if (target != null && !state.isLoading && !state.hasError) {
            val position = resolveScrollPosition(state.rows, target)
            if (position != null) {
                listState.scrollToItem(position.index, position.offset)
                onScrollHandled(target.requestId)
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList")
@Composable
private fun VerticalTasksList(
    state: VerticalTasksState,
    listState: LazyListState,
    onOpenTask: (Task) -> Unit,
    onMore: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMark: (Long, Boolean) -> Unit,
    onMove: (Long, Long) -> Unit,
    onDragStarted: (Long) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestMove by rememberUpdatedState(onMove)
    val latestRows by rememberUpdatedState(state.rows)
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromId = from.key as? Long
        val toId = to.key as? Long
        if (fromId != null && toId != null) {
            val source = latestRows.find { it.key == fromId }
            val target = latestRows.find { it.key == toId }
            if (source != null && source.day == target?.day) latestMove(fromId, toId)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = FabPadding.dp),
    ) {
        state.rows.forEach { row ->
            when (row) {
                is VerticalTaskRow.Header -> stickyHeader(key = row.key) {
                    TaskDayHeader(day = row.day, modifier = Modifier.fillMaxWidth())
                }
                is VerticalTaskRow.Empty -> item(key = row.key) {
                    Text(stringResource(R.string.vertical_empty_day),
                        modifier = Modifier.padding(
                            horizontal = AppTheme.dimens.contentMargin,
                            vertical = AppTheme.dimens.spacingSmall,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is VerticalTaskRow.Item -> item(key = row.key) {
                    ReorderableTaskRow(
                        state = reorderState,
                        task = row.task,
                        onClick = onOpenTask,
                        onClickMore = onMore,
                        onDeleteTask = onDelete,
                        onMarkTask = onMark,
                        onDragStopped = onDragStopped,
                        onDragStarted = { onDragStarted(row.task.id) },
                        modifier = Modifier.padding(
                            horizontal = AppTheme.dimens.contentMargin,
                            vertical = AppTheme.dimens.spacingSmall,
                        ),
                    )
                }
            }
        }
    }
}
