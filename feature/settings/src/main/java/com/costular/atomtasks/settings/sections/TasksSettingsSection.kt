package com.costular.atomtasks.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.utils.ofLocalizedTime
import com.costular.atomtasks.data.settings.dailyreminder.DailyReminder
import com.costular.atomtasks.settings.SettingSection
import com.costular.atomtasks.settings.SettingSwitch
import com.costular.atomtasks.settings.components.SettingDivider
import com.costular.atomtasks.settings.components.SettingOption
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalTime

@Composable
fun TasksSettingsSection(
    isMoveUndoneTasksTomorrowEnabled: Boolean,
    dailyReminder: DailyReminder?,
    isTaskListSectionsEnabled: Boolean,
    onSetTaskListSections: (Boolean) -> Unit,
    onEnableDailyReminder: (Boolean) -> Unit,
    onClickDailyReminder: () -> Unit,
    onSetMoveUndoneTasksTomorrow: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingSection(
        title = stringResource(R.string.settings_tasks_title),
        modifier = modifier.fillMaxWidth(),
    ) {
        DailyReminderItem(
            dailyReminder = dailyReminder,
            onEnableDailyReminder = onEnableDailyReminder
        )

        SettingOption(
            title = stringResource(R.string.settings_tasks_daily_reminder_time_label),
            option = dailyReminder?.time?.ofLocalizedTime() ?: "-",
            icon = null,
            onClick = onClickDailyReminder,
            enabled = dailyReminder?.isEnabled ?: false,
        )

        SettingDivider()

        AutoPostponeItem(
            isMoveUndoneTasksTomorrowEnabled = isMoveUndoneTasksTomorrowEnabled,
            onSetMoveUndoneTasksTomorrow = onSetMoveUndoneTasksTomorrow,
        )

        SettingDivider()

        TaskListSectionsItem(
            isEnabled = isTaskListSectionsEnabled,
            onSetTaskListSections = onSetTaskListSections,
        )
    }
}

@Composable
private fun DailyReminderItem(
    dailyReminder: DailyReminder?,
    onEnableDailyReminder: (Boolean) -> Unit
) {
    SettingSwitch(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_tasks_daily_reminder_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_tasks_daily_reminder_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        start = {
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Top)
            )
        },
        isSelected = dailyReminder?.isEnabled ?: false,
        onSelect = onEnableDailyReminder,
    )
}

@Composable
private fun AutoPostponeItem(
    isMoveUndoneTasksTomorrowEnabled: Boolean,
    onSetMoveUndoneTasksTomorrow: (Boolean) -> Unit,
) {
    SettingSwitch(
        start = {
            Icon(
                imageVector = Icons.Outlined.FastForward,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Top)
            )
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_tasks_autoforward_tasks_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.settings_tasks_autoforward_tasks_description,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        isSelected = isMoveUndoneTasksTomorrowEnabled,
        onSelect = onSetMoveUndoneTasksTomorrow,
    )
}

@Composable
private fun TaskListSectionsItem(
    isEnabled: Boolean,
    onSetTaskListSections: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingSwitch(
        start = {
            Icon(
                imageVector = Icons.Outlined.ViewList,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Top),
            )
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_tasks_sections_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_tasks_sections_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        isSelected = isEnabled,
        onSelect = onSetTaskListSections,
        modifier = modifier,
    )
}

@Preview
@Composable
fun TasksSettingsSectionPreview() {
    AtomTheme {
        TasksSettingsSection(
            dailyReminder = DailyReminder(true, LocalTime.of(8, 0)),
            isMoveUndoneTasksTomorrowEnabled = true,
            isTaskListSectionsEnabled = true,
            onSetTaskListSections = {},
            onSetMoveUndoneTasksTomorrow = {},
            onClickDailyReminder = {},
            onEnableDailyReminder = {},
        )
    }
}
