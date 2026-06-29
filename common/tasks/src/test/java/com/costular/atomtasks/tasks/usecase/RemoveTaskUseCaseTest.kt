package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.helper.TaskReminderManager
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.RemoveTaskError
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RemoveTaskUseCaseTest {

    lateinit var sut: RemoveTaskUseCase

    private val tasksRepository: TasksRepository = mockk(relaxUnitFun = true)
    private val tasksReminderManager: TaskReminderManager = mockk(relaxUnitFun = true)
    private val taskNotificationManager: TaskNotificationManager = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        sut = RemoveTaskUseCase(
            tasksRepository = tasksRepository,
            taskReminderManager = tasksReminderManager,
            taskNotificationManager = taskNotificationManager,
        )
    }

    @Test
    fun `Should call repository remove task with proper params when execute usecase`() = runTest {
        val taskId = 102L

        sut.invoke(RemoveTaskUseCase.Params(taskId))

        coVerify(exactly = 1) { tasksRepository.removeTask(taskId, null) }
    }

    @Test
    fun `Should call task reminder manager to cancel task with proper params when execute usecase`() =
        runTest {
            val taskId = 102L

            sut.invoke(RemoveTaskUseCase.Params(taskId))

            coVerify(exactly = 1) { tasksReminderManager.cancel(taskId) }
        }

    @Test
    fun `Should remove task notification when execute usecase`() = runTest {
        val taskId = 102L

        sut.invoke(RemoveTaskUseCase.Params(taskId))

        verify(exactly = 1) { taskNotificationManager.removeTaskNotification(taskId) }
    }

    @Test
    fun `Should return either error when execute usecase throws an exception`() = runTest {
        val taskId = 102L
        coEvery { tasksRepository.removeTask(taskId, null) } throws Exception("")

        val result = sut.invoke(RemoveTaskUseCase.Params(taskId))

        Truth.assertThat(result).isEqualTo(Either.Error(RemoveTaskError.UnknownError))
    }

    @Test
    fun `Should call task repository remove recurring when execute usecase given removal strategy was passed via params`() =
        runTest {
            val taskId = 102L
            val recurringRemovalStrategy = RecurringRemovalStrategy.SINGLE_AND_FUTURE_ONES

            sut.invoke(RemoveTaskUseCase.Params(taskId, recurringRemovalStrategy))

            coVerify(exactly = 1) { tasksRepository.removeTask(taskId, recurringRemovalStrategy) }
        }
}
