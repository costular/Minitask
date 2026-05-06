package com.atomtasks.feature.detail

import androidx.lifecycle.SavedStateHandle
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.ui.SnackbarManager
import com.costular.atomtasks.tasks.usecase.AreExactRemindersAvailable
import com.costular.atomtasks.tasks.usecase.CreateTaskUseCase
import com.costular.atomtasks.tasks.usecase.EditTaskUseCase
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@Ignore("Will be done in the future")
class TaskDetailViewModelTest {

    lateinit var sut: TaskDetailViewModel

    private val areExactRemindersAvailable: AreExactRemindersAvailable = mockk()
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val editTaskUseCase: EditTaskUseCase = mockk()
    private val createTaskUseCase: CreateTaskUseCase = mockk()
    private val removeTaskUseCase: RemoveTaskUseCase = mockk()
    private val atomAnalytics: AtomAnalytics = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private val updateTaskIsDoneUseCase: UpdateTaskIsDoneUseCase = mockk()
    private val snackbarManager: SnackbarManager = mockk()

    @Before
    fun setUp() {
        sut = TaskDetailViewModel(
            savedStateHandle = savedStateHandle,
            areExactRemindersAvailable = areExactRemindersAvailable,
            getTaskByIdUseCase = getTaskByIdUseCase,
            editTaskUseCase = editTaskUseCase,
            createTaskUseCase = createTaskUseCase,
            removeTaskUseCase = removeTaskUseCase,
            atomAnalytics = atomAnalytics,
            updateTaskIsDoneUseCase = updateTaskIsDoneUseCase,
            snackbarManager = snackbarManager,
        )
    }

    @Test
    fun `should expose date when pass date`() = runTest {
        val date = LocalDate.of(2021, 12, 24)

        sut.onDateChanged(date)

        assertThat(sut.state.value.taskState.date).isEqualTo(date)
    }

    @Test
    fun `should expose reminder when pass local time`() = runTest {
        val localTime = LocalTime.of(9, 0)

        sut.onReminderChanged(localTime)

        assertThat(sut.state.value.taskState.reminder).isEqualTo(localTime)
    }
}
