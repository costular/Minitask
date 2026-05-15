package com.costular.atomtasks.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.costular.atomtasks.ui.home.MainActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class DefaultNotificationNavigationIntentFactoryTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val factory = DefaultNotificationNavigationIntentFactory(context)

    @Test
    fun `openTaskDetail creates view pending intent for task detail deep link`() {
        val pendingIntent = factory.openTaskDetail(42L)

        val intent = shadowOf(pendingIntent).savedIntent
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data.toString()).isEqualTo("https://atomtasks.app/tasks/42")
        assertThat(intent.component?.className).isEqualTo(MainActivity::class.java.name)
    }

    @Test
    fun `openTaskDetail uses distinct request codes per task`() {
        val firstRequestCode = shadowOf(factory.openTaskDetail(42L)).requestCode
        val secondRequestCode = shadowOf(factory.openTaskDetail(43L)).requestCode

        assertThat(firstRequestCode).isEqualTo(1_042)
        assertThat(secondRequestCode).isEqualTo(1_043)
    }

    @Test
    fun `openTaskDetail uses immutable update current flags`() {
        val flags = shadowOf(factory.openTaskDetail(42L)).flags

        assertThat(flags and PendingIntent.FLAG_IMMUTABLE).isNotEqualTo(0)
        assertThat(flags and PendingIntent.FLAG_UPDATE_CURRENT).isNotEqualTo(0)
    }

    @Test
    fun `openApp creates view pending intent for main activity`() {
        val pendingIntent = factory.openApp()

        val intent = shadowOf(pendingIntent).savedIntent
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.component?.className).isEqualTo(MainActivity::class.java.name)
    }
}
