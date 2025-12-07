package com.costular.atomtasks.tasks.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurrenceGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurrenceManager: RecurrenceManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1L)

        return try {
            require(taskId == -1L) {
                "Task id has not been passed"
            }

            recurrenceManager.createAheadForTask(taskId)
            Result.success()
        } catch (e: Exception) {
            atomLog { e }
            Result.failure()
        }
    }
}
