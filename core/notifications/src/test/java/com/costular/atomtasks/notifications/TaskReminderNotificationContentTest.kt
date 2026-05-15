package com.costular.atomtasks.notifications

import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import java.util.Locale
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
}
