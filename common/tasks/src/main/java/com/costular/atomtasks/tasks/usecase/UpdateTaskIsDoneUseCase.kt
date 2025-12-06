package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.core.usecase.UseCase
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.model.UpdateTaskIsDoneError
import com.costular.atomtasks.tasks.repository.TasksRepository
import javax.inject.Inject

class UpdateTaskIsDoneUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val recurrenceScheduler: RecurrenceScheduler,
) : UseCase<UpdateTaskIsDoneUseCase.Params, Either<UpdateTaskIsDoneError, Unit>> {

    data class Params(
        val taskId: Long,
        val isDone: Boolean,
    )

    override suspend fun invoke(params: Params): Either<UpdateTaskIsDoneError, Unit> {
        return try {
            tasksRepository.markTask(params.taskId, params.isDone)

            if (params.isDone) {
                recurrenceScheduler.scheduleTaskRecurrence(params.taskId)
            }
            Either.Result(Unit)
        } catch (e: Exception) {
            atomLog { e }
            Either.Error(UpdateTaskIsDoneError.UnknownError)
        }
    }
}
