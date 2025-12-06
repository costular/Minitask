package com.costular.atomtasks.tasks.worker

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class NotifyTaskWorkerTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun `should fail when notify task given no task id was passed`() {
        val request = OneTimeWorkRequestBuilder<NotifyTaskWorker>()
            .setInputData(workDataOf("task_id" to null))
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val workInfo = workManager.getWorkInfoById(request.id).get()

        Truth.assertThat(workInfo!!.state).isEqualTo(WorkInfo.State.FAILED)
    }
}
