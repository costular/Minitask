package com.costular.atomtasks.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.data.tasks.TasksDao
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class TaskDatabaseTest {

    private lateinit var db: MinitaskDatabase
    private lateinit var tasksDao: TasksDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            MinitaskDatabase::class.java,
        ).build()
        tasksDao = db.getTasksDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAddTask() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = true,
        )

        tasksDao.addTask(task)

        tasksDao.getAllTasks().test {
            val item = awaitItem()
            assertThat(item.first().task.name).isEqualTo("whatever")
            assertThat(item.size).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testAddTwoTasks() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = true,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = true,
        )

        tasksDao.createTask(task)
        tasksDao.createTask(task2)

        val result = tasksDao.getAllTasks().first()
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun testTaskPosition() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 2,
        )

        tasksDao.addTask(task2)
        tasksDao.addTask(task1)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.first().task.position).isEqualTo(1)
        assertThat(result.last().task.position).isEqualTo(2)
    }

    @Test
    fun testTaskUpdatePosition() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 2,
        )

        val task1Id = tasksDao.addTask(task1)
        val task2Id = tasksDao.addTask(task2)
        tasksDao.updateTaskPosition(task1Id, 3)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.last().task.id).isEqualTo(task1Id)
    }

    @Test
    fun testGetMaxPositionForDate() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            position = 0,
        )

        tasksDao.createTask(task1)
        tasksDao.createTask(task2)
        val result = tasksDao.getMaxPositionForDate(LocalDate.now())

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun testMoveDownTaskPosition() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "1st",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "2nd",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task3 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "3rd",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )

        val id1 = tasksDao.createTask(task1)
        val id2 = tasksDao.createTask(task2)
        val id3 = tasksDao.createTask(task3)
        tasksDao.moveTask(LocalDate.now(), 1, 3)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.find { it.task.id == id2 }!!.task.position).isEqualTo(1)
        assertThat(result.find { it.task.id == id3 }!!.task.position).isEqualTo(2)
        assertThat(result.find { it.task.id == id1 }!!.task.position).isEqualTo(3)
    }

    @Test
    fun testMoveDownTaskPositionWithDifferentDays() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            position = 0,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            position = 0,
        )
        val task3 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            position = 0,
        )
        val task11 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task12 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )

        val id1 = tasksDao.createTask(task1)
        val id2 = tasksDao.createTask(task2)
        val id11 = tasksDao.createTask(task11)
        val id12 = tasksDao.createTask(task12)
        val id3 = tasksDao.createTask(task3)
        tasksDao.moveTask(LocalDate.now(), 1, 2)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.find { it.task.id == id1 }!!.task.position).isEqualTo(1)
        assertThat(result.find { it.task.id == id2 }!!.task.position).isEqualTo(2)
        assertThat(result.find { it.task.id == id3 }!!.task.position).isEqualTo(3)
        assertThat(result.find { it.task.id == id12 }!!.task.position).isEqualTo(1)
        assertThat(result.find { it.task.id == id11 }!!.task.position).isEqualTo(2)
    }

    @Test
    fun testMoveUpTaskPosition() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )
        val task3 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 0,
        )

        val id1 = tasksDao.createTask(task1)
        val id2 = tasksDao.createTask(task2)
        val id3 = tasksDao.createTask(task3)
        tasksDao.moveTask(LocalDate.now(), 3, 1)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.find { it.task.id == id3 }!!.task.position).isEqualTo(1)
        assertThat(result.find { it.task.id == id1 }!!.task.position).isEqualTo(2)
        assertThat(result.find { it.task.id == id2 }!!.task.position).isEqualTo(3)
    }

    @Test
    fun testMoveTaskToAClosePosition() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 2,
        )

        val id1 = tasksDao.createTask(task1)
        val id2 = tasksDao.createTask(task2)

        tasksDao.moveTask(LocalDate.now(), 2, 1)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.find { it.task.id == id1 }!!.task.position).isEqualTo(2)
        assertThat(result.find { it.task.id == id2 }!!.task.position).isEqualTo(1)
    }

    @Test
    fun testDoneTasksCount() = runTest {
        val task1 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )
        val task2 = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 2,
        )

        val id1 = tasksDao.createTask(task1)
        val id2 = tasksDao.createTask(task2)

        tasksDao.updateTaskDone(id1, true)
        tasksDao.updateTaskDone(id2, true)

        val result = tasksDao.getDoneTasksCount()

        assertThat(result).isEqualTo(2)
    }

    @Test
    fun testAddTaskWithRecurrence() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
            isRecurring = true,
            recurrenceType = "daily"
        )

        val id = tasksDao.createTask(task)
        val taskAggregated = tasksDao.getTaskById(id).first()

        assertThat(taskAggregated.task.recurrenceType).isEqualTo("daily")
    }

    @Test
    fun testRemoveAllRecurringTasksByChild() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Parent",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
            isRecurring = true,
            recurrenceType = "daily"
        )
        val parentTaskId = tasksDao.addTask(task)

        val childTask = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Child1",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
            position = 2,
        )
        val secondChildTask = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Child2",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
            position = 3,
        )
        val childId = tasksDao.addTask(childTask)
        val secondChildId = tasksDao.addTask(secondChildTask)

        tasksDao.removeAllOccurrences(id = childId)

        val result = tasksDao.getAllTasks().first()

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun testRemoveAllRecurringTasksByParent() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Parent",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
            isRecurring = true,
            recurrenceType = "daily"
        )
        val parentTaskId = tasksDao.addTask(task)

        val recurringTask = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Child1",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
            position = 2,
        )
        val secondRecurringTask = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Child2",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
            position = 3,
        )

        val childId = tasksDao.addTask(recurringTask)
        val secondChildId = tasksDao.addTask(secondRecurringTask)

        tasksDao.removeAllOccurrences(id = parentTaskId)
        val result = tasksDao.getAllTasks().first()

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun testRemoveFutureRecurringTasks() = runTest {
        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Parent",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
            isRecurring = true,
            recurrenceType = "daily"
        )
        val parentTaskId = tasksDao.addTask(task)

        val child = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Recurrent Task",
            day = LocalDate.now(),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
        )
        val secondChild = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Recurrent Task",
            day = LocalDate.now().plusDays(1),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
        )
        val thirdChild = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Recurrent Task",
            day = LocalDate.now().plusDays(2),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
        )
        val fourthChild = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "Recurrent Task",
            day = LocalDate.now().plusDays(2),
            isDone = false,
            isRecurring = true,
            recurrenceType = "daily",
            parentId = parentTaskId,
        )

        val childId = tasksDao.addTask(child)
        val secondChildId = tasksDao.addTask(secondChild)
        val thirdChildId = tasksDao.addTask(thirdChild)
        val fourthChildId = tasksDao.addTask(fourthChild)

        tasksDao.removeFutureOccurrencesAndSelf(id = secondChildId)

        val result = tasksDao.getAllTasks().first()
        assertThat(result.size).isEqualTo(2)
        assertThat(result.find { it.task.id == secondChildId }).isNull()
        assertThat(result.find { it.task.id == thirdChildId }).isNull()
        assertThat(result.find { it.task.id == fourthChildId }).isNull()
    }
}
