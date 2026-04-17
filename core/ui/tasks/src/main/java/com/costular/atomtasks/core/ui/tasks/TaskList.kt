package com.costular.atomtasks.core.ui.tasks

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.utils.VariantsPreview
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TaskList(
    tasks: List<Task>,
    onClick: (Task) -> Unit,
    onClickMore: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onMarkTask: (taskId: Long, isDone: Boolean) -> Unit,
    onMove: (from: ItemPosition, to: ItemPosition) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    reorderableLazyListState: ReorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            onMove(
                ItemPosition(from.index, from.key as Long),
                ItemPosition(to.index, to.key as Long),
            )
        }
    ),
    padding: PaddingValues = PaddingValues(0.dp),
) {
    if (tasks.isEmpty()) {
        Empty(modifier.padding(AppTheme.dimens.contentMargin))
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(
                    state = reorderableLazyListState,
                    task = task,
                    onDeleteTask = onDeleteTask,
                    onMarkTask = onMarkTask,
                    onClick = onClick,
                    onClickMore = onClickMore,
                    onDragStopped = onDragStopped,
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.TaskItem(
    state: ReorderableLazyListState,
    task: Task,
    onDeleteTask: (Task) -> Unit,
    onMarkTask: (taskId: Long, isDone: Boolean) -> Unit,
    onClick: (Task) -> Unit,
    onClickMore: (Task) -> Unit,
    onDragStopped: () -> Unit,
) {
    val view = LocalView.current
    val dismissState = rememberSwipeToDismissBoxState()
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    ReorderableItem(state, key = task.id) { isDragging ->
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromEndToStart = true,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                TaskRemoveBackground(dismissState)
            },
            onDismiss = { direction ->
                if (direction == SwipeToDismissBoxValue.EndToStart) {
                    onDeleteTask(task)
                    coroutineScope.launch {
                        dismissState.reset()
                    }
                }
            },
        ) {
            TaskCard(
                title = task.name,
                onMark = { onMarkTask(task.id, it) },
                onClick = { onClick(task) },
                reminder = task.reminder,
                isFinished = task.isDone,
                recurrenceType = task.recurrenceType,
                interactionSource = interactionSource,
                modifier = Modifier
                    .longPressDraggableHandle(
                        interactionSource = interactionSource,
                        onDragStarted = {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        },
                        onDragStopped = {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.GESTURE_END)
                            onDragStopped()
                        },
                    ),
                onClickMore = { onClickMore(task) },
            )
        }
    }
}

@Composable
private fun TaskRemoveBackground(
    state: SwipeToDismissBoxState,
) {
    val view = LocalView.current
    val scale by
    animateFloatAsState(
        targetValue = if (state.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing,
        )
    )

    val swipeTargetValue by remember {
        derivedStateOf { state.targetValue }
    }

    LaunchedEffect(swipeTargetValue) {
        if (state.targetValue == SwipeToDismissBoxValue.EndToStart) {
            view.performHapticFeedback(HapticFeedbackConstantsCompat.GESTURE_START)
        }
    }

    val (backgroundColor, contentColor) = when (state.dismissDirection) {
        SwipeToDismissBoxValue.Settled -> Color.Transparent to Color.Transparent
        SwipeToDismissBoxValue.StartToEnd -> Color.Transparent to Color.Transparent
        SwipeToDismissBoxValue.EndToStart -> {
            MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor, CardDefaults.elevatedShape)
            .padding(AppTheme.dimens.contentMargin),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.scale(scale),
        )
    }
}

@Composable
fun Empty(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.task_list_empty_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AppTheme.dimens.spacingMedium))

        Text(
            text = stringResource(R.string.task_list_empty_description),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@VariantsPreview
@Preview
@Composable
fun TaskListEmpty() {
    AtomTheme {
        Empty(Modifier.fillMaxWidth())
    }
}

@Suppress("MagicNumber")
@VariantsPreview
@Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
    AtomTheme {
        TaskList(
            modifier = Modifier.fillMaxWidth(),
            tasks = listOf(
                Task(
                    id = 1L,
                    name = "Task1",
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
                    name = "Task2",
                    createdAt = LocalDate.now(),
                    day = LocalDate.now(),
                    reminder = null,
                    isDone = false,
                    position = 0,
                    isRecurring = false,
                    recurrenceEndDate = null,
                    recurrenceType = null,
                    parentId = null,
                ),
                Task(
                    id = 3L,
                    name = "Task3",
                    createdAt = LocalDate.now(),
                    day = LocalDate.now(),
                    reminder = null,
                    isDone = true,
                    position = 0,
                    isRecurring = false,
                    recurrenceEndDate = null,
                    recurrenceType = null,
                    parentId = null,
                ),
            ),
            onClick = {},
            onMarkTask = { _, _ -> },
            onClickMore = {},
            onDeleteTask = {},
            onMove = { _, _ -> },
            onDragStopped = {},
        )
    }
}
