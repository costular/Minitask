package com.costular.atomtasks.tasks.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.notifications.TaskNotificationManager
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.google.common.truth.Truth.assertThat
import com.costular.atomtasks.tasks.usecase.ObserveTasksUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RestoreMissedTaskRemindersWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val observeTasksUseCase: ObserveTasksUseCase = mockk()
    private val taskNotificationManager: TaskNotificationManager = mockk(relaxed = true)

    private lateinit var sut: RestoreMissedTaskRemindersWorker

    @Before
    fun setUp() {
        sut = RestoreMissedTaskRemindersWorker(
            context,
            workerParams,
            observeTasksUseCase,
            taskNotificationManager,
        )
    }

    @Test
    fun `should remind overdue task for today when it is not done`() = runTest {
        val task = buildTask(
            id = 1L,
            reminder = Reminder(
                id = 10L,
                time = LocalTime.MIN,
                date = LocalDate.now(),
            ),
            isDone = false,
        )
        givenTasks(task)

        val result = sut.doWork()

        verify { taskNotificationManager.remindTask(task.id, task.name) }
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `should not remind task with future reminder for today`() = runTest {
        val futureTime = LocalDateTime.now().plusHours(1).toLocalTime()
        val task = buildTask(
            reminder = Reminder(
                id = 10L,
                time = futureTime,
                date = LocalDate.now(),
            ),
        )
        givenTasks(task)

        sut.doWork()

        verify(exactly = 0) { taskNotificationManager.remindTask(any(), any()) }
    }

    @Test
    fun `should not remind task with reminder from previous day`() = runTest {
        val task = buildTask(
            reminder = Reminder(
                id = 10L,
                time = LocalTime.MIN,
                date = LocalDate.now().minusDays(1),
            ),
        )
        givenTasks(task)

        sut.doWork()

        verify(exactly = 0) { taskNotificationManager.remindTask(any(), any()) }
    }

    @Test
    fun `should not remind task that is already done`() = runTest {
        val task = buildTask(
            reminder = Reminder(
                id = 10L,
                time = LocalTime.MIN,
                date = LocalDate.now(),
            ),
            isDone = true,
        )
        givenTasks(task)

        sut.doWork()

        verify(exactly = 0) { taskNotificationManager.remindTask(any(), any()) }
    }

    @Test
    fun `should not remind task without reminder`() = runTest {
        givenTasks(buildTask(reminder = null))

        sut.doWork()

        verify(exactly = 0) { taskNotificationManager.remindTask(any(), any()) }
    }

    @Test
    fun `should remind all overdue tasks for today`() = runTest {
        val firstTask = buildTask(
            id = 1L,
            name = "first",
            reminder = Reminder(
                id = 10L,
                time = LocalTime.MIN,
                date = LocalDate.now(),
            ),
        )
        val secondTask = buildTask(
            id = 2L,
            name = "second",
            reminder = Reminder(
                id = 20L,
                time = LocalTime.of(0, 1),
                date = LocalDate.now(),
            ),
        )
        givenTasks(secondTask, firstTask)

        sut.doWork()

        verifyOrder {
            taskNotificationManager.remindTask(firstTask.id, firstTask.name)
            taskNotificationManager.remindTask(secondTask.id, secondTask.name)
        }
    }

    @Test
    fun `should fail when observe tasks use case throws`() = runTest {
        every { observeTasksUseCase.invoke(any()) } throws IllegalStateException("boom")

        val result = sut.doWork()

        verify(exactly = 0) { taskNotificationManager.remindTask(any(), any()) }
        assertThat(result).isEqualTo(Result.failure())
    }

    private fun givenTasks(vararg tasks: Task) {
        every { observeTasksUseCase.invoke(any()) } returns flowOf(tasks.toList().toResult())
    }

    private fun buildTask(
        id: Long = 1L,
        name: String = "task",
        reminder: Reminder? = null,
        isDone: Boolean = false,
    ) = Task(
        id = id,
        name = name,
        createdAt = LocalDate.now(),
        day = LocalDate.now(),
        reminder = reminder,
        isDone = isDone,
        position = 0,
        isRecurring = false,
        recurrenceType = null,
        recurrenceEndDate = null,
        parentId = null,
    )
}
