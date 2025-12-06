package com.costular.atomtasks.tasks.helper.recurrence

import com.costular.atomtasks.tasks.model.RecurrenceType

object RecurrenceLookAhead {
    fun numberOfOccurrencesForType(recurrenceType: RecurrenceType): Int {
        return when (recurrenceType) {
            RecurrenceType.DAILY -> DaysToCreate
            RecurrenceType.WEEKDAYS -> WeekdaysToCreate
            RecurrenceType.WEEKLY -> WeeksToCreate
            RecurrenceType.MONTHLY -> WeeksToCreate
            RecurrenceType.YEARLY -> YearsToCreate
        }
    }

    private const val DaysToCreate = 14
    private const val WeekdaysToCreate = 10
    private const val WeeksToCreate = 4
    private const val YearsToCreate = 2
}
