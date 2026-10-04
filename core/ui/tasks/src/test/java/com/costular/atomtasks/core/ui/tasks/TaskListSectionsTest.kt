package com.costular.atomtasks.core.ui.tasks

import com.costular.atomtasks.tasks.fake.TaskToday
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskListSectionsTest {
    private val pending = TaskToday.copy(id = 1L, isDone = false)
    private val done = TaskToday.copy(id = 2L, isDone = true)

    @Test
    fun `given visible completion when synchronized then membership waits for animation`() {
        val sections = TaskListSections(listOf(pending))
        val changed = pending.copy(isDone = true)
        val tasks = listOf(changed)

        sections.synchronize(tasks, setOf(pending.id))

        assertThat(sections.isCompleted(changed)).isEqualTo(false)
    }

    @Test
    fun `given visible completion when animation finishes then membership settles`() {
        val sections = TaskListSections(listOf(pending))
        val changed = pending.copy(isDone = true)
        val tasks = listOf(changed)
        sections.synchronize(tasks, setOf(pending.id))

        sections.finishAnimation(changed.id, true, tasks)

        assertThat(sections.isCompleted(changed)).isEqualTo(true)
    }

    @Test
    fun `given invisible completion when synchronized then membership settles immediately`() {
        val sections = TaskListSections(listOf(pending))
        val changed = pending.copy(isDone = true)
        val tasks = listOf(changed)

        sections.synchronize(tasks, emptySet())

        assertThat(sections.isCompleted(changed)).isEqualTo(true)
    }

    @Test
    fun `given visible reopening when synchronized then membership waits for animation`() {
        val sections = TaskListSections(listOf(done))
        val changed = done.copy(isDone = false)
        val tasks = listOf(changed)

        sections.synchronize(tasks, setOf(done.id))

        assertThat(sections.isCompleted(changed)).isEqualTo(true)
    }

    @Test
    fun `given visible reopening when animation finishes then membership settles`() {
        val sections = TaskListSections(listOf(done))
        val changed = done.copy(isDone = false)
        val tasks = listOf(changed)
        sections.synchronize(tasks, setOf(done.id))

        sections.finishAnimation(changed.id, false, tasks)

        assertThat(sections.isCompleted(changed)).isEqualTo(false)
    }

    @Test
    fun `given invisible reopening when synchronized then membership settles immediately`() {
        val sections = TaskListSections(listOf(done))
        val changed = done.copy(isDone = false)
        val tasks = listOf(changed)

        sections.synchronize(tasks, emptySet())

        assertThat(sections.isCompleted(changed)).isEqualTo(false)
    }

    @Test
    fun `given a superseded animation when it finishes then the current status wins`() {
        val sections = TaskListSections(listOf(pending))
        sections.synchronize(listOf(pending.copy(isDone = true)), setOf(pending.id))

        sections.finishAnimation(pending.id, true, listOf(pending))

        assertThat(sections.isCompleted(pending)).isFalse()
    }

    @Test
    fun `given a removed task when its id is reused then old membership is discarded`() {
        val sections = TaskListSections(listOf(pending))
        sections.synchronize(emptyList(), emptySet())
        sections.finishAnimation(pending.id, false, emptyList())
        val replacement = pending.copy(isDone = true)

        sections.synchronize(listOf(replacement), setOf(replacement.id))

        assertThat(sections.isCompleted(replacement)).isTrue()
    }

    @Test
    fun `given simultaneous completions when one animation finishes then the other task keeps waiting`() {
        val second = pending.copy(id = 3L)
        val sections = TaskListSections(listOf(pending, second))
        val tasks = listOf(pending.copy(isDone = true), second.copy(isDone = true))
        sections.synchronize(tasks, setOf(pending.id, second.id))

        sections.finishAnimation(second.id, true, tasks)

        assertThat(sections.isCompleted(tasks.first())).isFalse()
    }

    @Test
    fun `given simultaneous completions when one animation finishes then that task settles`() {
        val second = pending.copy(id = 3L)
        val sections = TaskListSections(listOf(pending, second))
        val tasks = listOf(pending.copy(isDone = true), second.copy(isDone = true))
        sections.synchronize(tasks, setOf(pending.id, second.id))

        sections.finishAnimation(second.id, true, tasks)

        assertThat(sections.isCompleted(tasks.last())).isTrue()
    }

    @Test
    fun `given interleaved statuses when pending tasks move then source indices are used`() {
        val last = pending.copy(id = 3L)
        val tasks = listOf(pending, done, last)

        val move = taskListMove(tasks, last.id, pending.id, TaskListSections(tasks))

        assertThat(move).isEqualTo(ItemPosition(2, last.id) to ItemPosition(0, pending.id))
    }

    @Test
    fun `given interleaved statuses when completed tasks move then source indices are used`() {
        val otherDone = done.copy(id = 3L)
        val tasks = listOf(done, pending, otherDone)

        val move = taskListMove(tasks, done.id, otherDone.id, TaskListSections(tasks))

        assertThat(move).isEqualTo(ItemPosition(0, done.id) to ItemPosition(2, otherDone.id))
    }

    @Test
    fun `given flat mode when dragging across statuses then the move is allowed`() {
        val tasks = listOf(pending, done)

        val move = taskListMove(tasks, pending.id, done.id, null)

        assertThat(move).isEqualTo(ItemPosition(0, pending.id) to ItemPosition(1, done.id))
    }

    @Test
    fun `given sections when dragging pending to completed then the move is rejected`() {
        val tasks = listOf(pending, done)
        val sections = TaskListSections(tasks)

        val move = taskListMove(tasks, pending.id, done.id, sections)

        assertThat(move).isNull()
    }

    @Test
    fun `given sections when dragging completed to pending then the move is rejected`() {
        val tasks = listOf(pending, done)
        val sections = TaskListSections(tasks)

        val move = taskListMove(tasks, done.id, pending.id, sections)

        assertThat(move).isNull()
    }

    @Test
    fun `given sections when dragging to a header then the move is rejected`() {
        val tasks = listOf(pending, done)
        val sections = TaskListSections(tasks)

        val move = taskListMove(tasks, pending.id, "completed_header", sections)

        assertThat(move).isNull()
    }

    @Test
    fun `given sections when dragging a removed task then the move is rejected`() {
        val tasks = listOf(pending, done)
        val sections = TaskListSections(tasks)

        val move = taskListMove(tasks, 99L, pending.id, sections)

        assertThat(move).isNull()
    }

    @Test
    fun `given an unfinished animation when dragging from a transitioning task then the move is rejected`() {
        val sections = TaskListSections(listOf(pending, done))
        val completed = pending.copy(isDone = true)
        val tasks = listOf(completed, done)
        sections.synchronize(tasks, setOf(pending.id, done.id))

        val move = taskListMove(tasks, completed.id, done.id, sections)

        assertThat(move).isNull()
    }

    @Test
    fun `given an unfinished animation when dragging to a transitioning task then the move is rejected`() {
        val sections = TaskListSections(listOf(pending, done))
        val completed = pending.copy(isDone = true)
        val tasks = listOf(completed, done)
        sections.synchronize(tasks, setOf(pending.id, done.id))

        val move = taskListMove(tasks, done.id, completed.id, sections)

        assertThat(move).isNull()
    }
}
