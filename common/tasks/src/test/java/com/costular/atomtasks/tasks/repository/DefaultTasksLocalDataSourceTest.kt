package com.costular.atomtasks.tasks.repository

import com.costular.atomtasks.data.database.TransactionRunner
import com.costular.atomtasks.data.tasks.ReminderDao
import com.costular.atomtasks.data.tasks.TaskAggregated
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.data.tasks.TasksDao
import com.costular.atomtasks.data.tasks.taskNameSearchPattern
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.model.asString
import com.costular.atomtasks.core.testing.net.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTasksLocalDataSourceTest {

    lateinit var sut: TaskLocalDataSource

    private val tasksDao: TasksDao = mockk(relaxed = true)
    private val reminderDao: ReminderDao = mockk(relaxUnitFun = true)
    private val transactionRunner: TransactionRunner = mockk(relaxed = true)

    @Before
    fun setUp() {
        sut = DefaultTasksLocalDataSource(
            tasksDao = tasksDao,
            reminderDao = reminderDao,
            transactionRunner = transactionRunner,
            dispatchers = TestDispatcherProvider(UnconfinedTestDispatcher()),
        )
    }

    @Test
    fun `Should call task dao accordingly when update task`() = runTest {
        val id = 1121L

        val taskAggregated = TaskAggregated(
            task = TaskEntity(
                id = id,
                name = "Pierre Jimenez",
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                isDone = false,
                position = 1,
                isRecurring = false,
                recurrenceType = null,
                recurrenceEndDate = null,
                parentId = null,
            ),
            reminder = null,
        )
        coEvery { tasksDao.getTaskById(id) } returns flowOf(taskAggregated)

        val updatedTask = taskAggregated.task.copy(
            name = "Whatever",
            recurrenceType = RecurrenceType.DAILY.asString(),
        )
        sut.updateTask(updatedTask)

        coVerify {
            tasksDao.update(updatedTask)
        }
    }

    @Test
    fun `Should call dao when count future occurrences for recurring task`() = runTest {
        val id = 10L
        val expectedCount = 5

        val taskAggregated = TaskAggregated(
            task = TaskEntity(
                id = id,
                name = "Pierre Jimenez",
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                isDone = false,
                position = 1,
                isRecurring = false,
                recurrenceType = null,
                recurrenceEndDate = null,
                parentId = null,
            ),
            reminder = null,
        )
        coEvery { tasksDao.getTaskById(id) } returns flowOf(taskAggregated)
        coEvery {
            tasksDao.countFutureOccurrences(10L, LocalDate.now())
        } returns expectedCount

        val result = sut.numberFutureOccurrences(10L, LocalDate.now())

        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `search encodes a literal query on the data dispatcher and never loads all tasks`() = runTest {
        val computation = StandardTestDispatcher(testScheduler, name = "data computation")
        val source = DefaultTasksLocalDataSource(tasksDao, reminderDao, transactionRunner,
            TestDispatcherProvider(computation))
        val query = "%_École*?[]"
        every { tasksDao.observeSearchTasks(taskNameSearchPattern(query)) } answers {
            kotlinx.coroutines.flow.flow {
                assertThat(currentCoroutineContext()[CoroutineDispatcher]).isSameInstanceAs(computation)
                emit(emptyList())
            }
        }
        assertThat(source.observeSearchTasks(query).first()).isEmpty()
        verify(exactly = 1) { tasksDao.observeSearchTasks(taskNameSearchPattern(query)) }
        verify(exactly = 0) { tasksDao.getAllTasks() }
    }
}
