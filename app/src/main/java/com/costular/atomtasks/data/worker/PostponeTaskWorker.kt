package com.costular.atomtasks.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.costular.atomtasks.domain.interactor.GetTaskByIdInteractor
import com.costular.atomtasks.domain.interactor.UpdateTaskReminderInteractor
import com.costular.atomtasks.domain.manager.NotifManager
import com.costular.atomtasks.domain.manager.ReminderManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalTime

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

            if (task.reminder == null || (!task.reminder.isEnabled)) {
                throw IllegalStateException("Task has no active reminder")
            }

            val reminderTime = LocalTime.now().plusHours(1)

            updateTaskReminderInteractor(
                UpdateTaskReminderInteractor.Params(
                    taskId,
                    reminderTime
                )
            )
            reminderManager.set(task.id, reminderTime.atDate(LocalDate.now()))
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

}