package com.costular.atomtasks.tasks.repository

import com.costular.atomtasks.core.net.DispatcherProvider
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.model.asString
import com.costular.atomtasks.tasks.model.toDomain
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class DefaultTasksRepository @Inject constructor(
    private val localDataSource: TaskLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : TasksRepository {

    override suspend fun createTask(
        name: String,
        date: LocalDate,
        reminderEnabled: Boolean,
        reminderTime: LocalTime?,
        recurrenceType: RecurrenceType?,
        parentId: Long?,
    ): Long {
        val taskEntity = TaskEntity(
            id = 0,
            createdAt = LocalDate.now(),
            name = name,
            day = date,
            isDone = false,
            recurrenceType = recurrenceType?.asString(),
            isRecurring = recurrenceType != null,
            parentId = parentId,
        )
        val taskId = localDataSource.createTask(taskEntity)

        if (reminderEnabled) {
            localDataSource.createReminderForTask(
                time = requireNotNull(reminderTime),
                date = date,
                reminderEnabled = reminderEnabled,
                taskId = taskId,
            )
        }

        return taskId
    }

    override suspend fun getTaskCount(): Int {
        return localDataSource.getTasksCount()
    }

    override fun observeSearchTasks(query: String): Flow<List<Task>> =
        localDataSource.observeSearchTasks(query).mapLatest { tasks ->
            withContext(dispatchers.computation) {
                tasks.map {
                    currentCoroutineContext().ensureActive()
                    it.toDomain()
                }
            }
        }

    override fun getTaskById(id: Long): Flow<Task?> {
        return localDataSource.getTaskById(id)
            .map { it?.toDomain() }
            .flowOn(dispatchers.computation)
    }

    override fun getTasks(day: LocalDate?): Flow<List<Task>> {
        return localDataSource.getTasks(day)
            .map { tasks -> tasks.map { it.toDomain() } }
            .flowOn(dispatchers.computation)
    }

    override suspend fun removeTask(
        taskId: Long,
        recurringRemovalStrategy: RecurringRemovalStrategy?
    ) {
        localDataSource.removeTask(taskId, recurringRemovalStrategy)
    }

    override suspend fun markTask(taskId: Long, isDone: Boolean) {
        localDataSource.markTask(taskId, isDone)
    }

    override suspend fun updateTaskReminder(
        taskId: Long,
        reminderTime: LocalTime,
        reminderDate: LocalDate,
    ) {
        localDataSource.updateTaskReminder(taskId, reminderTime, reminderDate)
    }

    override suspend fun removeReminder(taskId: Long) {
        localDataSource.removeReminder(taskId)
    }

    override suspend fun updateTask(taskEntity: TaskEntity) {
        localDataSource.updateTask(taskEntity)
    }

    override suspend fun numberFutureOccurrences(parentId: Long, from: LocalDate): Int {
        return localDataSource.numberFutureOccurrences(parentId, from)
    }

    override suspend fun moveTask(day: LocalDate, fromPosition: Int, toPosition: Int) {
        localDataSource.moveTask(day, fromPosition, toPosition)
    }

    override suspend fun getMaxPositionForDate(date: LocalDate): Int {
        return localDataSource.getMaxPositionForDate(date)
    }

    override suspend fun runOrRollback(body: suspend () -> Unit) {
        localDataSource.runAsTransaction(body)
    }
}
