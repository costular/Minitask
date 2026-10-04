package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.data.tasks.ReminderEntity
import com.costular.atomtasks.data.tasks.TaskAggregated
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.repository.DefaultTasksRepository
import com.costular.atomtasks.tasks.repository.TaskLocalDataSource
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultTasksRepositoryTest {

    private lateinit var sut: TasksRepository

    private val localDataSource: TaskLocalDataSource = mockk(relaxed = true)

    @Before
    fun setUp() {
        sut = DefaultTasksRepository(localDataSource)
    }

    @Test
    fun `missing task emits null rather than leaving callers waiting`() = runTest {
        coEvery { localDataSource.getTaskById(47L) } returns flowOf(null)
        assertThat(sut.getTaskById(47L).first()).isNull()
    }

    @Test
    fun `should call local data source add task when add task`() = runTest {
        val taskName = "task name"
        val taskDate = LocalDate.now()

        val taskSlot = slot<TaskEntity>()

        sut.createTask(
            name = taskName,
            date = taskDate,
            reminderEnabled = false,
            reminderTime = null,
            recurrenceType = null,
            parentId = null,
        )

        coVerify { localDataSource.createTask(capture(taskSlot)) }
        assertThat(taskSlot.captured.name).isEqualTo(taskName)
    }

    @Test
    fun `should call local data source add reminder when add task with reminder`() = runTest {
        val taskName = "task name"
        val taskDate = LocalDate.of(2022, 6, 4)
        val taskReminderTime = LocalTime.of(9, 0)
        val reminderEnabled = true
        val parentId = 200L

        val taskId = sut.createTask(
            name = taskName,
            date = taskDate,
            reminderEnabled = reminderEnabled,
            reminderTime = taskReminderTime,
            recurrenceType = null,
            parentId = null,
        )

        coVerify {
            localDataSource.createReminderForTask(
                taskReminderTime,
                taskDate,
                reminderEnabled,
                taskId,
            )
        }
    }

    @Test
    fun `should get task by id`() = runTest {
        val taskId = 101L
        val taskName = "just whatever"
        val createdAt = LocalDate.now()
        val taskDay = LocalDate.now().plusDays(1)
        val taskReminder = LocalTime.now()
        val taskIsDone = true

        val taskResult = TaskEntity(
            id = taskId,
            createdAt = createdAt,
            name = taskName,
            day = taskDay,
            isDone = taskIsDone,
        )
        val result = TaskAggregated(
            task = taskResult,
            reminder = ReminderEntity(
                reminderId = 10L,
                time = taskReminder,
                date = taskDay,
                taskId = taskId,
            ),
        )
        val expected = Task(
            id = taskId,
            name = taskName,
            createdAt = createdAt,
            day = taskDay,
            reminder = Reminder(
                id = 100L,
                time = taskReminder,
                date = LocalDate.now(),
            ),
            isDone = true,
            position = 1,
            isRecurring = false,
            recurrenceEndDate = null,
            recurrenceType = null,
            parentId = null,
        )

        coEvery { localDataSource.getTaskById(taskId) } returns flowOf(result)

        val actual = sut.getTaskById(taskId).first()

        assertThat(actual?.name).isEqualTo(expected.name)
        assertThat(actual?.createdAt).isEqualTo(expected.createdAt)
        assertThat(actual?.day).isEqualTo(expected.day)
        assertThat(actual?.isDone).isEqualTo(expected.isDone)
        assertThat(actual?.reminder?.time).isEqualTo(expected.reminder?.time)
    }

    @Test
    fun `should call local data source update task when update task`() = runTest {
        val taskId = 10L
        val taskName = "Task name"
        val taskDay = LocalDate.of(2022, 6, 4)

        val task = TaskEntity(
            id = taskId,
            createdAt = LocalDate.now(),
            name = taskName,
            day = taskDay,
            isDone = true,
            position = 2,
            isRecurring = true,
            recurrenceType = "daily",
            recurrenceEndDate = LocalDate.now().plusDays(1),
            parentId = 1,
        )

        sut.updateTask(task)

        coVerify { localDataSource.updateTask(task) }
    }

    @Test
    fun `should call local data source move task when move task`() = runTest {
        val from = 1
        val to = 3

        sut.moveTask(LocalDate.now(), from, to)

        coVerify(exactly = 1) { localDataSource.moveTask(LocalDate.now(), from, to) }
    }

    @Test
    fun `Should call local data source when call number future occurrences`() = runTest {
        val parentId = 10L
        val day = LocalDate.now()
        val expectedCount = 10

        coEvery { localDataSource.numberFutureOccurrences(parentId, day) } returns expectedCount

        val result = sut.numberFutureOccurrences(parentId, day)

        assertThat(result).isEqualTo(expectedCount)
    }
}
