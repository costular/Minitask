package com.costular.atomtasks.tasks.helper.recurrence

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.costular.atomtasks.core.util.getDelayUntil
import com.costular.atomtasks.tasks.worker.PopulateTasksWorker
import com.costular.atomtasks.tasks.worker.RecurrenceGenerationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

class RecurrenceScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun initialize() {
        val delay = getDelayUntil(LocalTime.parse(TIME_FOR_POPULATE_WORKER))

        val worker = PeriodicWorkRequestBuilder<PopulateTasksWorker>(
            repeatInterval = Duration.ofHours(RepetitionHours),
            flexTimeInterval = Duration.ofMinutes(FlexIntervalMinutes),
        )
            .setInitialDelay(delay)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "populate_recurring_tasks",
                ExistingPeriodicWorkPolicy.UPDATE,
                worker,
            )
    }

    fun scheduleTaskRecurrence(taskId: Long) {
        val worker = OneTimeWorkRequestBuilder<RecurrenceGenerationWorker>()
            .setInputData(workDataOf("task_id" to taskId))
            .build()

        WorkManager.getInstance(context).enqueue(worker)
    }

    companion object {
        private const val TIME_FOR_POPULATE_WORKER = "00:05"
        private const val RepetitionHours = 24L
        private const val FlexIntervalMinutes = 15L
    }
}
