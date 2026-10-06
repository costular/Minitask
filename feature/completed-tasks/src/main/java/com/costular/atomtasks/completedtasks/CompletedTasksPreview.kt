package com.costular.atomtasks.completedtasks

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf

@Preview(showBackground = true)
@Composable
private fun CompletedTasksPreview() {
    val day = LocalDate.of(2026, 10, 5)
    val task = Task(1, "Read a book", day, day, null, true, 0, false, null, null, null)
    AtomTheme {
        CompletedTasksScreen(
            state = CompletedTasksState.Success(persistentListOf(task, task.copy(id = 2, day = day.minusDays(1)))),
            onBack = {},
            onRetry = {},
            onOpenTask = {},
            onMore = {},
            onDelete = {},
            onMark = { _, _ -> },
        )
    }
}
