package com.costular.atomreminders.data.tasks

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskLocalDataSource {

    suspend fun createTask(taskEntity: TaskEntity): Long
    suspend fun createReminderForTask(reminderEntity: ReminderEntity)
    fun getTasks(day: LocalDate? = null): Flow<List<TaskAggregated>>
    fun getTaskById(id: Long): Flow<TaskAggregated>
    suspend fun removeTask(taskId: Long)

}