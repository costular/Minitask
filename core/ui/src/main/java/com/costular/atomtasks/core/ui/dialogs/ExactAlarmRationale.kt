package com.costular.atomtasks.core.ui.dialogs

import android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.costular.atomtasks.core.ui.R.string as S

@Composable
fun ExactAlarmRationale(
    onDismiss: () -> Unit,
    navigateToExactAlarmSettings: () -> Unit,
    onPermissionStateChanged: () -> Unit,
) {
    ObserveScheduleExactAlarmPermissionState(onPermissionStateChanged)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    S.create_task_schedule_exact_reminder_permission_missing_title
                )
            )
        },
        text = {
            Text(
                stringResource(
                    S.create_task_schedule_exact_reminder_permission_missing_description
                )
            )
        },
        confirmButton = {
            TextButton(onClick = navigateToExactAlarmSettings) {
                Text(
                    stringResource(
                        S.create_task_schedule_exact_reminder_permission_missing_confirm
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(
                        S.create_task_schedule_exact_reminder_permission_missing_dismiss
                    )
                )
            }
        }
    )
}

@Composable
private fun ObserveScheduleExactAlarmPermissionState(onPermissionStateChanged: () -> Unit) {
    val context = LocalContext.current

    DisposableEffect(context) {
        val intentFilter = IntentFilter(ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onPermissionStateChanged()
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter)

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }
}
