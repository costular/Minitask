package com.costular.atomtasks.tasks.helper.recurrence

import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceLookAhead.numberOfOccurrencesForType
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.costular.atomtasks.tasks.usecase.PopulateRecurringTasksUseCase
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class RecurrenceManagerImpl @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val populateRecurringTasksUseCase: PopulateRecurringTasksUseCase,
) : RecurrenceManager {

    override suspend fun createAheadTasks(date: LocalDate) {
        tasksRepository.getTasks(day = date).first()
            .filter { it.isRecurring && it.recurrenceType != null }
            .forEach { task ->
                createAheadForTask(task.id)
            }
    }

    override suspend fun createAheadForTask(taskId: Long) {
        tasksRepository.getTaskById(taskId).firstOrNull()
            ?.takeIf { it.isRecurring && it.recurrenceType != null }
            ?.let { task ->
                val effectiveParentId = task.parentId ?: task.id
                val aheadTasksCountForType = numberOfOccurrencesForType(task.recurrenceType!!)
                val futureOccurrencesFromNow = tasksRepository.numberFutureOccurrences(
                    effectiveParentId,
                    LocalDate.now(),
                )

                if (futureOccurrencesFromNow < aheadTasksCountForType) {
                    val futureOccurrencesFromTask = tasksRepository.numberFutureOccurrences(
                        effectiveParentId,
                        task.day
                    )
                    PopulateRecurringTasksUseCase.Params(
                        taskId = task.id,
                        drop = futureOccurrencesFromTask,
                    )
                } else {
                    null
                }
            }
            ?.let { params ->
                populateRecurringTasksUseCase(params)
            }
    }
}
