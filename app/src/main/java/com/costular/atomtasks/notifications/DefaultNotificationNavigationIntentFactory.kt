package com.costular.atomtasks.notifications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import com.costular.atomtasks.ui.home.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultNotificationNavigationIntentFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationNavigationIntentFactory {

    override fun openApp(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            RequestOpenApp,
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun openTaskDetail(taskId: Long): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("$TaskDetailDeepLinkBase/$taskId"),
        ).apply {
            component = ComponentName(context, MainActivity::class.java)
        }

        return requireNotNull(
            TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(
                    taskId.openTaskRequestCode(),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
        )
    }

    private companion object {
        const val RequestOpenApp = 20
        const val RequestOpenTaskBase = 1_000
        const val TaskDetailDeepLinkBase = "https://atomtasks.app/tasks"

        fun Long.openTaskRequestCode(): Int = RequestOpenTaskBase + toInt()
    }
}
