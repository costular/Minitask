package com.costular.atomtasks.core.ui.tasks

import androidx.compose.runtime.mutableStateMapOf
import com.costular.atomtasks.tasks.model.Task

/** Keeps visual section membership separate from the persisted completion status during animation. */
internal class TaskListSections(tasks: List<Task>) {
    private val completed = mutableStateMapOf<Long, Boolean>().apply {
        tasks.forEach { put(it.id, it.isDone) }
    }

    fun isCompleted(task: Task): Boolean = completed[task.id] ?: task.isDone

    fun canMove(from: Task, to: Task): Boolean =
        isCompleted(from) == isCompleted(to) &&
            from.isDone == to.isDone &&
            isCompleted(from) == from.isDone &&
            isCompleted(to) == to.isDone

    fun synchronize(tasks: List<Task>, visibleIds: Set<Long>) {
        completed.keys.retainAll(tasks.map { it.id }.toSet())
        tasks.forEach { task ->
            if (task.id !in completed || task.id !in visibleIds) {
                completed[task.id] = task.isDone
            }
        }
    }

    fun finishAnimation(taskId: Long, isDone: Boolean, tasks: List<Task>) {
        // Ignore callbacks from removed tasks or an animation superseded by another toggle.
        if (tasks.any { it.id == taskId && it.isDone == isDone }) {
            completed[taskId] = isDone
        }
    }
}

/** Translate lazy-list keys to the caller's source indices; headers never enter the sorting API. */
internal fun taskListMove(
    tasks: List<Task>,
    fromKey: Any,
    toKey: Any,
    sections: TaskListSections?,
): Pair<ItemPosition, ItemPosition>? {
    val fromIndex = tasks.indexOfFirst { it.id == fromKey }
    val toIndex = tasks.indexOfFirst { it.id == toKey }
    if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return null
    val from = tasks[fromIndex]
    val to = tasks[toIndex]
    return if (sections != null && !sections.canMove(from, to)) {
        null
    } else {
        ItemPosition(fromIndex, from.id) to ItemPosition(toIndex, to.id)
    }
}
