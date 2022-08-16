package com.costular.atomtasks.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.costular.atomtasks.data.tasks.GetTaskByIdInteractor
import com.costular.atomtasks.data.tasks.UpdateTaskReminderInteractor
import com.costular.atomtasks.data.manager.NotifManager
import com.costular.atomtasks.data.manager.ReminderManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import timber.log.Timber

@Suppress("TooGenericExceptionCaught")
@HiltWorker
class PostponeTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getTaskByIdInteractor: GetTaskByIdInteractor,
    private val updateTaskReminderInteractor: UpdateTaskReminderInteractor,
    private val notifManager: NotifManager,
    private val reminderManager: ReminderManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1L)

        return try {
            if (taskId == -1L) {
                throw IllegalArgumentException("Task id has not been passed")
            }
            notifManager.removeTaskNotification(taskId)

            getTaskByIdInteractor(GetTaskByIdInteractor.Params(taskId))
            val task = getTaskByIdInteractor.observe().first()

            if (task.reminder == null || (task.reminder?.isEnabled == false)) {
                throw IllegalStateException("Task has no active reminder")
            }

            val reminderTime = LocalTime.now().plusHours(1)

            updateTaskReminderInteractor(
                UpdateTaskReminderInteractor.Params(
                    taskId,
                    reminderTime,
                ),
            )
            reminderManager.set(task.id, reminderTime.atDate(LocalDate.now()))
            Result.success()
        } catch (e: Exception) {
            Timber.d(e)
            Result.failure()
        }
    }
}