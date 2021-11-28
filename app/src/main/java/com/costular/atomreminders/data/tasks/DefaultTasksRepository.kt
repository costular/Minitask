package com.costular.atomreminders.data.tasks

import com.costular.atomreminders.data.toDomain
import com.costular.atomreminders.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

class DefaultTasksRepository(
    private val localDataSource: TaskLocalDataSource
) : TasksRepository {

    override suspend fun createTask(
        name: String,
        date: LocalDate,
        reminderEnabled: Boolean,
        reminderTime: LocalTime?
    ) {
        val habitEntity = TaskEntity(
            0,
            LocalDate.now(),
            name,
            date,
            false,
        )
        val taskId = localDataSource.createTask(habitEntity)

        if (reminderEnabled) {
            val reminder = ReminderEntity(
                0L,
                requireNotNull(reminderTime),
                date,
                reminderEnabled,
                taskId
            )
            localDataSource.createReminderForTask(reminder)
        }
    }

    override fun getTaskById(id: Long): Flow<Task> {
        return localDataSource.getTaskById(id).map { it.toDomain() }
    }

    override fun getTasks(day: LocalDate?): Flow<List<Task>> {
        return localDataSource.getTasks(day).map { habits -> habits.map { it.toDomain() } }
    }

    override suspend fun removeTask(taskId: Long) {
        localDataSource.removeTask(taskId)
    }
}