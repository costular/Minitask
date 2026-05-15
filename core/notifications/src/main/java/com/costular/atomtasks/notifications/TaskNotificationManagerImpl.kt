package com.costular.atomtasks.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.ConfigurationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TaskNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationNavigationIntentFactory: NotificationNavigationIntentFactory,
    private val notificationResources: NotificationResources,
) : TaskNotificationManager {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val reminders =
            NotificationChannel(
                NotificationChannels.Reminders,
                notificationResources.remindersChannelTitle,
                importance,
            ).apply {
                description = notificationResources.remindersChannelDescription
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(reminders)
    }

    override fun remindTask(taskId: Long, taskName: String, reminderDateTime: LocalDateTime) {
        val reminderDateTimeText = reminderDateTime.formatReminderNotificationDateTime(
            locale = context.notificationLocale(),
            use24HourFormat = DateFormat.is24HourFormat(context),
        )
        val builder = context
            .buildNotificationBase(NotificationChannels.Reminders, notificationResources)
            .applyTaskReminderContent(taskName, reminderDateTimeText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notificationNavigationIntentFactory.openTaskDetail(taskId))
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
        notificationResources.taskReminderPostponeAction,
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
        notificationResources.taskReminderDoneAction,
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
    reminderDateTimeText: String,
): NotificationCompat.Builder = apply {
    setContentTitle(taskName)
    setContentText(reminderDateTimeText)
    setStyle(
        NotificationCompat.BigTextStyle()
            .bigText(reminderDateTimeText),
    )
}

internal fun LocalDateTime.formatReminderNotificationDateTime(
    locale: Locale,
    use24HourFormat: Boolean,
): String {
    val date = Date.from(atZone(ZoneId.systemDefault()).toInstant())
    val timePattern = DateFormat.getBestDateTimePattern(
        locale,
        if (use24HourFormat) "Hm" else "hma",
    )
    val datePattern = DateFormat.getBestDateTimePattern(locale, "EEEEdMMM")
    val timeText = SimpleDateFormat(timePattern, locale).format(date)
    val dateText = SimpleDateFormat(datePattern, locale).format(date)

    return "$timeText · $dateText"
}

private fun Context.notificationLocale(): Locale =
    ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()
