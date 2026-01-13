package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.core.usecase.UseCase
import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.model.CreateTaskError
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.repository.TasksRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val taskReminderManager: TaskReminderManager,
    private val populateRecurringTasksUseCase: PopulateRecurringTasksUseCase,
    private val settingsRepository: SettingsRepository,
) : UseCase<CreateTaskUseCase.Params, Either<CreateTaskError, Unit>> {

    data class Params(
        val name: String,
        val date: LocalDate,
        val reminderEnabled: Boolean,
        val reminderTime: LocalTime?,
        val recurrenceType: RecurrenceType?,
    )

    override suspend fun invoke(params: Params): Either<CreateTaskError, Unit> {
        return try {
            val taskId = tasksRepository.createTask(
                name = params.name,
                date = params.date,
                reminderEnabled = params.reminderEnabled,
                reminderTime = params.reminderTime,
                recurrenceType = params.recurrenceType,
                parentId = null,
            )
            if (params.reminderEnabled && params.reminderTime != null) {
                taskReminderManager.set(taskId, params.reminderTime.atDate(params.date))
            }

            populateRecurringTasksUseCase(PopulateRecurringTasksUseCase.Params(taskId)) // TODO handle error handling

            settingsRepository.setHasUserCreatedTask(true)

            Either.Result(Unit)
        } catch (e: Exception) {
            atomLog { e }
            Either.Error(CreateTaskError.UnknownError)
        }
    }
}
