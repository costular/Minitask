package com.costular.atomtasks.core.ui.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.utils.VariantsPreview
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalDate
import java.time.LocalTime
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Suppress("LongMethod")
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
    padding: PaddingValues = PaddingValues(0.dp),
    reorderingEnabled: Boolean = true,
    sectionsEnabled: Boolean = false,
) {
    val sections = remember(sectionsEnabled) {
        if (sectionsEnabled) TaskListSections(tasks) else null
    }
    var completedExpanded by rememberSaveable { mutableStateOf(true) }
    val latestTasks by rememberUpdatedState(tasks)
    val latestOnMove by rememberUpdatedState(onMove)
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        taskListMove(latestTasks, from.key, to.key, sections)
            ?.let { (source, target) -> latestOnMove(source, target) }
    }
    SideEffect {
        sections?.synchronize(
            tasks,
            lazyListState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? Long }.toSet(),
        )
    }
    val (completed, pending) = tasks.partition { sections?.isCompleted(it) == true }

    if (tasks.isEmpty()) {
        Empty(modifier.padding(AppTheme.dimens.contentMargin))
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sectionsEnabled) {
                item(key = "pending_header", contentType = "header") {
                    PendingTasksHeader(pending.size, Modifier.fillMaxWidth().animateItem())
                }
                if (pending.isEmpty()) {
                    item(key = "pending_empty", contentType = "empty") {
                        SectionEmpty(stringResource(R.string.task_list_no_pending), Modifier.animateItem())
                    }
                }
            }
            // Keep task IDs as keys across both sections so Compose animates their movement.
            val rowContent: @Composable LazyItemScope.(Task) -> Unit = { task ->
                TaskListRow(
                    task = task,
                    state = reorderState,
                    reorderingEnabled = reorderingEnabled,
                    onClick = onClick,
                    onClickMore = onClickMore,
                    onDeleteTask = onDeleteTask,
                    onMarkTask = onMarkTask,
                    onDragStopped = onDragStopped,
                    onCompletionAnimationFinished = { isDone ->
                        sections?.finishAnimation(task.id, isDone, latestTasks)
                    },
                )
            }
            items(
                if (sectionsEnabled) pending else tasks,
                key = { it.id },
                contentType = { "task" },
                itemContent = rowContent,
            )
            if (sectionsEnabled) {
                item(key = "completed_header", contentType = "header") {
                    CompletedTasksHeader(
                        count = completed.size,
                        expanded = completedExpanded,
                        onToggle = { completedExpanded = !completedExpanded },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                    )
                }
                if (completedExpanded) {
                    if (completed.isEmpty()) {
                        item(key = "completed_empty", contentType = "empty") {
                            SectionEmpty(stringResource(R.string.task_list_no_completed), Modifier.animateItem())
                        }
                    }
                    items(completed, key = { it.id }, contentType = { "task" }, itemContent = rowContent)
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.TaskListRow(
    task: Task,
    state: ReorderableLazyListState,
    reorderingEnabled: Boolean,
    onClick: (Task) -> Unit,
    onClickMore: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onMarkTask: (Long, Boolean) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    onCompletionAnimationFinished: (Boolean) -> Unit = {},
) {
    if (reorderingEnabled) {
        ReorderableTaskRow(
            state = state,
            task = task,
            onClick = onClick,
            onClickMore = onClickMore,
            onDeleteTask = onDeleteTask,
            onMarkTask = onMarkTask,
            onDragStopped = onDragStopped,
            modifier = modifier,
            onCompletionAnimationFinished = onCompletionAnimationFinished,
        )
    } else {
        TaskRow(
            task = task,
            onClick = onClick,
            onClickMore = onClickMore,
            onDeleteTask = onDeleteTask,
            onMarkTask = onMarkTask,
            modifier = modifier.animateItem(),
            onCompletionAnimationFinished = onCompletionAnimationFinished,
        )
    }
}

@Composable
private fun PendingTasksHeader(count: Int, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = {
            Text(
                stringResource(R.string.task_list_section_count, stringResource(R.string.task_list_pending), count),
                style = MaterialTheme.typography.titleSmall,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.semantics { heading() },
    )
}

private const val ExpandedChevronRotation = 180f

@Composable
private fun CompletedTasksHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = stringResource(
        if (expanded) R.string.task_list_collapse_completed else R.string.task_list_expand_completed,
    )
    val expandedDescription = stringResource(
        if (expanded) R.string.task_list_expanded else R.string.task_list_collapsed,
    )
    val rotation = animateFloatAsState(
        if (expanded) ExpandedChevronRotation else 0f,
        label = "Completed section chevron",
    )
    ListItem(
        modifier = modifier
            .clickable(role = Role.Button, onClickLabel = action, onClick = onToggle)
            .semantics {
                heading()
                stateDescription = expandedDescription
            },
        headlineContent = {
            Text(
                stringResource(
                    R.string.task_list_section_count,
                    stringResource(R.string.task_list_completed),
                    count,
                ),
                style = MaterialTheme.typography.titleSmall,
            )
        },
        trailingContent = {
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SectionEmpty(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
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
            sectionsEnabled = true,
        )
    }
}
