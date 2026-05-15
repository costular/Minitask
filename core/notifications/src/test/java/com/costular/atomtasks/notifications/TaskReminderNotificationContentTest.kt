package com.costular.atomtasks.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class TaskReminderNotificationContentTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val notificationResources = FakeNotificationResources()
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun `applyTaskReminderContent uses task name as notification title`() {
        val taskName = "Prepare quarterly roadmap"

        val notification = NotificationCompat.Builder(context, "reminders")
            .applyTaskReminderContent(taskName, "1/2/26, 9:05 AM")
            .build()

        assertThat(notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
            .isEqualTo(taskName)
    }

    @Test
    fun `applyTaskReminderContent uses reminder date time as notification text`() {
        val reminderDateTimeText = "1/2/26, 9:05 AM"

        val notification = NotificationCompat.Builder(context, "reminders")
            .applyTaskReminderContent("Prepare quarterly roadmap", reminderDateTimeText)
            .build()

        assertThat(notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
            .isEqualTo(reminderDateTimeText)
        assertThat(notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
            .isEqualTo(reminderDateTimeText)
    }

    @Test
    fun `remindTask uses task detail navigation pending intent`() {
        val taskId = 42L
        val notificationNavigationIntentFactory = FakeNotificationNavigationIntentFactory(context)
        val manager = TaskNotificationManagerImpl(
            context,
            notificationNavigationIntentFactory,
            notificationResources,
        )

        manager.remindTask(
            taskId = taskId,
            taskName = "Prepare quarterly roadmap",
            reminderDateTime = LocalDateTime.of(2024, 5, 15, 18, 23),
        )

        val notification = shadowOf(notificationManager).getNotification(taskId.toInt())
        assertThat(notification.contentIntent)
            .isEqualTo(notificationNavigationIntentFactory.openTaskDetailPendingIntent)
        assertThat(notificationNavigationIntentFactory.openTaskDetailTaskId).isEqualTo(taskId)
    }

    @Test
    fun `remindTask actions keep expected task id extras`() {
        val taskId = 42L
        val manager = TaskNotificationManagerImpl(
            context,
            FakeNotificationNavigationIntentFactory(context),
            notificationResources,
        )

        manager.remindTask(
            taskId = taskId,
            taskName = "Prepare quarterly roadmap",
            reminderDateTime = LocalDateTime.of(2024, 5, 15, 18, 23),
        )

        val notification = shadowOf(notificationManager).getNotification(taskId.toInt())
        val doneIntent = shadowOf(notification.actions[0].actionIntent).savedIntent
        val postponeIntent = shadowOf(notification.actions[1].actionIntent).savedIntent
        assertThat(doneIntent.getLongExtra("task_id", -1L)).isEqualTo(taskId)
        assertThat(postponeIntent.getLongExtra("postpone_param_task_id", -1L)).isEqualTo(taskId)
    }

    @Test
    fun `formatReminderNotificationDateTime uses readable 12 hour time and localized date`() {
        val reminderDateTime = LocalDateTime.of(2024, 5, 15, 18, 23)

        val formatted = reminderDateTime.formatReminderNotificationDateTime(
            locale = Locale.US,
            use24HourFormat = false,
        )

        assertThat(formatted).contains("6:23")
        assertThat(formatted).contains("PM")
        assertThat(formatted).contains("Wednesday")
        assertThat(formatted).contains("May")
        assertThat(formatted).contains("15")
        assertThat(formatted).contains("·")
    }

    @Test
    fun `formatReminderNotificationDateTime uses readable 24 hour time and localized date`() {
        val reminderDateTime = LocalDateTime.of(2024, 5, 15, 18, 23)
        val spanishLocale = Locale.forLanguageTag("es-ES")

        val formatted = reminderDateTime.formatReminderNotificationDateTime(
            locale = spanishLocale,
            use24HourFormat = true,
        )

        assertThat(formatted).contains("18:23")
        assertThat(formatted.lowercase(spanishLocale)).contains("miércoles")
        assertThat(formatted.lowercase(spanishLocale)).contains("may")
        assertThat(formatted).contains("15")
        assertThat(formatted).contains("·")
        assertThat(formatted).doesNotContain("PM")
        assertThat(formatted).doesNotContain("AM")
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
        val openTaskDetailPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            2,
            Intent("open_task_detail"),
            PendingIntent.FLAG_IMMUTABLE,
        )
        var openTaskDetailTaskId: Long? = null

        override fun openApp(): PendingIntent = openAppPendingIntent

        override fun openTaskDetail(taskId: Long): PendingIntent {
            openTaskDetailTaskId = taskId
            return openTaskDetailPendingIntent
        }
    }
}
