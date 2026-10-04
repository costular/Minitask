package com.costular.atomtasks.core.ui.tasks

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

@Composable
fun TaskRow(
    task: Task,
    onClick: (Task) -> Unit,
    onClickMore: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onCompletionAnimationFinished: (Boolean) -> Unit = {},
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current
    val latestTask by rememberUpdatedState(task)
    val latestDelete by rememberUpdatedState(onDeleteTask)
    LaunchedEffect(dismissState, dismissState.settledValue) {
        if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            latestDelete(latestTask)
            dismissState.reset()
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = { TaskRemoveBackground(dismissState) },
    ) {
        TaskCard(
            title = task.name,
            isFinished = task.isDone,
            recurrenceType = task.recurrenceType,
            reminder = task.reminder,
            onClick = { onClick(task) },
            onClickMore = { onClickMore(task) },
            onMark = {
                onMarkTask(task.id, it)
                if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            interactionSource = interactionSource,
            onCompletionAnimationFinished = onCompletionAnimationFinished,
        )
    }
}

@Composable
fun LazyItemScope.ReorderableTaskRow(
    state: ReorderableLazyListState,
    task: Task,
    onClick: (Task) -> Unit,
    onClickMore: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    onDragStarted: () -> Unit = {},
    onCompletionAnimationFinished: (Boolean) -> Unit = {},
) {
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    ReorderableItem(state, key = task.id) {
        TaskRow(
            task = task,
            onClick = onClick,
            onClickMore = onClickMore,
            onDeleteTask = onDeleteTask,
            onMarkTask = onMarkTask,
            modifier = modifier.longPressDraggableHandle(
                interactionSource = interaction,
                onDragStarted = {
                    onDragStarted()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragStopped = {
                    onDragStopped()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            ),
            interactionSource = interaction,
            onCompletionAnimationFinished = onCompletionAnimationFinished,
        )
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
