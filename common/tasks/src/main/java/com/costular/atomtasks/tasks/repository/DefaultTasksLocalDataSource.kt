package com.costular.atomtasks.tasks.repository

import com.costular.atomtasks.core.net.DispatcherProvider
import com.costular.atomtasks.data.database.TransactionRunner
import com.costular.atomtasks.data.tasks.ReminderDao
import com.costular.atomtasks.data.tasks.ReminderEntity
import com.costular.atomtasks.data.tasks.TaskAggregated
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.data.tasks.TasksDao
import com.costular.atomtasks.data.tasks.taskNameSearchPattern
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Suppress("TooManyFunctions")
internal class DefaultTasksLocalDataSource @Inject constructor(
    private val tasksDao: TasksDao,
    private val reminderDao: ReminderDao,
    private val transactionRunner: TransactionRunner,
    private val dispatchers: DispatcherProvider,
) : TaskLocalDataSource {

    override suspend fun createTask(taskEntity: TaskEntity): Long {
        return tasksDao.createTask(taskEntity)
    }

    override suspend fun createReminderForTask(
        time: LocalTime,
        date: LocalDate,
        reminderEnabled: Boolean,
        taskId: Long,
    ) {
        val reminder = ReminderEntity(
            reminderId = 0L,
            time = time,
            date = date,
            taskId = taskId,
        )
        reminderDao.insertReminder(reminder)
    }

    override suspend fun getTasksCount(): Int {
        return tasksDao.getTaskCount()
    }

    override fun getTasks(day: LocalDate?): Flow<List<TaskAggregated>> {
        return if (day != null) {
            tasksDao.getAllTasksForDate(day).distinctUntilChanged()
        } else {
            tasksDao.getAllTasks().distinctUntilChanged()
        }
    }

    override fun observeSearchTasks(query: String): Flow<List<TaskAggregated>> = flow {
        emitAll(tasksDao.observeSearchTasks(taskNameSearchPattern(query)))
    }.distinctUntilChanged().flowOn(dispatchers.computation)

    override fun getTaskById(id: Long): Flow<TaskAggregated?> {
        return tasksDao.getTaskById(id).distinctUntilChanged()
    }

    override suspend fun removeTask(
        taskId: Long,
        recurringRemovalStrategy: RecurringRemovalStrategy?
    ) {
        if (recurringRemovalStrategy == null) {
            tasksDao.removeTaskById(taskId)
        } else {
            when (recurringRemovalStrategy) {
                RecurringRemovalStrategy.SINGLE -> {
                    tasksDao.removeTaskById(taskId)
                }

                RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES -> {
                    tasksDao.removeFutureOccurrencesAndSelf(id = taskId)
                }

                RecurringRemovalStrategy.FUTURE_ONES -> {
                    tasksDao.removeFutureOccurrences(id = taskId)
                }

                RecurringRemovalStrategy.ALL -> {
                    tasksDao.removeAllOccurrences(id = taskId)
                }
            }
        }
    }

    override suspend fun markTask(taskId: Long, isDone: Boolean) {
        tasksDao.updateTaskDone(taskId, isDone)
    }

    override suspend fun updateTaskReminder(taskId: Long, time: LocalTime, date: LocalDate) {
        if (reminderDao.reminderExistForTask(taskId)) {
            reminderDao.updateReminder(taskId, date, time)
        } else {
            createReminderForTask(time, date, true, taskId)
        }
    }

    override suspend fun removeReminder(taskId: Long) {
        reminderDao.removeReminder(taskId)
    }

    override suspend fun updateTask(taskEntity: TaskEntity) {
        tasksDao.update(taskEntity)
    }

    override suspend fun numberFutureOccurrences(parentId: Long, from: LocalDate): Int {
        return tasksDao.countFutureOccurrences(parentId, from)
    }

    override suspend fun getMaxPositionForDate(date: LocalDate): Int {
        return tasksDao.getMaxPositionForDate(date)
    }

    override suspend fun moveTask(day: LocalDate, fromPosition: Int, toPosition: Int) {
        tasksDao.moveTask(day, fromPosition, toPosition)
    }

    override suspend fun runAsTransaction(block: suspend () -> Unit) {
        transactionRunner.runAsTransaction(block)
    }
}
