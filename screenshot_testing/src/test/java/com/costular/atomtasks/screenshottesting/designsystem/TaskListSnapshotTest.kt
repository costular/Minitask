package com.costular.atomtasks.screenshottesting.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.costular.atomtasks.screenshottesting.utils.FontSize
import com.costular.atomtasks.screenshottesting.utils.PaparazziFactory
import com.costular.atomtasks.screenshottesting.utils.Theme
import com.costular.atomtasks.screenshottesting.utils.asFloat
import com.costular.atomtasks.screenshottesting.utils.isDarkTheme
import com.costular.atomtasks.screenshottesting.utils.screenshot
import com.costular.atomtasks.tasks.model.Reminder
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.core.ui.tasks.TaskList
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Ignore("This causes a OutOfMemoryError that should be fixed in the future")
@RunWith(TestParameterInjector::class)
class TaskListSnapshotTest {

    @TestParameter
    private lateinit var fontScale: FontSize

    @TestParameter
    private lateinit var themeMode: Theme

    @get:Rule
    val paparazzi: Paparazzi = PaparazziFactory.create()

    @Test
    fun emptyTaskList() {
        paparazzi.screenshot(
            isDarkTheme = themeMode.isDarkTheme(),
            fontScale = fontScale.asFloat(),
        ) {
            GeneratedTaskList(tasks = emptyList())
        }
    }

    @Test
    fun taskList() {
        paparazzi.screenshot(
            isDarkTheme = themeMode.isDarkTheme(),
            fontScale = fontScale.asFloat(),
        ) {
            GeneratedTaskList(
                tasks = listOf(
                    Task(
                        0L,
                        "Task 1",
                        LocalDate.now(),
                        LocalDate.now(),
                        null,
                        true,
                        position = 1,
                        isRecurring = false,
                        recurrenceEndDate = null,
                        recurrenceType = null,
                        parentId = null,
                    ),
                    Task(
                        id = 0L,
                        name = "Task 2",
                        createdAt = LocalDate.now(),
                        day = LocalDate.now(),
                        reminder = Reminder(
                            id = 1L,
                            time = LocalTime.of(9, 0),
                            date = LocalDate.now(),
                        ),
                        isDone = false,
                        position = 2,
                        isRecurring = false,
                        recurrenceEndDate = null,
                        recurrenceType = null,
                        parentId = null,
                    ),
                    Task(
                        id = 0L,
                        name = "Task 3",
                        createdAt = LocalDate.now(),
                        day = LocalDate.now(),
                        reminder = null,
                        isDone = false,
                        position = 3,
                        isRecurring = false,
                        recurrenceEndDate = null,
                        recurrenceType = null,
                        parentId = null,
                    ),
                ),
            )
        }
    }

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun GeneratedTaskList(tasks: List<Task>) {
        Scaffold {
            TaskList(
                tasks = tasks,
                onClick = {},
                onMarkTask = { _, _ -> },
                modifier = Modifier.fillMaxWidth(),
                onClickMore = {},
                onDeleteTask = {},
                onMove = { _, _ -> },
                onDragStopped = {},
            )
        }
    }
}
