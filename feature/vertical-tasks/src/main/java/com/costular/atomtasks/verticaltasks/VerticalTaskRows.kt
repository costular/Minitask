package com.costular.atomtasks.verticaltasks

import com.costular.atomtasks.tasks.model.Task
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal fun buildVerticalTaskRows(
    window: DateWindow,
    tasks: List<Task>,
    today: LocalDate,
    undoneOnly: Boolean,
): ImmutableList<VerticalTaskRow> = buildList {
    val grouped = tasks.groupBy { it.day }
    if (window.start < today) {
        add(VerticalTaskRow.PastHeader(minOf(window.end, today.minusDays(1))))
        tasks.asSequence()
            .filter { it.day in window.start..window.end && it.day < today && (!undoneOnly || !it.isDone) }
            .sortedWith(compareByDescending<Task> { it.day }.thenBy { it.position }.thenBy { it.id })
            .forEach { add(VerticalTaskRow.Item(it)) }
        add(VerticalTaskRow.LoadOlder(window.start))
    }
    var day = maxOf(window.start, today)
    while (day <= window.end) {
        add(VerticalTaskRow.Header(day))
        val dailyTasks = grouped[day].orEmpty()
        if (dailyTasks.isEmpty()) add(VerticalTaskRow.Empty(day))
        else dailyTasks.forEach { add(VerticalTaskRow.Item(it)) }
        day = day.plusDays(1)
    }
}.toImmutableList()
