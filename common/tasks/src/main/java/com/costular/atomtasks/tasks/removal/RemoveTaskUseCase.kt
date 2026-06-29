package com.costular.atomtasks.tasks.removal

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.core.usecase.UseCase
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.model.RemoveTaskError
import com.costular.atomtasks.tasks.repository.TasksRepository
import javax.inject.Inject

class RemoveTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val taskReminderManager: TaskReminderManager,
    private val taskNotificationManager: TaskNotificationManager,
) : UseCase<RemoveTaskUseCase.Params, Either<RemoveTaskError, Unit>> {

    data class Params(
        val taskId: Long,
        val strategy: RecurringRemovalStrategy? = null,
    )

    override suspend fun invoke(params: Params): Either<RemoveTaskError, Unit> {
        return try {
            tasksRepository.removeTask(params.taskId, params.strategy)
            taskReminderManager.cancel(params.taskId)
            taskNotificationManager.removeTaskNotification(params.taskId)
            Either.Result(Unit)
        } catch (e: Exception) {
            atomLog { e }
            Either.Error(RemoveTaskError.UnknownError)
        }
    }
}
