package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.model.UpdateTaskIsDoneError
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateTaskIsDoneUseCaseTest {

    lateinit var sut: UpdateTaskIsDoneUseCase

    private val tasksRepository: TasksRepository = mockk(relaxed = true)
    private val recurrenceScheduler: RecurrenceScheduler = mockk(relaxed = true)

    @Before
    fun setUp() {
        sut = UpdateTaskIsDoneUseCase(tasksRepository, recurrenceScheduler)
    }

    @Test
    fun `Should call mark task repository method and return success when invoke usecase`() = runTest {
        val taskId = 100L
        val isDone = true

        val result = sut(UpdateTaskIsDoneUseCase.Params(taskId, isDone))

        coVerify { tasksRepository.markTask(taskId, isDone) }
        assertThat(result).isEqualTo(Either.Result(Unit))
    }

    @Test
    fun `Should trigger recurrence scheduling and return success when task is marked as done`() = runTest {
        val taskId = 100L
        val isDone = true

        val result = sut(UpdateTaskIsDoneUseCase.Params(taskId, isDone))

        coVerify { recurrenceScheduler.scheduleTaskRecurrence(taskId) }
        assertThat(result).isEqualTo(Either.Result(Unit))
    }

    @Test
    fun `Should NOT trigger recurrence scheduling and return success when task is marked as NOT done`() = runTest {
        val taskId = 100L
        val isDone = false

        val result = sut(UpdateTaskIsDoneUseCase.Params(taskId, isDone))

        coVerify(exactly = 0) { recurrenceScheduler.scheduleTaskRecurrence(any()) }
        assertThat(result).isEqualTo(Either.Result(Unit))
    }

    @Test
    fun `Should return error when repository fails`() = runTest {
        coEvery { tasksRepository.markTask(any(), any()) } throws Exception("Boom")

        val result = sut(UpdateTaskIsDoneUseCase.Params(1L, true))

        assertThat(result).isEqualTo(Either.Error(UpdateTaskIsDoneError.UnknownError))
    }

    @Test
    fun `Should return error when recurrence scheduler fails`() = runTest {
        coEvery { recurrenceScheduler.scheduleTaskRecurrence(any()) } throws Exception("Scheduler Boom")

        val result = sut(UpdateTaskIsDoneUseCase.Params(1L, true))

        assertThat(result).isEqualTo(Either.Error(UpdateTaskIsDoneError.UnknownError))
    }
}