package com.costular.atomtasks.agenda.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.tasks.actions.TaskActionsResult
import com.costular.atomtasks.core.ui.mvi.EventObserver
import com.costular.atomtasks.core.ui.tasks.ItemPosition
import com.costular.atomtasks.core.ui.tasks.TaskList
import com.costular.atomtasks.core.ui.utils.DevicesPreview
import com.costular.atomtasks.core.ui.tasks.TaskInteractionEffects
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.components.CircularLoadingIndicator
import com.costular.designsystem.dialogs.DatePickerDialog
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.costular.designsystem.util.supportWideScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.taskactions.destinations.TasksActionsBottomSheetDestination
import com.ramcosta.composedestinations.result.ResultRecipient
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
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
    val interactionState by viewModel.taskInteractionState.collectAsStateWithLifecycle()

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

    TaskInteractionEffects(
        state = interactionState,
        resultRecipient = resultRecipient,
        onEdit = viewModel::onEditTask,
        onAskDelete = viewModel::askDelete,
        onMarkTask = viewModel::onMarkTask,
        onDismissDelete = viewModel::dismissDelete,
        onDelete = viewModel::deleteTask,
        onDeleteRecurring = viewModel::deleteRecurringTask,
        onReviewFinished = viewModel::onReviewFinished,
    )

    AgendaScreen(
        state = state,
        onSelectDate = viewModel::setSelectedDay,
        onSearch = navigator::navigateToSearch,
        onSelectToday = viewModel::setSelectedDayToday,
        onMarkTask = viewModel::onMarkTask,
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
            viewModel.askDelete(it.id)
        },
        onDismissCalendarView = viewModel::dismissCalendarView,
    )
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
    openTaskDetail: (Task) -> Unit,
    openTaskAction: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onDragTask: (ItemPosition, ItemPosition) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
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

    Column(
        modifier = modifier,
    ) {
        AgendaHeader(
            selectedDay = state.selectedDay,
            onSelectDate = onSelectDate,
            // Start using date provider instead of fixed date
            shouldShowTodayAction = state.selectedDay.date != LocalDate.now(),
            onSelectToday = onSelectToday,
            onClickCalendar = onClickOpenCalendarView,
            onSearch = onSearch,
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

    when (val tasks = state.tasks) {
        is TasksState.Success -> {
            TaskList(
                onMove = onDragTask,
                tasks = tasks.data,
                sectionsEnabled = state.taskListSectionsEnabled,
                onClick = onOpenTask,
                onMarkTask = onMarkTask,
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
