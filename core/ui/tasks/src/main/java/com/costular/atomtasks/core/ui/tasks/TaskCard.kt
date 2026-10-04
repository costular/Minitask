package com.costular.atomtasks.core.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.utils.ofLocalizedTime
import com.costular.atomtasks.tasks.fake.TaskFinished
import com.costular.atomtasks.tasks.fake.TaskRecurring
import com.costular.atomtasks.tasks.fake.TaskRecurringWithReminder
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.fake.TaskWithReminder
import com.costular.atomtasks.tasks.format.localized
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.components.Markable
import com.costular.designsystem.decorator.strikeThrough
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme

const val ReminderIconId = "reminder"
const val RecurringIconId = "recurring"

@Composable
fun TaskCard(
    title: String,
    isFinished: Boolean,
    recurrenceType: RecurrenceType?,
    reminder: Reminder?,
    onMark: (Boolean) -> Unit,
    onClick: () -> Unit,
    onClickMore: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onCompletionAnimationFinished: (Boolean) -> Unit = {},
) {
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val shouldShowExtraDetails = !isFinished && (reminder != null || recurrenceType != null)

    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(),
    ) {
        val reminderInlineContent = reminderInline(contentColor)
        val recurringInlineContent = recurringInline(contentColor)

        Row(
            modifier = Modifier.padding(vertical = AppTheme.dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(AppTheme.dimens.spacingLarge))

            Markable(
                isMarked = isFinished,
                onMarkChanged = onMark,
            )

            Spacer(modifier = Modifier.width(AppTheme.dimens.spacingLarge))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                TaskTitle(
                    isFinished = isFinished,
                    title = title,
                    onAnimationFinished = onCompletionAnimationFinished,
                )

                if (shouldShowExtraDetails) {
                    Spacer(Modifier.height(AppTheme.dimens.spacingSmall))
                }

                AnimatedVisibility(shouldShowExtraDetails) {
                    this@Column.TaskDetails(
                        recurrenceType = recurrenceType,
                        reminder = reminder,
                        contentColor = contentColor,
                        reminderInlineContent = reminderInlineContent,
                        recurringInlineContent = recurringInlineContent
                    )
                }
            }

            IconButton(
                onClick = onClickMore,
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(end = AppTheme.dimens.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.task_more_actions),
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.TaskDetails(
    recurrenceType: RecurrenceType?,
    reminder: Reminder?,
    contentColor: Color,
    reminderInlineContent: Map<String, InlineTextContent>,
    recurringInlineContent: Map<String, InlineTextContent>
) {
    val recurringContent = if (recurrenceType != null) {
        val label = recurrenceType.localized()
        RecurringContent.Recurring(label)
    } else {
        RecurringContent.None
    }

    val hasReminder = reminder != null
    val hasRecurringInfo = recurringContent is RecurringContent.Recurring

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (reminder != null) {
            Row {
                val alarmText = buildAnnotatedString {
                    appendInlineContent(
                        ReminderIconId,
                        "[alarm]"
                    )
                    append(" ")
                    append(reminder.time.ofLocalizedTime())
                }
                Text(
                    text = alarmText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    inlineContent = reminderInlineContent,
                )
            }
        }

        if (hasReminder && hasRecurringInfo) {
            Canvas(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.spacingMedium)
                    .size(3.dp),
                onDraw = {
                    drawCircle(color = contentColor.copy(alpha = 0.66f))
                }
            )
        }

        if (hasRecurringInfo) {
            val content = recurringContent as RecurringContent.Recurring

            val recurringText = buildAnnotatedString {
                appendInlineContent(
                    RecurringIconId,
                    "[recurring]"
                )
                append(" ")
                append(content.recurrenceLabel)
            }
            Text(
                text = recurringText,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                inlineContent = recurringInlineContent,
            )
        }
    }
}

@Composable
private fun TaskTitle(
    isFinished: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    onAnimationFinished: (Boolean) -> Unit = {},
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val progress = remember { Animatable(if (isFinished) 1f else 0f) }
    val latestOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    val latestIsFinished by rememberUpdatedState(isFinished)

    LaunchedEffect(isFinished) {
        progress.animateTo(
            targetValue = if (isFinished) 1f else 0f,
            animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
        )
        latestOnAnimationFinished(isFinished)
    }
    // A row leaving the viewport must not leave a section transition waiting on an invisible title.
    DisposableEffect(Unit) {
        onDispose { latestOnAnimationFinished(latestIsFinished) }
    }

    Text(
        text = title,
        onTextLayout = {
            textLayoutResult = it
        },
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.strikeThrough(
            color = LocalContentColor.current,
            border = 2.dp,
            progress = { progress.value },
            textLayoutResult = textLayoutResult,
            enabled = true,
        )
    )
}

@Composable
private fun reminderInline(color: Color) = mapOf(
    Pair(
        ReminderIconId,
        InlineTextContent(
            Placeholder(
                width = 16.sp,
                height = 16.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = null,
                tint = color,
            )
        },
    ),
)

@Composable
private fun recurringInline(color: Color) = mapOf(
    Pair(
        RecurringIconId,
        InlineTextContent(
            Placeholder(
                width = 16.sp,
                height = 16.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = null,
                tint = color,
            )
        }
    )
)

private sealed interface RecurringContent {

    data object None : RecurringContent

    data class Recurring(
        val recurrenceLabel: String,
    ) : RecurringContent

}

@Preview(showBackground = true)
@Composable
private fun TaskCardPreview(
    @PreviewParameter(TaskPreviewProvider::class) task: Task,
) {
    AtomTheme {
        TaskCard(
            title = task.name,
            isFinished = task.isDone,
            recurrenceType = task.recurrenceType,
            onMark = {},
            onClick = {},
            reminder = task.reminder,
            modifier = Modifier.fillMaxWidth(),
            onClickMore = {},
        )
    }
}

private class TaskPreviewProvider : PreviewParameterProvider<Task> {
    override val values: Sequence<Task>
        get() = sequenceOf(
            TaskToday,
            TaskFinished,
            TaskWithReminder,
            TaskRecurring,
            TaskRecurringWithReminder,
        )
}
