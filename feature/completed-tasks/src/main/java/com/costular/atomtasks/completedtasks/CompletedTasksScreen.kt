package com.costular.atomtasks.completedtasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.tasks.TaskDayHeader
import com.costular.atomtasks.core.ui.tasks.TaskInteractionEffects
import com.costular.atomtasks.core.ui.tasks.TaskRow
import com.costular.atomtasks.core.ui.tasks.actions.TaskActionsResult
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.components.AtomTopBar
import com.costular.designsystem.components.CircularLoadingIndicator
import com.costular.designsystem.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.taskactions.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.ResultRecipient

@Destination<CompletedTasksGraph>(start = true)
@Composable
fun CompletedTasksScreen(
    navigator: CompletedTasksNavigator,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
) {
    val viewModel: CompletedTasksViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val interactionState by viewModel.taskInteractionState.collectAsStateWithLifecycle()
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
    CompletedTasksScreen(
        state = state,
        onBack = navigator::navigateUp,
        onRetry = viewModel::retry,
        onOpenTask = { navigator.navigateToDetailScreenToEdit(it.id) },
        onMore = { navigator.openTaskActions(it.id, it.name, it.isDone) },
        onDelete = { viewModel.askDelete(it.id) },
        onMark = viewModel::onMarkTask,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList", "LongMethod")
@Composable
fun CompletedTasksScreen(
    state: CompletedTasksState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onMore: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMark: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(modifier = modifier) {
        AtomTopBar(
            title = { Text(stringResource(R.string.completed_tasks)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.navigate_back))
                }
            },
        )
        when (state) {
            CompletedTasksState.Loading -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularLoadingIndicator()
            }
            CompletedTasksState.Failure -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.tasks_load_error))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.tasks_retry)) }
            }
            is CompletedTasksState.Success -> if (state.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.completed_tasks_empty))
                }
            } else {
                CompletedTasksList(
                    tasks = state.tasks,
                    listState = listState,
                    onOpenTask = onOpenTask,
                    onMore = onMore,
                    onDelete = onDelete,
                    onMark = onMark,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedTasksList(
    tasks: List<Task>,
    listState: LazyListState,
    onOpenTask: (Task) -> Unit,
    onMore: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMark: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember(tasks) { tasks.groupBy { it.day } }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = AppTheme.dimens.contentMargin),
    ) {
        days.forEach { (day, dailyTasks) ->
            stickyHeader(key = "day:$day", contentType = "day_header") {
                TaskDayHeader(day = day, modifier = Modifier.fillMaxWidth())
            }
            items(dailyTasks, key = { it.id }, contentType = { "task" }) { task ->
                TaskRow(
                    task = task,
                    onClick = onOpenTask,
                    onClickMore = onMore,
                    onDeleteTask = onDelete,
                    onMarkTask = onMark,
                    modifier = Modifier
                        .animateItem()
                        .padding(
                            horizontal = AppTheme.dimens.contentMargin,
                            vertical = AppTheme.dimens.spacingSmall,
                        ),
                )
            }
        }
    }
}

