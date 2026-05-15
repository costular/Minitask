package com.costular.atomtasks.notifications

internal class FakeNotificationResources : NotificationResources {
    override val smallIcon: Int = android.R.drawable.ic_dialog_info
    override val color: Int = 0xFF000000.toInt()
    override val remindersChannelTitle: String = "Reminders"
    override val remindersChannelDescription: String = "Reminders for your tasks"
    override val dailyReminderChannelTitle: String = "Daily reminder"
    override val dailyReminderChannelDescription: String = "Daily reminder description"
    override val dailyReminderTitle: String = "What do you need to do today?"
    override val dailyReminderDescription: String = "Tap here to organize them on Minitask"
    override val taskReminderDoneAction: String = "Done"
    override val taskReminderPostponeAction: String = "Postpone"
}
