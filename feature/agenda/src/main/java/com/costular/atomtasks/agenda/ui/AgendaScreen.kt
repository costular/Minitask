package com.costular.atomtasks.agenda.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import java.time.temporal.ChronoUnit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.agenda.actions.TaskActionsResult
import com.costular.atomtasks.core.ui.mvi.EventObserver
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.ui.utils.DevicesPreview
import com.costular.atomtasks.review.ui.ReviewHandler
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.core.ui.tasks.TaskList
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiHandler
import com.costular.designsystem.components.CircularLoadingIndicator
import com.costular.designsystem.dialogs.DatePickerDialog
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.costular.designsystem.util.supportWideScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.agenda.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.collections.immutable.persistentListOf

const val TestTagHeader = "AgendaTitle"

@Destination<AgendaGraph>(
    start = true,
)
@Composable
fun AgendaScreen(
    navigator: AgendaNavigator,
    setFabOnClick: (() -> Unit) -> Unit,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
) {
    AgendaScreen(
        navigator = navigator,
        setFabOnClick = setFabOnClick,
        resultRecipient = resultRecipient,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun AgendaScreen(
    navigator: AgendaNavigator,
    setFabOnClick: (() -> Unit) -> Unit,
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
    viewModel: AgendaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EventObserver(viewModel.uiEvents) { event ->
        when (event) {
            is AgendaUiEvents.GoToNewTaskScreen -> {
                navigator.navigateToDetailScreenForCreateTask(event.date.toString())
            }

            is AgendaUiEvents.GoToEditScreen -> {
                navigator.navigateToDetailScreenToEdit(event.taskId)
            }

            is AgendaUiEvents.OpenOnboarding -> {
                navigator.navigateToOnboarding()
            }
        }
    }

    LaunchedEffect(Unit) {
        setFabOnClick(viewModel::onCreateTask)
    }

    HandleResultRecipients(
        resultRecipient = resultRecipient,
        onEdit = viewModel::onEditTask,
        onDelete = viewModel::actionDelete,
        onMarkTask = viewModel::onMarkTask,
    )

    ReviewHandler(
        shouldRequestReview = state.shouldShowReviewDialog,
        onFinish = viewModel::onReviewFinished,
    )

    AgendaScreen(
        state = state,
        onSelectDate = viewModel::setSelectedDay,
        onSelectToday = viewModel::setSelectedDayToday,
        onMarkTask = viewModel::onMarkTask,
        deleteTask = viewModel::deleteTask,
        deleteRecurringTask = viewModel::deleteRecurringTask,
        dismissDelete = viewModel::dismissDelete,
        onClickOpenCalendarView = viewModel::openCalendarView,
        openTaskAction = { task ->
            viewModel.onOpenTaskActions()
            navigator.openTaskActions(
                taskId = task.id,
                taskName = task.name,
                isDone = task.isDone,
            )
        },
        onDragTask = viewModel::onDragTask,
        onDragStopped = viewModel::onDragStopped,
        openTaskDetail = {
            viewModel.onEditTask(it.id)
        },
        onDeleteTask = {
            viewModel.actionDelete(it.id)
        },
        onDismissCalendarView = viewModel::dismissCalendarView,
    )
}

@Composable
private fun HandleResultRecipients(
    resultRecipient: ResultRecipient<TasksActionsBottomSheetDestination, TaskActionsResult>,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
) {
    resultRecipient.onNavResult { result ->
        when (result) {
            is NavResult.Canceled -> Unit
            is NavResult.Value -> {
                when (val response = result.value) {
                    is TaskActionsResult.Remove -> {
                        onDelete(response.taskId)
                    }

                    is TaskActionsResult.Edit -> {
                        onEdit(response.taskId)
                    }

                    is TaskActionsResult.MarkAsNotDone -> {
                        onMarkTask(response.taskId, false)
                    }

                    is TaskActionsResult.MarkAsDone -> {
                        onMarkTask(response.taskId, true)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "LongParameterList", "ForbiddenComment")
@Composable
fun AgendaScreen(
    state: AgendaState,
    onSelectDate: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    onClickOpenCalendarView: () -> Unit,
    onDismissCalendarView: () -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    deleteTask: (id: Long) -> Unit,
    deleteRecurringTask: (id: Long, strategy: RecurringRemovalStrategy) -> Unit,
    dismissDelete: () -> Unit,
    openTaskDetail: (Task) -> Unit,
    openTaskAction: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onDragTask: (ItemPosition, ItemPosition) -> Unit,
    onDragStopped: () -> Unit,
) {
    RemoveTaskConfirmationUiHandler(
        uiState = state.removeTaskConfirmationUiState,
        onDismiss = dismissDelete,
        onDeleteRecurring = deleteRecurringTask,
        onDelete = deleteTask,
    )

    if (state.shouldShowCalendarView) {
        DatePickerDialog(
            onDismiss = onDismissCalendarView,
            currentDate = state.selectedDay.date,
            onDateSelected = {
                onSelectDate(it)
                onDismissCalendarView()
            },
        )
    }

    val initialDate = remember { LocalDate.now() }
    val startIndex = Int.MAX_VALUE / 2
    val initialPage = remember(initialDate, state.selectedDay) {
        startIndex + ChronoUnit.DAYS.between(initialDate, state.selectedDay.date).toInt()
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    val currentSelectedDay by rememberUpdatedState(state.selectedDay)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val newDate = initialDate.plusDays(page - startIndex.toLong())
            if (newDate != currentSelectedDay.date) {
                onSelectDate(newDate)
            }
        }
    }

    LaunchedEffect(state.selectedDay) {
        val targetPage = startIndex + ChronoUnit.DAYS.between(initialDate, state.selectedDay.date).toInt()
        if (targetPage != pagerState.currentPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AgendaHeader(
            selectedDay = state.selectedDay,
            onSelectDate = onSelectDate,
            // Start using date provider instead of fixed date
            shouldShowTodayAction = state.selectedDay.date != LocalDate.now(),
            onSelectToday = onSelectToday,
            onClickCalendar = onClickOpenCalendarView,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val pageDate = initialDate.plusDays(page - startIndex.toLong())

            if (pageDate == state.selectedDay.date) {
                // This prevents adjacent days to show duplicated tasks while loading
                // Even though it's very unlikely to happen due to the local database, it's good
                // having this protection
                TasksContent(
                    state = state,
                    onOpenTask = openTaskDetail,
                    onMarkTask = onMarkTask,
                    modifier = Modifier.supportWideScreen(),
                    onDragStopped = onDragStopped,
                    onDragTask = onDragTask,
                    onDeleteTask = onDeleteTask,
                    onClickTaskMore = openTaskAction,
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun TasksContent(
    state: AgendaState,
    onOpenTask: (Task) -> Unit,
    onClickTaskMore: (Task) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    onDragTask: (ItemPosition, ItemPosition) -> Unit,
    onDragStopped: () -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    when (val tasks = state.tasks) {
        is TasksState.Success -> {
            TaskList(
                onMove = onDragTask,
                tasks = tasks.data,
                onClick = onOpenTask,
                onMarkTask = { taskId, isDone ->
                    onMarkTask(taskId, isDone)

                    if (isDone) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                padding = PaddingValues(
                    start = AppTheme.dimens.contentMargin,
                    end = AppTheme.dimens.contentMargin,
                    top = AppTheme.dimens.spacingLarge,
                    bottom = ContentPaddingForFAB.dp,
                ),
                modifier = modifier
                    .fillMaxSize()
                    .testTag("AgendaTaskList"),
                onClickMore = onClickTaskMore,
                onDeleteTask = onDeleteTask,
                onDragStopped = onDragStopped,
            )
        }

        TasksState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularLoadingIndicator()
            }
        }

        is TasksState.Failure -> {}
        TasksState.Uninitialized -> {}
    }
}

@Suppress("MagicNumber")
@DevicesPreview
@Composable
fun AgendaPreview() {
    AtomTheme {
        AgendaScreen(
            state = AgendaState(
                tasks = TasksState.Success(
                    persistentListOf(
                        Task(
                            id = 1L,
                            name = "🏋🏼 Go to the gym",
                            createdAt = LocalDate.now(),
                            day = LocalDate.now(),
                            reminder = Reminder(
                                id = 1L,
                                time = LocalTime.of(9, 0),
                                date = LocalDate.now(),
                            ),
                            isDone = false,
                            position = 0,
                            isRecurring = false,
                            recurrenceEndDate = null,
                            recurrenceType = null,
                            parentId = null,
                        ),
                        Task(
                            id = 2L,
                            name = "🎹 Play the piano!",
                            createdAt = LocalDate.now(),
                            day = LocalDate.now(),
                            reminder = Reminder(
                                id = 1L,
                                time = LocalTime.of(9, 0),
                                date = LocalDate.now(),
                            ),
                            isDone = true,
                            position = 0,
                            isRecurring = false,
                            recurrenceEndDate = null,
                            recurrenceType = null,
                            parentId = null,
                        ),
                    ),
                ),
            ),
            onSelectDate = {},
            onSelectToday = {},
            onMarkTask = { _, _ -> },
            deleteTask = {},
            deleteRecurringTask = { _, _ -> },
            dismissDelete = {},
            openTaskAction = {},
            onDragTask = { _, _ -> },
            onDragStopped = {},
            openTaskDetail = {},
            onDismissCalendarView = {},
            onClickOpenCalendarView = {},
            onDeleteTask = {},
        )
    }
}

private const val ContentPaddingForFAB = 90
