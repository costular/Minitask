package com.costular.atomtasks.agenda

import com.costular.atomtasks.agenda.ui.AgendaScreen
import com.costular.atomtasks.agenda.ui.AgendaState
import com.costular.atomtasks.agenda.ui.TasksState
import com.costular.atomtasks.core.testing.ui.AndroidTest
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.tasks.model.Task
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

@HiltAndroidTest
class AgendaTest : AndroidTest() {

    @Test
    fun shouldShowYesterdayHeader_whenSelectedDayIsYesterday() {
        givenAgenda(
            state = AgendaState(
                selectedDay = LocalDate.now().minusDays(1).asDay(),
            ),
        )

        agenda {
            assertDayText(composeTestRule.getString(R.string.day_yesterday))
        }
    }

    @Test
    fun shouldShowTomorrowHeader_whenSelectedDayIsTomorrow() {
        givenAgenda(
            state = AgendaState(
                selectedDay = LocalDate.now().plusDays(1).asDay(),
            ),
        )

        agenda {
            assertDayText(composeTestRule.getString(R.string.day_tomorrow))
        }
    }

    @Test
    fun shouldShowTodayHeader_whenSelectedDayIsToday() {
        givenAgenda()

        agenda {
            assertDayText(composeTestRule.getString(R.string.today))
        }
    }

    @Test
    fun shouldShowTaskInList_whenLandOnScreen() {
        val task = Task(
            id = 1L,
            name = "this is a test :D",
            createdAt = LocalDate.now(),
            reminder = null,
            isDone = true,
            day = LocalDate.now(),
            position = 1,
            isRecurring = false,
            recurrenceEndDate = null,
            recurrenceType = null,
            parentId = null,
        )

        givenAgenda(
            state = AgendaState(
                tasks = TasksState.Success(
                    persistentListOf(task),
                ),
            ),
        )

        agenda {
            taskHasText(task.name)
        }
    }

    @Test
    fun shouldShowTaskMarkedAsDone_whenLandOnScreen() {
        val taskName = "this is a test :D"
        val isDone = true

        val tasks = listOf(
            Task(
                id = 1L,
                name = taskName,
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                reminder = null,
                isDone = isDone,
                position = 1,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
        )

        givenAgenda(
            state = AgendaState(
                tasks = TasksState.Success(tasks.toImmutableList()),
            ),
        )

        agenda {
            taskIsDone(taskName, isDone)
        }
    }

    @Test
    fun shouldShowTaskMarkedAsNotDone_whenLandOnScreen() {
        val taskName = "this is a test :D"
        val isDone = false

        val tasks = listOf(
            Task(
                id = 1L,
                name = taskName,
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                reminder = null,
                isDone = isDone,
                position = 1,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
        )

        givenAgenda(
            state = AgendaState(
                tasks = TasksState.Success(tasks.toImmutableList()),
            ),
        )

        agenda {
            taskIsDone(taskName, isDone)
        }
    }

    @Test
    fun shouldToggleTaskStatus_whenClickOnTaskCheckbox() {
        val id = 1L
        val taskName = "this is a test :D"
        val isDone = false

        val tasks = listOf(
            Task(
                id = id,
                name = taskName,
                createdAt = LocalDate.now(),
                day = LocalDate.now(),
                reminder = null,
                isDone = isDone,
                position = 1,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
        )

        givenAgenda(
            state = AgendaState(
                tasks = TasksState.Success(tasks.toImmutableList()),
            ),
        )

        agenda {
            toggleTask(taskName)
        }
    }

    private fun givenAgenda(state: AgendaState = AgendaState.Empty) {
        composeTestRule.setContent {
            AgendaScreen(
                state = state,
                onSelectDate = {},
                onSelectToday = {},
                onClickOpenCalendarView = {},
                onDismissCalendarView = {},
                onMarkTask = { _, _ -> },
                openTaskDetail = {},
                openTaskAction = {},
                onDeleteTask = {},
                onDragTask = { _, _ -> },
                onDragStopped = {},
            )
        }
    }
}
