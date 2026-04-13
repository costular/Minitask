package com.costular.atomtasks.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.costular.atomtasks.core.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TaskNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TaskNotificationManager {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val name = context.getString(R.string.notification_channel_reminders_title)
        val descriptionText =
            context.getString(R.string.notification_channel_reminders_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val reminders =
            NotificationChannel(NotificationChannels.Reminders, name, importance).apply {
                description = descriptionText
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(reminders)
    }

    override fun remindTask(taskId: Long, taskName: String) {
        val builder = context.buildNotificationBase(NotificationChannels.Reminders)
            .applyTaskReminderContent(taskName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .openAppContentIntent(context)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(
                buildDoneAction(taskId),
            )
            .addAction(
                buildPostponeAction(taskId),
            )

        notify(taskId.toInt(), builder.build())
    }

    private fun buildPostponeAction(taskId: Long) = NotificationCompat.Action.Builder(
        0,
        context.getString(R.string.notification_reminder_postpone),
        PendingIntent.getActivity(
            context,
            generateRandomRequestCode(),
            Intent().apply {
                action = Intent.ACTION_VIEW
                component = ComponentName(
                    context.packageName,
                    POSTPONE_ACTIVITY_NAME,
                )
                putExtra(
                    "postpone_param_task_id",
                    taskId,
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            UpdateFlag,
        )
    ).build()

    private fun buildDoneAction(taskId: Long) = NotificationCompat.Action.Builder(
        0,
        context.getString(R.string.notification_reminder_done),
        PendingIntent.getBroadcast(
            context,
            generateRandomRequestCode(),
            Intent().apply {
                action = Intent.ACTION_VIEW
                component = ComponentName(
                    context.packageName,
                    MARK_TASK_AS_DONE_RECEIVER,
                )
                putExtra(
                    "task_id",
                    taskId,
                )
            },
            UpdateFlag,
        ),
    ).build()

    override fun removeTaskNotification(taskId: Long) {
        notificationManager.cancel(taskId.toInt())
    }

    @SuppressLint("MissingPermission")
    private fun notify(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    companion object {
        const val POSTPONE_ACTIVITY_NAME =
            "com.costular.atomtasks.postponetask.ui.PostponeTaskActivity"
        const val MARK_TASK_AS_DONE_RECEIVER =
            "com.costular.atomtasks.tasks.receiver.MarkTaskAsDoneReceiver"
    }
}

internal fun NotificationCompat.Builder.applyTaskReminderContent(
    taskName: String,
): NotificationCompat.Builder = apply {
    setContentTitle(taskName)
    setStyle(
        NotificationCompat.BigTextStyle()
            .bigText(taskName),
    )
}
