package com.costular.atomtasks.tasks.helper.recurrence

import java.time.LocalDate

interface RecurrenceManager {
    suspend fun createAheadTasks(date: LocalDate)
    suspend fun createAheadForTask(taskId: Long)
}
