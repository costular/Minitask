package com.costular.atomtasks.notifications

import android.content.Context
import androidx.core.app.NotificationCompat

internal fun Context.buildNotificationBase(
    channel: String,
    notificationResources: NotificationResources,
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channel)
        .setSmallIcon(notificationResources.smallIcon)
        .setColor(notificationResources.color)

internal fun generateRandomRequestCode(): Int {
    return (0..Int.MAX_VALUE).random()
}

internal const val UpdateFlag =
    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
