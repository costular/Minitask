package com.costular.atomtasks.tasks.interactor

import com.costular.atomtasks.tasks.TasksRepository
import com.costular.atomtasks.tasks.manager.TaskReminderManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalTime
@ExperimentalCoroutinesApi
class CreateTaskInteractorTest {

    private lateinit var createTaskInteractor: CreateTaskInteractor

    private val tasksRepository: TasksRepository = mockk(relaxed = true)
    private val taskReminderManager: TaskReminderManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        createTaskInteractor = CreateTaskInteractor(
            tasksRepository = tasksRepository,
            taskReminderManager = taskReminderManager,
        )
    }

    @Test
    fun `should call repository with given input when create task`() = runBlockingTest {
        val name = "Call my mom"
        val date = LocalDate.of(2021, 1, 7)
        val reminder = LocalTime.of(9, 0)

        createTaskInteractor.executeSync(
            CreateTaskInteractor.Params(
                name = name,
                date = date,
                reminderEnabled = true,
                reminderTime = reminder,
            ),
        )

        coEvery { tasksRepository.createTask(name, date, true, reminder) }
    }

    @Test
    fun `should set reminder when create task given reminder's been passed correctly`() =
        runBlockingTest {
            val name = "Call my mom"
            val date = LocalDate.of(2021, 1, 7)
            val reminder = LocalTime.of(9, 0)
            val taskId = 100L

            coEvery {
                tasksRepository.createTask(name, date, true, reminder)
            } returns taskId

            createTaskInteractor.executeSync(
                CreateTaskInteractor.Params(
                    name = name,
                    date = date,
                    reminderEnabled = true,
                    reminderTime = reminder,
                ),
            )

            verify { taskReminderManager.set(taskId, reminder.atDate(date)) }
        }
}