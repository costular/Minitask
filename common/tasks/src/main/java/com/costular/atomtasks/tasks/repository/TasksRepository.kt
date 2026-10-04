package com.costular.atomtasks.tasks.repository

import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Suppress("TooManyFunctions")
interface TasksRepository {
    suspend fun createTask(
        name: String,
        date: LocalDate,
        reminderEnabled: Boolean,
        reminderTime: LocalTime?,
        recurrenceType: RecurrenceType?,
        parentId: Long?,
    ): Long
    suspend fun getTaskCount(): Int
    fun getTaskById(id: Long): Flow<Task?>
    fun getTasks(day: LocalDate? = null): Flow<List<Task>>
    fun observeTasksInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>
    fun observeSearchTasks(query: String): Flow<List<Task>>
    suspend fun removeTask(taskId: Long, recurringRemovalStrategy: RecurringRemovalStrategy?)
    suspend fun markTask(taskId: Long, isDone: Boolean)
    suspend fun updateTaskReminder(taskId: Long, reminderTime: LocalTime, reminderDate: LocalDate)
    suspend fun removeReminder(taskId: Long)
    suspend fun updateTask(taskEntity: TaskEntity)
    suspend fun numberFutureOccurrences(parentId: Long, from: LocalDate): Int
    suspend fun moveTask(day: LocalDate, fromPosition: Int, toPosition: Int)
    suspend fun getMaxPositionForDate(localDate: LocalDate): Int
    suspend fun runOrRollback(body: suspend () -> Unit)
}
