package com.costular.atomtasks.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.tasks.TaskInteractionEffects
import com.costular.atomtasks.core.ui.tasks.TaskDayHeader
import com.costular.atomtasks.core.ui.tasks.TaskRow
import com.costular.atomtasks.core.ui.tasks.actions.TaskActionsResult
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.components.CircularLoadingIndicator
import com.costular.designsystem.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.taskactions.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.ResultRecipient

@Destination<SearchGraph>(start = true)
@Composable
fun SearchScreen(
    navigator: SearchNavigator,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
) {
    val viewModel: SearchViewModel = hiltViewModel()
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
    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onBack = navigator::navigateUp,
        onRetry = viewModel::retry,
        onOpenTask = { navigator.navigateToDetailScreenToEdit(it.id) },
        onMore = { navigator.openTaskActions(it.id, it.name, it.isDone) },
        onMark = { id, done -> viewModel.onMarkTask(id, done) },
        onDelete = { viewModel.askDelete(it.id) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
fun SearchScreen(
    state: SearchState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onMore: (Task) -> Unit,
    onMark: (Long, Boolean) -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(focusRequester) {
        if (!hasRequestedFocus) {
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }
    Column(modifier = modifier.imePadding()) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.contentMargin),
            expanded = false,
            onExpandedChange = {},
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier.focusRequester(focusRequester),
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSearch = { keyboard?.hide() },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(R.string.search_tasks)) },
                    leadingIcon = {
                        IconButton(onClick = { keyboard?.hide(); onBack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.navigate_back))
                        }
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = {
                                onQueryChange("")
                                focusRequester.requestFocus()
                                keyboard?.show()
                            }) {
                                Icon(Icons.Outlined.Close, stringResource(R.string.search_clear))
                            }
                        }
                    },
                )
            },
        ) {}
        when (val results = state.results) {
            is SearchResults.Success -> if (results.tasks.isEmpty()) {
                SearchMessage(R.string.search_no_results, Modifier.weight(1f))
            } else {
                SearchResultsList(
                    tasks = results.tasks,
                    onOpenTask = { keyboard?.hide(); onOpenTask(it) },
                    onMore = { keyboard?.hide(); onMore(it) },
                    onDelete = onDelete,
                    onMark = onMark,
                    modifier = Modifier.weight(1f),
                    listState = listState,
                )
            }
            SearchResults.Idle -> SearchMessage(R.string.search_minimum_characters, Modifier.weight(1f))
            SearchResults.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularLoadingIndicator()
            }
            SearchResults.Failure -> Column(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.tasks_load_error))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.tasks_retry)) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsList(
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

@Composable
private fun SearchMessage(message: Int, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(stringResource(message), modifier = Modifier.padding(AppTheme.dimens.contentMargin))
    }
}
