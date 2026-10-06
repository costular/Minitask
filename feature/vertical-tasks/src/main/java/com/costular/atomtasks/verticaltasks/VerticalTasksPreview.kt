package com.costular.atomtasks.verticaltasks

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
private fun VerticalTasksPreview() {
    val today = LocalDate.of(2026, 10, 5)
    val window = DateWindow(today.minusDays(30), today.plusDays(1))
    val task = Task(1, "Read a book", today, today.minusDays(1), null, false, 0, false, null, null, null)
    AtomTheme {
        VerticalTasksScreen(
            state = VerticalTasksState(
                selectedDay = today,
                window = window,
                rows = buildVerticalTaskRows(window, listOf(task), today, undoneOnly = false),
                isLoading = false,
            ),
            onToday = {},
            onSearch = {},
            onCompletedTasks = {},
            onCalendar = {},
            onLoadOlder = {},
            onDismissCalendar = {},
            onSelectDay = {},
            onScrollHandled = {},
            onAnchor = { _, _, _ -> },
            onVisibleRange = { _, _ -> },
            onRetry = {},
            onOpenTask = {},
            onMore = {},
            onDelete = {},
            onMark = { _, _ -> },
            onMove = { _, _ -> },
            onDragStarted = {},
            onDragStopped = {},
            today = today,
        )
    }
}
