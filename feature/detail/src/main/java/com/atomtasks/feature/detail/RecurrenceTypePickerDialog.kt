package com.atomtasks.feature.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.costular.atomtasks.core.ui.R.string as S

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceTypePickerDialog(
    recurrenceType: RecurrenceType?,
    onRecurrenceTypeSelected: (RecurrenceType?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(vertical = AppTheme.DialogPadding),
            ) {
                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_none),
                    isSelected = recurrenceType == null,
                    onClick = { onRecurrenceTypeSelected(null) },
                )

                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_daily),
                    isSelected = recurrenceType == RecurrenceType.DAILY,
                    onClick = { onRecurrenceTypeSelected(RecurrenceType.DAILY) },
                )

                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_weekdays),
                    isSelected = recurrenceType == RecurrenceType.WEEKDAYS,
                    onClick = { onRecurrenceTypeSelected(RecurrenceType.WEEKDAYS) },
                )

                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_weekly),
                    isSelected = recurrenceType == RecurrenceType.WEEKLY,
                    onClick = { onRecurrenceTypeSelected(RecurrenceType.WEEKLY) },
                )

                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_monthly),
                    isSelected = recurrenceType == RecurrenceType.MONTHLY,
                    onClick = { onRecurrenceTypeSelected(RecurrenceType.MONTHLY) },
                )

                ListItem(
                    text = stringResource(S.create_task_recurrence_picker_yearly),
                    isSelected = recurrenceType == RecurrenceType.YEARLY,
                    onClick = { onRecurrenceTypeSelected(RecurrenceType.YEARLY) },
                )
            }
        }
    }
}

@Composable
private fun ListItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        RadioButton(
            modifier = Modifier.padding(start = AppTheme.DialogPadding),
            selected = isSelected,
            onClick = onClick,
        )

        Spacer(Modifier.width(AppTheme.dimens.spacingMedium))

        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = AppTheme.dimens.contentMargin),
        )
    }
}

@Preview
@Composable
private fun RecurrenceTypePickerPreview() {
    AtomTheme {
        var recurrenceType by remember {
            mutableStateOf<RecurrenceType?>(null)
        }

        RecurrenceTypePickerDialog(
            recurrenceType = recurrenceType,
            onRecurrenceTypeSelected = {
                recurrenceType = it
            },
            onDismissRequest = {},
        )
    }
}
