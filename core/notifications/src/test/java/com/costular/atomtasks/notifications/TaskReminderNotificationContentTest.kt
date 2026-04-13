package com.costular.atomtasks.notifications

import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class TaskReminderNotificationContentTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `applyTaskReminderContent uses task name as notification title`() {
        val taskName = "Prepare quarterly roadmap"

        val notification = NotificationCompat.Builder(context, "reminders")
            .applyTaskReminderContent(taskName)
            .build()

        assertThat(notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
            .isEqualTo(taskName)
    }

    @Test
    fun `applyTaskReminderContent keeps full long task name in expanded content`() {
        val taskName = buildString {
            append("Review architecture proposal for adaptive notification layout and ")
            append("validate every long-title edge case before the release candidate goes live")
        }

        val notification = NotificationCompat.Builder(context, "reminders")
            .applyTaskReminderContent(taskName)
            .build()

        assertThat(notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
            .isEqualTo(taskName)
    }
}
