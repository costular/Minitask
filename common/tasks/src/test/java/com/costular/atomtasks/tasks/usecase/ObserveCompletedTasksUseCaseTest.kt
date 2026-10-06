package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveCompletedTasksUseCaseTest {
    private val repository = mockk<TasksRepository>()
    private val useCase = ObserveCompletedTasksUseCase(repository)

    @Test
    fun `observation returns completed tasks from the repository`() = runTest {
        val task = TaskToday.copy(isDone = true)
        every { repository.observeCompletedTasks() } returns flowOf(listOf(task))
        assertThat((useCase(Unit).first() as Either.Result).result).containsExactly(task)
    }

    @Test
    fun `database errors become recoverable errors`() = runTest {
        every { repository.observeCompletedTasks() } returns flow { error("unavailable") }
        assertThat(useCase(Unit).first()).isInstanceOf(Either.Error::class.java)
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is propagated`() = runTest {
        every { repository.observeCompletedTasks() } returns flow { throw CancellationException() }
        useCase(Unit).first()
    }
}
