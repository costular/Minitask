package com.costular.atomtasks.tasks.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

@Suppress("TooGenericExceptionCaught", "SwallowedException")
@HiltWorker
class RestoreMissedTaskRemindersWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val taskNotificationManager: TaskNotificationManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        observeTasksUseCase(ObserveTasksUseCase.Params(day = today))
            .first()
            .tap { tasks ->
                tasks
                    .asSequence()
                    .filter { task ->
                        val reminder = task.reminder
                        reminder != null &&
                            !task.isDone &&
                            reminder.date == today &&
                            !reminder.localDateTime.isAfter(now)
                    }
                    .sortedBy { task -> task.reminder?.localDateTime }
                    .forEach { task ->
                        taskNotificationManager.remindTask(task.id, task.name)
                    }
            }

        Result.success()
    } catch (e: Exception) {
        atomLog { e }
        Result.failure()
    }

    companion object {
        fun start() = OneTimeWorkRequestBuilder<RestoreMissedTaskRemindersWorker>().build()
    }
}
