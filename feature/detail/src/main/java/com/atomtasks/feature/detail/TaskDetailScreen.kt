@file:Suppress("TooManyFunctions")

package com.atomtasks.feature.detail

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.dialogs.ExactAlarmRationale
import com.costular.atomtasks.core.ui.mvi.EventObserver
import com.costular.atomtasks.core.ui.utils.DateUtils.dayAsText
import com.costular.atomtasks.core.ui.utils.ofLocalizedTime
import com.costular.atomtasks.tasks.format.localized
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiHandler
import com.costular.designsystem.components.AtomTopBar
import com.costular.designsystem.components.Markable
import com.costular.designsystem.components.PrimaryButton
import com.costular.designsystem.dialogs.DatePickerDialog
import com.costular.designsystem.dialogs.TimePickerDialog
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalDate
import java.time.LocalTime
import com.costular.atomtasks.core.ui.R.string as S

private const val FieldMaxLines = 5
private const val FieldMinLines = 1

@OptIn(ExperimentalFoundationApi::class)
@Destination<TaskDetailGraph>(
    start = true,
    navArgs = TaskDetailNavArgs::class,
)
@Composable
fun TaskDetailScreen(
    navigator: DestinationsNavigator,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    EventObserver(viewModel.uiEvents) { event ->
        when (event) {
            is TaskDetailUiEvent.Close -> navigator.navigateUp()

            is TaskDetailUiEvent.NavigateToExactAlarmSettings -> {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
                    context.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
    }

    TaskDetailScreen(
        uiState = uiState,
        onSelectDate = viewModel::onSelectDateClicked,
        onSelectReminder = viewModel::onSelectReminderClicked,
        onSelectRecurrence = viewModel::onSelectRecurrenceClicked,
        onRecurrenceChanged = viewModel::onRecurrenceChanged,
        onSave = viewModel::save,
        onCloseSelectDate = viewModel::closeSelectDate,
        onDateChanged = viewModel::onDateChanged,
        onClearReminder = viewModel::onReminderRemoved,
        onSetReminder = viewModel::onReminderChanged,
        onCloseSelectReminder = viewModel::closeSelectReminder,
        onExactAlarmSettingChanged = viewModel::exactAlarmSettingChanged,
        navigateToExactAlarmSettings = viewModel::navigateToExactAlarmSettings,
        onCloseSelectRecurrence = viewModel::closeSelectRecurrence,
        onCancelRecurringEdition = viewModel::cancelRecurringEdition,
        onConfirmRecurringEdition = viewModel::confirmRecurringEdition,
        onClose = viewModel::onClose,
        onClearRecurrence = viewModel::clearRecurrence,
        onMarkTask = viewModel::onMarkTask,
        onDelete = viewModel::onDelete,
        onConfirmDeleteRecurring = viewModel::deleteRecurringTask,
        onConfirmDelete = viewModel::deleteTask,
        onDismissDelete = viewModel::dismissDeleteConfirmation,
        onDismissDiscardChanges = viewModel::cancelDiscardChanges,
        onDiscardChanges = viewModel::discardChanges,
    )
}

@Suppress("LongParameterList", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onClose: () -> Unit,
    onSelectDate: () -> Unit,
    onSelectReminder: () -> Unit,
    onClearReminder: () -> Unit,
    onSelectRecurrence: () -> Unit,
    onClearRecurrence: () -> Unit,
    onRecurrenceChanged: (RecurrenceType?) -> Unit,
    onSave: () -> Unit,
    onCloseSelectDate: () -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onSetReminder: (LocalTime) -> Unit,
    onCloseSelectReminder: () -> Unit,
    onExactAlarmSettingChanged: () -> Unit,
    navigateToExactAlarmSettings: () -> Unit,
    onCloseSelectRecurrence: () -> Unit,
    onCancelRecurringEdition: () -> Unit,
    onConfirmRecurringEdition: (EditRecurringTaskResponse) -> Unit,
    onMarkTask: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: (id: Long) -> Unit,
    onConfirmDeleteRecurring: (id: Long, strategy: RecurringRemovalStrategy) -> Unit,
    onDismissDiscardChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
) {
    BackHandler(
        enabled = true,
        onBack = onClose,
    )

    if (uiState.showSetDate) {
        DatePickerDialog(
            onDismiss = onCloseSelectDate,
            currentDate = uiState.taskState.date,
            onDateSelected = onDateChanged,
        )
    }

    if (uiState.showSetReminder) {
        if (uiState.shouldShowExactAlarmRationale) {
            ExactAlarmRationale(
                onDismiss = onCloseSelectReminder,
                navigateToExactAlarmSettings = navigateToExactAlarmSettings,
                onPermissionStateChanged = onExactAlarmSettingChanged
            )
        } else {
            NotificationPermissionEffect(
                onRevoke = onCloseSelectReminder
            ) {
                TimePickerDialog(
                    onDismiss = onCloseSelectReminder,
                    selectedTime = uiState.taskState.reminder,
                    onSelectTime = onSetReminder,
                )
            }
        }
    }

    if (uiState.showSetRecurrence) {
        RecurrenceTypePickerDialog(
            recurrenceType = uiState.taskState.recurrenceType,
            onRecurrenceTypeSelected = onRecurrenceChanged,
            onDismissRequest = onCloseSelectRecurrence,
        )
    }

    if (uiState.taskToSave != null) {
        EditRecurringTaskConfirmDialog(
            onCancel = onCancelRecurringEdition,
            onEdit = onConfirmRecurringEdition,
        )
    }

    if (uiState.shouldShowDiscardChangesConfirmation) {
        DiscardUnsavedChangesDialog(
            onDismiss = onDismissDiscardChanges,
            onDiscard = onDiscardChanges,
        )
    }

    RemoveTaskConfirmationUiHandler(
        uiState = uiState.removeTaskConfirmationUiState,
        onDismiss = onDismissDelete,
        onDeleteRecurring = onConfirmDeleteRecurring,
        onDelete = onConfirmDelete,
    )

    TaskDetailContent(
        uiState = uiState,
        onSelectDate = onSelectDate,
        onSelectReminder = onSelectReminder,
        onSelectRecurrence = onSelectRecurrence,
        onSave = onSave,
        onClearReminder = onClearReminder,
        onClose = onClose,
        onClearRecurrence = onClearRecurrence,
        onMarkTask = onMarkTask,
        onDelete = onDelete,
    )
}

@Suppress("LongParameterList", "LongMethod")
@ExperimentalFoundationApi
@Composable
private fun TaskDetailContent(
    uiState: TaskDetailUiState,
    onSelectDate: () -> Unit,
    onSelectReminder: () -> Unit,
    onClearReminder: () -> Unit,
    onSelectRecurrence: () -> Unit,
    onClearRecurrence: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onMarkTask: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Header(
                    onClose = onClose,
                    isEditing = uiState.isEditMode,
                    onDelete = onDelete,
                )

                Spacer(Modifier.height(AppTheme.dimens.spacingMedium))

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = AppTheme.dimens.contentMargin),
                ) {
                    if (uiState.isEditMode) {
                        Markable(
                            isMarked = uiState.isDone,
                            onMarkChanged = onMarkTask,
                        )

                        Spacer(Modifier.width(AppTheme.dimens.spacingMedium))
                    }

                    TaskInput(
                        name = uiState.taskState.name,
                        focusRequester = focusRequester,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(AppTheme.dimens.spacingXLarge))

                TaskDateSection(
                    localDate = uiState.taskState.date,
                    onSelectDate = onSelectDate,
                )

                TaskReminderSection(
                    reminder = uiState.taskState.reminder,
                    onSelectReminder = onSelectReminder,
                    onClearReminder = onClearReminder,
                )

                TaskRecurrenceSection(
                    recurrenceType = uiState.taskState.recurrenceType,
                    onSelectRecurrence = onSelectRecurrence,
                    onClearRecurrence = onClearRecurrence,
                )
            }

            PrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(AppTheme.dimens.contentMargin),
                onClick = onSave,
            ) {
                Text(stringResource(S.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Header(
    onClose: () -> Unit,
    isEditing: Boolean,
    onDelete: () -> Unit,
) {
    AtomTopBar(
        title = {},
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onClose,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

@Composable
private fun ColumnScope.TaskDateSection(
    localDate: LocalDate,
    onSelectDate: () -> Unit,
) {
    Field(
        icon = Icons.Outlined.CalendarToday,
        onClick = onSelectDate,
        content = {
            Text(text = dayAsText(localDate))
        },
        hasValue = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ColumnScope.TaskReminderSection(
    reminder: LocalTime?,
    onSelectReminder: () -> Unit,
    onClearReminder: () -> Unit,
) {
    val text =
        reminder?.ofLocalizedTime() ?: stringResource(S.task_detail_reminder_add_reminder)

    Field(
        icon = Icons.Outlined.Alarm,
        onClick = onSelectReminder,
        content = {
            AnimatedContent(text, label = "Reminder") {
                Text(text = it)
            }
        },
        isClearable = reminder != null,
        hasValue = reminder != null,
        onClear = onClearReminder,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ColumnScope.TaskRecurrenceSection(
    recurrenceType: RecurrenceType?,
    onSelectRecurrence: () -> Unit,
    onClearRecurrence: () -> Unit,
) {
    Field(
        icon = Icons.Outlined.Repeat,
        onClick = onSelectRecurrence,
        onClear = onClearRecurrence,
        content = {
            Text(text = recurrenceType.localized())
        },
        hasValue = recurrenceType != null,
        modifier = Modifier.fillMaxWidth()
    )
}


@ExperimentalFoundationApi
@Composable
private fun TaskInput(
    name: TextFieldState,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (name.text.isEmpty()) {
            Text(
                text = stringResource(S.task_detail_task_name_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = AppTheme.dimens.spacingLarge),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        BasicTextField(
            state = name,
            lineLimits = TextFieldLineLimits.MultiLine(FieldMinLines, FieldMaxLines),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = AppTheme.dimens.spacingLarge)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
fun Field(
    icon: ImageVector,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    hasValue: Boolean,
    modifier: Modifier = Modifier,
    isClearable: Boolean = false,
    onClear: () -> Unit = {},
) {
    val contentColor = if (hasValue) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        trailingContent = {
            if (isClearable) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                    )
                }
            }
        },
        headlineContent = content,
        colors = ListItemDefaults.colors(
            headlineColor = contentColor,
            leadingIconColor = contentColor,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier.clickable(enabled = true, onClick = onClick),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun TaskDetailScreenPreview(
    @PreviewParameter(TaskDetailPreviewParameterProvider::class) uiState: TaskDetailUiState,
) {
    AtomTheme {
        TaskDetailScreen(
            uiState = uiState,
            onSave = {},
            onSelectReminder = {},
            onSelectDate = {},
            onSelectRecurrence = {},
            onDateChanged = {},
            onCloseSelectDate = {},
            onClearReminder = {},
            onCloseSelectReminder = {},
            onExactAlarmSettingChanged = {},
            onSetReminder = {},
            navigateToExactAlarmSettings = {},
            onRecurrenceChanged = {},
            onCloseSelectRecurrence = {},
            onCancelRecurringEdition = {},
            onConfirmRecurringEdition = {},
            onClose = {},
            onClearRecurrence = {},
            onMarkTask = {},
            onDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onConfirmDeleteRecurring = { _, _ -> },
            onDiscardChanges = {},
            onDismissDiscardChanges = {},
        )
    }
}
