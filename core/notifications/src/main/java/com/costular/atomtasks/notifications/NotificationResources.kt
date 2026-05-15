package com.costular.atomtasks.notifications

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.costular.atomtasks.core.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface NotificationResources {
    @get:DrawableRes
    val smallIcon: Int

    @get:ColorInt
    val color: Int

    val remindersChannelTitle: String
    val remindersChannelDescription: String
    val dailyReminderChannelTitle: String
    val dailyReminderChannelDescription: String
    val dailyReminderTitle: String
    val dailyReminderDescription: String
    val taskReminderDoneAction: String
    val taskReminderPostponeAction: String
}

class AndroidNotificationResources @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationResources {
    override val smallIcon: Int = R.drawable.ic_minitask
    override val color: Int
        get() = context.getColor(R.color.primary)
    override val remindersChannelTitle: String
        get() = context.getString(R.string.notification_channel_reminders_title)
    override val remindersChannelDescription: String
        get() = context.getString(R.string.notification_channel_reminders_description)
    override val dailyReminderChannelTitle: String
        get() = context.getString(R.string.notification_channel_daily_reminder)
    override val dailyReminderChannelDescription: String
        get() = context.getString(R.string.notification_channel_daily_reminder_description)
    override val dailyReminderTitle: String
        get() = context.getString(R.string.notification_daily_reminder_title)
    override val dailyReminderDescription: String
        get() = context.getString(R.string.notification_daily_reminder_description)
    override val taskReminderDoneAction: String
        get() = context.getString(R.string.notification_reminder_done)
    override val taskReminderPostponeAction: String
        get() = context.getString(R.string.notification_reminder_postpone)
}
