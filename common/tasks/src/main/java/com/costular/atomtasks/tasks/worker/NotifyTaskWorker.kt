package com.costular.atomtasks.tasks.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@Suppress("TooGenericExceptionCaught", "SwallowedException")
@HiltWorker
class NotifyTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val taskNotificationManager: TaskNotificationManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1L)

        return try {
            if (taskId == -1L) {
                throw IllegalArgumentException("Task id has not been passed")
            }

            val task = checkNotNull(
                getTaskByIdUseCase(GetTaskByIdUseCase.Params(taskId)).firstOrNull()
            ) {
                "Task is null"
            }

            if (task.reminder == null) {
                throw IllegalStateException("Reminder is null")
            }

            if (!task.reminder.isToday) {
                throw IllegalStateException("Reminder is not valid")
            }

            if (task.isDone) {
                throw IllegalStateException(
                    "Reminder is done so does not makes sense to notify the reminder",
                )
            }

            taskNotificationManager.remindTask(
                task.id,
                task.name,
                task.reminder.localDateTime,
            )
            Result.success()
        } catch (e: Exception) {
            atomLog { e }
            Result.failure()
        }
    }
}
