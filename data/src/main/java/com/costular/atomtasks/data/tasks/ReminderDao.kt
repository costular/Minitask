package com.costular.atomtasks.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminderEntity: ReminderEntity)

    @Query("SELECT EXISTS(SELECT * FROM reminders WHERE task_id = :taskId)")
    suspend fun reminderExistForTask(taskId: Long): Boolean

    @Query("UPDATE reminders SET date = :date, time = :time WHERE task_id = :taskId")
    suspend fun updateReminder(taskId: Long, date: LocalDate, time: LocalTime)

    @Query("DELETE FROM reminders WHERE task_id = :taskId")
    suspend fun removeReminder(taskId: Long)

    @Update
    suspend fun update(reminderEntity: ReminderEntity)

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(reminders: List<ReminderEntity>)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
