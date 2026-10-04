package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveTasksInRangeUseCaseTest {
    private val repository = mockk<TasksRepository>()
    private val useCase = ObserveTasksInRangeUseCase(repository)
    private val start = LocalDate.of(2026, 10, 3)
    private val end = start.plusDays(30)

    @Test
    fun `given a date range when observed then inclusive boundaries reach the repository`() = runTest {
        every { repository.observeTasksInRange(start, end) } returns flowOf(listOf(TaskToday))

        useCase(ObserveTasksInRangeUseCase.Params(start, end)).first()

        verify(exactly = 1) { repository.observeTasksInRange(start, end) }
    }

    @Test
    fun `given a date range when observed then domain tasks are returned`() = runTest {
        every { repository.observeTasksInRange(start, end) } returns flowOf(listOf(TaskToday))

        val result = useCase(ObserveTasksInRangeUseCase.Params(start, end)).first()

        assertThat((result as Either.Result).result).containsExactly(TaskToday)
    }

    @Test
    fun `given a failed range observation when collected then a recoverable error is returned`() = runTest {
        every { repository.observeTasksInRange(start, end) } returns flow { error("database unavailable") }

        val result = useCase(ObserveTasksInRangeUseCase.Params(start, end)).first()

        assertThat(result).isInstanceOf(Either.Error::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given inverted boundaries when constructing a range then it is rejected`() {
        ObserveTasksInRangeUseCase.Params(start, start.minusDays(1))
    }
}
