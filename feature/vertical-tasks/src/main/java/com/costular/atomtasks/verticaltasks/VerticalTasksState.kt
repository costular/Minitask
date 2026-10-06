package com.costular.atomtasks.verticaltasks

import com.costular.atomtasks.tasks.model.Task
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface VerticalTaskRow {
    val day: LocalDate
    val key: Any

    data class PastHeader(override val day: LocalDate) : VerticalTaskRow {
        override val key: String get() = "past"
    }
    data class LoadOlder(override val day: LocalDate) : VerticalTaskRow {
        override val key: String get() = "load_older"
    }
    data class Header(override val day: LocalDate) : VerticalTaskRow {
        override val key: String get() = "day:$day"
    }
    data class Empty(override val day: LocalDate) : VerticalTaskRow {
        override val key: String get() = "empty:$day"
    }
    data class Item(val task: Task) : VerticalTaskRow {
        override val day: LocalDate get() = task.day
        override val key: Long get() = task.id
    }
}

data class ScrollTarget(
    val day: LocalDate,
    val taskId: Long? = null,
    val offset: Int = 0,
    val requestId: Long = 0,
)

data class VerticalTasksState(
    val selectedDay: LocalDate,
    val window: DateWindow,
    val rows: ImmutableList<VerticalTaskRow> = persistentListOf(),
    val pastTasksUndoneOnlyEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val isDragging: Boolean = false,
    val shouldShowCalendar: Boolean = false,
    val scrollTarget: ScrollTarget? = null,
)
