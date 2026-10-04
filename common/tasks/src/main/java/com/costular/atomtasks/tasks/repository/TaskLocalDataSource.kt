package com.costular.atomtasks.tasks.repository

import com.costular.atomtasks.data.tasks.TaskAggregated
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface TaskLocalDataSource {
    suspend fun createTask(taskEntity: TaskEntity): Long
    suspend fun createReminderForTask(
        time: LocalTime,
        date: LocalDate,
        reminderEnabled: Boolean,
        taskId: Long,
    )
    suspend fun getTasksCount(): Int
    fun getTasks(day: LocalDate? = null): Flow<List<TaskAggregated>>
    fun observeSearchTasks(query: String): Flow<List<TaskAggregated>>
    fun getTaskById(id: Long): Flow<TaskAggregated?>
    suspend fun removeTask(taskId: Long, recurringRemovalStrategy: RecurringRemovalStrategy?)
    suspend fun markTask(taskId: Long, isDone: Boolean)
    suspend fun updateTaskReminder(taskId: Long, time: LocalTime, date: LocalDate)
    suspend fun removeReminder(taskId: Long)
    suspend fun updateTask(taskEntity: TaskEntity)
    suspend fun numberFutureOccurrences(parentId: Long, from: LocalDate): Int
    suspend fun getMaxPositionForDate(date: LocalDate): Int
    suspend fun moveTask(day: LocalDate, fromPosition: Int, toPosition: Int)

    suspend fun runAsTransaction(block: suspend () -> Unit)
}
