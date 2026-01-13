package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.repository.TasksRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class CreateTaskUseCaseTest {

    lateinit var createTaskUseCase: CreateTaskUseCase

    private val tasksRepository: TasksRepository = mockk(relaxed = true)
    private val taskReminderManager: TaskReminderManager = mockk(relaxed = true)
    private val populateRecurringTasksUseCase: PopulateRecurringTasksUseCase = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        createTaskUseCase = CreateTaskUseCase(
            tasksRepository = tasksRepository,
            taskReminderManager = taskReminderManager,
            populateRecurringTasksUseCase = populateRecurringTasksUseCase,
            settingsRepository = settingsRepository,
        )
    }

    @Test
    fun `should call repository with given input when create task`() = runTest {
        val name = "Call my mom"
        val date = LocalDate.of(2021, 1, 7)
        val reminder = LocalTime.of(9, 0)

        createTaskUseCase.invoke(
            CreateTaskUseCase.Params(
                name = name,
                date = date,
                reminderEnabled = true,
                reminderTime = reminder,
                recurrenceType = RecurrenceType.WEEKLY,
            ),
        )

        coVerify(exactly = 1) {
            tasksRepository.createTask(
                name,
                date,
                true,
                reminder,
                RecurrenceType.WEEKLY,
                parentId = null,
            )
        }
    }

    @Test
    fun `should set reminder when create task given reminder's been passed correctly`() = runTest {
        val name = "Call my mom"
        val date = LocalDate.of(2021, 1, 7)
        val reminder = LocalTime.of(9, 0)
        val taskId = 100L

        coEvery {
            tasksRepository.createTask(name, date, true, reminder, null, null)
        } returns taskId

        createTaskUseCase.invoke(
            CreateTaskUseCase.Params(
                name = name,
                date = date,
                reminderEnabled = true,
                reminderTime = reminder,
                recurrenceType = null,
            ),
        )

        verify(exactly = 1) { taskReminderManager.set(taskId, reminder.atDate(date)) }
    }

    @Test
    fun `should set user created task flag when create task`() = runTest {
        val name = "Call my mom"
        val date = LocalDate.of(2021, 1, 7)

        createTaskUseCase.invoke(
            CreateTaskUseCase.Params(
                name = name,
                date = date,
                reminderEnabled = false,
                reminderTime = null,
                recurrenceType = null,
            ),
        )

        coVerify(exactly = 1) { settingsRepository.setHasUserCreatedTask(true) }
    }
}
