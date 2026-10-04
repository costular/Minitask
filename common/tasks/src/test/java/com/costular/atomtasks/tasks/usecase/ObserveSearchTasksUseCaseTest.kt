package com.costular.atomtasks.tasks.usecase

import app.cash.turbine.test
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveSearchTasksUseCaseTest {
    private val repository = mockk<TasksRepository>()
    private val useCase = ObserveSearchTasksUseCase(repository)

    @Test
    fun `given a padded query when observed then the normalized query reaches the repository`() = runTest {
        val matches = listOf(TaskToday.copy(id = 2), TaskToday.copy(id = 1))
        every { repository.observeSearchTasks("gYm") } returns flowOf(matches)

        useCase("  gYm  ").first()

        verify(exactly = 1) { repository.observeSearchTasks("gYm") }
    }

    @Test
    fun `given a padded query when observed then repository ordering is preserved`() = runTest {
        val matches = listOf(TaskToday.copy(id = 2), TaskToday.copy(id = 1))
        every { repository.observeSearchTasks("gYm") } returns flowOf(matches)

        val result = useCase("  gYm  ").first()

        assertThat((result as Either.Result).result).containsExactlyElementsIn(matches).inOrder()
    }

    @Test
    fun `given a padded query when observed then legacy day queries are not used`() = runTest {
        val matches = listOf(TaskToday.copy(id = 2), TaskToday.copy(id = 1))
        every { repository.observeSearchTasks("gYm") } returns flowOf(matches)

        useCase("  gYm  ").first()

        verify(exactly = 0) { repository.getTasks(any()) }
    }

    @Test
    fun `given a empty query when observed then a typed error is returned`() = runTest {
        val result = useCase("").first()

        assertThat(result).isInstanceOf(Either.Error::class.java)
    }

    @Test
    fun `given a empty query when observed then the repository is not accessed`() = runTest {
        useCase("").first()

        verify { repository wasNot Called }
    }

    @Test
    fun `given a blank query when observed then a typed error is returned`() = runTest {
        val result = useCase(" ").first()

        assertThat(result).isInstanceOf(Either.Error::class.java)
    }

    @Test
    fun `given a blank query when observed then the repository is not accessed`() = runTest {
        useCase(" ").first()

        verify { repository wasNot Called }
    }

    @Test
    fun `given a short query when observed then a typed error is returned`() = runTest {
        val result = useCase(" a ").first()

        assertThat(result).isInstanceOf(Either.Error::class.java)
    }

    @Test
    fun `given a short query when observed then the repository is not accessed`() = runTest {
        useCase(" a ").first()

        verify { repository wasNot Called }
    }

    @Test
    fun `given database updates when tasks are removed then the empty result is forwarded`() = runTest {
        val updates = MutableStateFlow(listOf(TaskToday))
        every { repository.observeSearchTasks("gym") } returns updates

        useCase("gym").test {
            awaitItem()

            updates.value = emptyList()

            assertThat((awaitItem() as Either.Result).result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given repository tasks when observed then tasks are forwarded without refiltering`() = runTest {
        every { repository.observeSearchTasks("gym") } returns flowOf(listOf(TaskToday))

        val result = useCase("gym").first()

        assertThat((result as Either.Result).result).containsExactly(TaskToday)
    }

    @Test
    fun `given an observation failure when searching then a typed error is returned`() = runTest {
        every { repository.observeSearchTasks("gym") } returns flow { error("database unavailable") }

        val result = useCase("gym").first()

        assertThat(result).isInstanceOf(Either.Error::class.java)
    }

    @Test(expected = CancellationException::class)
    fun `given a cancelled observation when searching then cancellation propagates`() = runTest {
        every { repository.observeSearchTasks("gym") } returns flow { throw CancellationException("cancelled") }

        useCase("gym").first()
    }
}
