package com.costular.atomtasks.verticaltasks

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateWindow(val start: LocalDate, val end: LocalDate) {
    fun contains(date: LocalDate): Boolean = date in start..end

    fun extendPast(): DateWindow {
        val newStart = start.minusDays(ChunkDays)
        val newEnd = minOf(end, newStart.plusDays(MaximumDays - 1))
        return DateWindow(newStart, newEnd)
    }

    fun extendFuture(): DateWindow {
        val newEnd = end.plusDays(ChunkDays)
        val newStart = maxOf(start, newEnd.minusDays(MaximumDays - 1))
        return DateWindow(newStart, newEnd)
    }

    val dayCount: Long get() = ChronoUnit.DAYS.between(start, end) + 1

    companion object {
        const val ChunkDays = 30L
        const val MaximumDays = 121L
        const val PrefetchDays = 7L
        fun around(date: LocalDate): DateWindow = DateWindow(date.minusDays(ChunkDays), date.plusDays(ChunkDays))
    }
}
