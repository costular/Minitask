package com.costular.atomtasks.edittask

import app.cash.turbine.test
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.testing.MviViewModelTest
import com.costular.atomtasks.core.toError
import com.costular.atomtasks.tasks.fake.TaskToday
import com.costular.atomtasks.tasks.model.UpdateTaskUseCaseError
import com.costular.atomtasks.tasks.usecase.EditTaskUseCase
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import com.costular.atomtasks.ui.features.edittask.EditTaskViewModel
import com.costular.atomtasks.ui.features.edittask.SavingState
import com.costular.atomtasks.ui.features.edittask.TaskState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EditTaskViewModelTest : MviViewModelTest() {

    lateinit var sut: EditTaskViewModel

    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk(relaxed = true)
    private val editTaskUseCase: EditTaskUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        sut = EditTaskViewModel(
            getTaskByIdUseCase = getTaskByIdUseCase,
            editTaskUseCase = editTaskUseCase,
        )
    }

    @Test
    fun `should load task successfully`() = runTest {
        coEvery { getTaskByIdUseCase.invoke(any()) } returns flowOf(TaskToday)

        sut.loadTask(TaskToday.id)

        sut.state.test {
            assertThat(
                (expectMostRecentItem().taskState as TaskState.Success).taskId,
            ).isEqualTo(TaskToday.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit idle when state is created`() = runTest {
        sut.state.test {
            assertThat(awaitItem().taskState).isInstanceOf(TaskState.Idle::class.java)
        }
    }

    @Test
    fun `should not be able to update if task has not been loaded`() = runTest {
        sut.editTask(
            name = "whatever",
            date = LocalDate.now(),
            reminder = null,
            recurrenceType = null,
        )

        coVerify(exactly = 0) { editTaskUseCase(any()) }
    }

    @Test
    fun `should emit success when edit task succeeded`() = runTest {
        coEvery { getTaskByIdUseCase.invoke(any()) } returns flowOf(TaskToday)
        coEvery { editTaskUseCase.invoke(any()) } returns Either.Result(Unit)

        val newTask = "whatever"
        val newDate = LocalDate.now().plusDays(1)
        val newReminder = LocalTime.of(10, 0)

        sut.loadTask(TaskToday.id)
        sut.editTask(
            name = newTask,
            date = newDate,
            reminder = newReminder,
            recurrenceType = null,
        )

        sut.state.test {
            assertThat(expectMostRecentItem().savingTask).isInstanceOf(SavingState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit error when edit task fails`() = runTest {
        coEvery { getTaskByIdUseCase.invoke(any()) } returns flowOf(TaskToday)
        coEvery {
            editTaskUseCase.invoke(any())
        } returns UpdateTaskUseCaseError.UnknownError.toError()

        val newTask = "whatever"
        val newDate = LocalDate.now().plusDays(1)
        val newReminder = LocalTime.of(10, 0)

        sut.loadTask(TaskToday.id)
        sut.editTask(
            name = newTask,
            date = newDate,
            reminder = newReminder,
            recurrenceType = null,
        )

        assertThat(sut.state.value.savingTask).isInstanceOf(SavingState.Failure::class.java)
    }
}
