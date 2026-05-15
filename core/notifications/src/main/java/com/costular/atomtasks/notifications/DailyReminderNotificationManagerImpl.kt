package com.costular.atomtasks.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DailyReminderNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationNavigationIntentFactory: NotificationNavigationIntentFactory,
    private val notificationResources: NotificationResources,
) : DailyReminderNotificationManager {

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val dailyReminder =
            NotificationChannel(
                NotificationChannels.DailyReminder,
                notificationResources.dailyReminderChannelTitle,
                importance,
            ).apply {
                description = notificationResources.dailyReminderChannelDescription
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(dailyReminder)
    }

    @SuppressLint("MissingPermission")
    override fun showDailyReminderNotification() {
        val builder = context
            .buildNotificationBase(NotificationChannels.DailyReminder, notificationResources)
            .setContentTitle(notificationResources.dailyReminderTitle)
            .setContentText(notificationResources.dailyReminderDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationResources.dailyReminderDescription),
            )
            .setContentIntent(notificationNavigationIntentFactory.openApp())

        notificationManager.notify(DAILY_REMINDER_NOTIFICATION_ID, builder.build())
    }

    override fun removeDailyReminderNotification() {
        notificationManager.cancel(DAILY_REMINDER_NOTIFICATION_ID)
    }

    private companion object {
        const val DAILY_REMINDER_NOTIFICATION_ID = 9999990
    }
}
