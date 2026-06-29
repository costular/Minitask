package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.core.toError
import com.costular.atomtasks.core.usecase.UseCase
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.RecurringUpdateStrategy
import com.costular.atomtasks.tasks.model.UpdateTaskUseCaseError
import com.costular.atomtasks.tasks.model.asString
import com.costular.atomtasks.tasks.model.toEntity
import com.costular.atomtasks.tasks.repository.TasksRepository
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class EditTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val taskReminderManager: TaskReminderManager,
    private val populateRecurringTasksUseCase: PopulateRecurringTasksUseCase,
    private val taskNotificationManager: TaskNotificationManager,
) : UseCase<EditTaskUseCase.Params, Either<UpdateTaskUseCaseError, Unit>> {

    data class Params(
        val taskId: Long,
        val name: String,
        val date: LocalDate,
        val reminderTime: LocalTime?,
        val recurrenceType: RecurrenceType?,
        val recurringUpdateStrategy: RecurringUpdateStrategy?,
    )

    @Suppress("ReturnCount")
    override suspend fun invoke(params: Params): Either<UpdateTaskUseCaseError, Unit> {
        return try {
            with(params) {
                if (name.isEmpty()) {
                    return UpdateTaskUseCaseError.NameCannotBeEmpty.toError()
                }

                val task = tasksRepository.getTaskById(taskId).firstOrNull()

                if (task == null) {
                    return UpdateTaskUseCaseError.TaskNotFound.toError()
                }

                tasksRepository.runOrRollback {
                    val oldDay = task.day
                    val newPosition = if (oldDay != date) {
                        tasksRepository.getMaxPositionForDate(date) + 1
                    } else {
                        task.position
                    }

                    val taskToUpdate = task.toEntity().copy(
                        name = name,
                        day = date,
                        recurrenceType = recurrenceType?.asString(),
                        position = newPosition,
                    )

                    val wasRecurring = task.isRecurring

                    tasksRepository.updateTask(taskToUpdate)
                    updateReminder(reminderTime)

                    if (wasRecurring && recurringUpdateStrategy != null) {
                        when (recurringUpdateStrategy) {
                            RecurringUpdateStrategy.SINGLE -> {
                                // do nothing, it got updated already actually
                            }

                            RecurringUpdateStrategy.SINGLE_AND_FUTURE -> {
                                // Remove future tasks to re-create them with the new data
                                tasksRepository.removeTask(
                                    taskId = taskId,
                                    recurringRemovalStrategy = RecurringRemovalStrategy.FUTURE_ONES
                                )
                                populateRecurringTasksUseCase(
                                    PopulateRecurringTasksUseCase.Params(
                                        taskId = taskId
                                    )
                                )
                            }
                        }
                    }
                }
                taskNotificationManager.removeTaskNotification(taskId)
                Either.Result(Unit)
            }
        } catch (sql: SQLException) {
            atomLog { sql }
            Either.Error(UpdateTaskUseCaseError.UnableToSave)
        } catch (e: Exception) {
            atomLog { e }
            Either.Error(UpdateTaskUseCaseError.UnknownError)
        }
    }

    private suspend fun Params.updateReminder(
        reminderTime: LocalTime?
    ) {
        if (reminderTime != null) {
            tasksRepository.updateTaskReminder(taskId, reminderTime, date)
            taskReminderManager.set(taskId, reminderTime.atDate(date))
        } else {
            tasksRepository.removeReminder(taskId)
            taskReminderManager.cancel(taskId)
        }
    }
}
