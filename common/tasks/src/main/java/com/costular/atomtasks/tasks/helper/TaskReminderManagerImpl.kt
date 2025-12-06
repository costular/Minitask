package com.costular.atomtasks.tasks.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import com.costular.atomtasks.tasks.receiver.NotifyTaskReceiver
import java.time.LocalDateTime
import java.time.ZoneId

internal class TaskReminderManagerImpl(
    private val context: Context,
) : TaskReminderManager {

    private val alarmManager = context.getSystemService<AlarmManager>()
    override fun canScheduleReminders(): Boolean {
        checkNotNull(alarmManager)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun set(taskId: Long, localDateTime: LocalDateTime) {
        checkNotNull(alarmManager)

        if (localDateTime.isBefore(LocalDateTime.now())) {
            return
        }

        if (!canScheduleReminders()) {
            return
        }

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            buildTaskReminderPendingIntent(taskId),
        )
    }

    override fun cancel(taskId: Long) {
        checkNotNull(alarmManager)
        buildTaskReminderPendingIntent(taskId).also { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildTaskReminderPendingIntent(taskId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            Intent(context, NotifyTaskReceiver::class.java).apply {
                putExtra("task_id", taskId)
            },
            Flags,
        )

    private companion object {
        val Flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }
}
