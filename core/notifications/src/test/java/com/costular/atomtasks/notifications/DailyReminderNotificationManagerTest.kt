package com.costular.atomtasks.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class DailyReminderNotificationManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun `showDailyReminderNotification uses open app navigation pending intent`() {
        val notificationNavigationIntentFactory = FakeNotificationNavigationIntentFactory(context)
        val manager = DailyReminderNotificationManagerImpl(
            context,
            notificationNavigationIntentFactory,
            FakeNotificationResources(),
        )

        manager.showDailyReminderNotification()

        val notification = shadowOf(notificationManager).getNotification(DailyReminderNotificationId)
        assertThat(notification.contentIntent)
            .isEqualTo(notificationNavigationIntentFactory.openAppPendingIntent)
    }

    private class FakeNotificationNavigationIntentFactory(
        private val context: Context,
    ) : NotificationNavigationIntentFactory {
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent("open_app"),
            PendingIntent.FLAG_IMMUTABLE,
        )

        override fun openApp(): PendingIntent = openAppPendingIntent

        override fun openTaskDetail(taskId: Long): PendingIntent {
            error("Unexpected task detail navigation")
        }
    }

    private companion object {
        const val DailyReminderNotificationId = 9999990
    }
}
