package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.RecurringUpdateStrategy
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.model.UpdateTaskUseCaseError
import com.costular.atomtasks.tasks.model.asString
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EditTaskUseCaseTest {

    private lateinit var sut: EditTaskUseCase

    private val repository: TasksRepository = mockk(relaxUnitFun = true)
    private val taskReminderManager: TaskReminderManager = mockk(relaxed = true)
    private val populateRecurringTasksUseCase: PopulateRecurringTasksUseCase = mockk(relaxed = true)
    private val taskNotificationManager: TaskNotificationManager = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        sut = EditTaskUseCase(
            repository,
            taskReminderManager,
            populateRecurringTasksUseCase,
            taskNotificationManager,
        )

        coEvery {
            repository.runOrRollback(captureCoroutine<suspend () -> Unit>())
        } coAnswers {
            coroutine<suspend () -> Unit>().coInvoke()
        }

        coEvery { repository.getMaxPositionForDate(any()) } returns 1
    }

    @Test
    fun `should update task`() = runTest {
        val taskId = 10L
        val name = "New name"
        val newDay = LocalDate.now()
        val newRecurrence = RecurrenceType.WEEKLY

        val slot = slot<TaskEntity>()
        coEvery {
            repository.updateTask(capture(slot))
        } just runs

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = name,
                date = newDay,
                reminderTime = null,
                recurrenceType = newRecurrence,
                recurringUpdateStrategy = null,
            ),
        )

        Truth.assertThat(slot.captured.id).isEqualTo(taskId)
        Truth.assertThat(slot.captured.name).isEqualTo(name)
        Truth.assertThat(slot.captured.day).isEqualTo(newDay)
        Truth.assertThat(slot.captured.recurrenceType).isEqualTo(newRecurrence.asString())
    }

    @Test
    fun `should update reminder when is enabled not null`() = runTest {
        val taskId = 10L
        val name = "New name"
        val newDay = LocalDate.now()
        val reminder = LocalTime.of(9, 0)

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = name,
                date = newDay,
                reminderTime = reminder,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        coVerify { repository.updateTaskReminder(taskId, reminder, newDay) }
    }

    @Test
    fun `should set reminder when reminder is enabled and not null`() = runTest {
        val taskId = 10L
        val name = "New name"
        val newDay = LocalDate.now()
        val reminder = LocalTime.of(9, 0)

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = name,
                date = newDay,
                reminderTime = reminder,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        coVerify { taskReminderManager.set(taskId, reminder.atDate(newDay)) }
    }

    @Test
    fun `should remove reminder when reminder is null and not enabled`() = runTest {
        val taskId = 10L
        val name = "New name"
        val newDay = LocalDate.now()
        val reminder = null

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = name,
                date = newDay,
                reminderTime = reminder,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        coVerify { repository.removeReminder(taskId) }
    }

    @Test
    fun `should set reminder when reminder is not null`() = runTest {
        val taskId = 10L
        val name = "New name"
        val newDay = LocalDate.now()
        val reminder = null

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = name,
                date = newDay,
                reminderTime = reminder,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        coVerify { taskReminderManager.cancel(taskId) }
    }

    @Test
    fun `should set max position when update task day`() = runTest {
        val taskId = 10L
        val newDay = LocalDate.now().plusDays(10)
        val maxPositionForDay = 2

        givenTask(taskId)
        coEvery { repository.getMaxPositionForDate(newDay) } returns maxPositionForDay

        val slot = slot<TaskEntity>()
        coEvery {
            repository.updateTask(capture(slot))
        } just runs

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = "Whatever",
                date = newDay,
                reminderTime = null,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        Truth.assertThat(slot.captured.position).isEqualTo(maxPositionForDay + 1)
    }

    @Test
    fun `should remove task notification after updating task`() = runTest {
        val taskId = 10L

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = "Whatever",
                date = LocalDate.now(),
                reminderTime = null,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        verify(exactly = 1) { taskNotificationManager.removeTaskNotification(taskId) }
    }

    @Test
    fun `should return NameCannotBeEmpty error when set empty name`() = runTest {
        val taskId = 10L
        val newName = ""

        givenTask(taskId)

        val result = sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = newName,
                date = LocalDate.now(),
                reminderTime = null,
                recurrenceType = null,
                recurringUpdateStrategy = null,
            ),
        )

        Truth.assertThat((result as Either.Error).error)
            .isInstanceOf(UpdateTaskUseCaseError.NameCannotBeEmpty::class.java)
        verify(exactly = 0) { taskNotificationManager.removeTaskNotification(taskId) }
    }

    @Test
    fun `should don't re-populate tasks when edit mode is single`() = runTest {
        val taskId = 10L
        val newName = "Whatever"

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = newName,
                date = LocalDate.now(),
                reminderTime = null,
                recurrenceType = RecurrenceType.DAILY,
                recurringUpdateStrategy = RecurringUpdateStrategy.SINGLE,
            ),
        )

        coVerify(exactly = 0) { populateRecurringTasksUseCase.invoke(any()) }
    }

    @Test
    fun `should re-populate tasks when edit mode is single and future`() = runTest {
        val taskId = 10L
        val newName = "Whatever"

        givenTask(taskId)

        sut(
            EditTaskUseCase.Params(
                taskId = taskId,
                name = newName,
                date = LocalDate.now(),
                reminderTime = null,
                recurrenceType = RecurrenceType.DAILY,
                recurringUpdateStrategy = RecurringUpdateStrategy.SINGLE_AND_FUTURE,
            ),
        )

        coVerifyOrder {
            repository.removeTask(taskId, RecurringRemovalStrategy.FUTURE_ONES)
            populateRecurringTasksUseCase.invoke(any())
        }
    }

    private fun givenTask(taskId: Long) {
        coEvery { repository.getTaskById(taskId) } returns flowOf(
            Task(
                id = taskId,
                name = "Hester Kinney",
                createdAt = LocalDate.now(),
                day = LocalDate.now().plusDays(1),
                reminder = Reminder(
                    10L,
                    LocalTime.of(9, 0),
                    LocalDate.now().plusDays(1)
                ),
                isDone = false,
                position = 86,
                isRecurring = true,
                recurrenceType = RecurrenceType.DAILY,
                recurrenceEndDate = null,
                parentId = null,
            )
        )
    }
}
