package com.costular.atomtasks.data

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.worker.RestoreMissedTaskRemindersWorker
import com.costular.atomtasks.tasks.worker.SetTasksRemindersWorker
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class BootReceiverTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private lateinit var receiver: BootReceiver
    private val recurrenceScheduler: RecurrenceScheduler = mockk(relaxed = true)

    @Before
    fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        receiver = BootReceiver().apply {
            this.recurrenceScheduler = this@BootReceiverTest.recurrenceScheduler
        }
    }

    @Test
    fun `should enqueue restore and reset reminder workers on boot completed`() {
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        val workManager = WorkManager.getInstance(context)
        val restoreWorkInfos = workManager
            .getWorkInfosByTag(RestoreMissedTaskRemindersWorker::class.java.name)
            .get()
        val resetWorkInfos = workManager
            .getWorkInfosByTag(SetTasksRemindersWorker::class.java.name)
            .get()

        assertThat(restoreWorkInfos).hasSize(1)
        assertThat(resetWorkInfos).hasSize(1)
    }

    @Test
    fun `should ignore non boot intents`() {
        receiver.onReceive(context, Intent("custom.intent.ACTION"))

        val workManager = WorkManager.getInstance(context)
        val restoreWorkInfos = workManager
            .getWorkInfosByTag(RestoreMissedTaskRemindersWorker::class.java.name)
            .get()
        val resetWorkInfos = workManager
            .getWorkInfosByTag(SetTasksRemindersWorker::class.java.name)
            .get()

        assertThat(restoreWorkInfos).isEmpty()
        assertThat(resetWorkInfos).isEmpty()
    }
}
