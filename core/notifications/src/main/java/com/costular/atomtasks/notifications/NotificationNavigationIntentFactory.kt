package com.costular.atomtasks.notifications

import android.app.PendingIntent

interface NotificationNavigationIntentFactory {
    fun openApp(): PendingIntent
    fun openTaskDetail(taskId: Long): PendingIntent
}
