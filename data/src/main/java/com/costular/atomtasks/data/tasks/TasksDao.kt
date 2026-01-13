package com.costular.atomtasks.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@Dao
interface TasksDao {
    @Query("SELECT COUNT(*) FROM tasks;")
    suspend fun getTaskCount(): Int

    @Transaction
    suspend fun createTask(taskEntity: TaskEntity): Long {
        val latestPosition = getMaxPositionForDate(taskEntity.day)
        return addTask(taskEntity.copy(position = latestPosition + 1))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTask(taskEntity: TaskEntity): Long

    @Update
    suspend fun update(taskEntity: TaskEntity): Int

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY position ASC;")
    fun getAllTasks(): Flow<List<TaskAggregated>>

    @Transaction
    @Query(
        """
            SELECT * FROM tasks
            WHERE date = :date
            ORDER BY position ASC;
        """
    )
    fun getAllTasksForDate(date: LocalDate): Flow<List<TaskAggregated>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: Long): Flow<TaskAggregated?>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun removeTaskById(id: Long)

    @Query(
        value =
        """
            DELETE FROM tasks 
            WHERE 
                (parent_id = :id OR id = (SELECT parent_id FROM tasks WHERE id = :id) OR parent_id = (SELECT parent_id FROM tasks where id = :id) OR id = :id)
                AND (date > (SELECT date FROM tasks WHERE id = :id) OR id = :id)
        """
    )
    suspend fun removeFutureOccurrencesAndSelf(id: Long)

    @Query(
        value =
        """
            DELETE FROM tasks 
            WHERE (parent_id = :id OR id = (SELECT parent_id FROM tasks WHERE id = :id) OR parent_id = (SELECT parent_id FROM tasks where id = :id))
            AND date > (SELECT date FROM tasks WHERE id = :id)
        """
    )
    suspend fun removeFutureOccurrences(id: Long)

    @Query(
        value = """
            DELETE FROM tasks
            WHERE parent_id = :id OR id = (SELECT parent_id FROM tasks WHERE id = :id) OR parent_id = (SELECT parent_id FROM tasks where id = :id) OR id = :id;
        """
    )
    suspend fun removeAllOccurrences(id: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE parent_id = :parentId AND date > :currentDate")
    suspend fun countFutureOccurrences(parentId: Long, currentDate: LocalDate): Int

    @Query("UPDATE tasks SET is_done = :isDone WHERE id = :id")
    suspend fun updateTaskDone(id: Long, isDone: Boolean)

    @Query("UPDATE tasks SET position = :position WHERE id = :id")
    suspend fun updateTaskPosition(id: Long, position: Int)

    @Query("SELECT MAX(position) FROM tasks WHERE date = :day")
    suspend fun getMaxPositionForDate(day: LocalDate): Int

    @Suppress("MaxLineLength")
    @Transaction
    @Query("SELECT * FROM tasks WHERE ((position BETWEEN :start AND :end) OR (position BETWEEN :end AND :start)) AND (date = :day) ORDER BY position ASC")
    fun getTasksInRange(day: LocalDate, start: Int, end: Int): List<TaskAggregated>

    @Transaction
    @Query("SELECT * FROM tasks WHERE position = :position AND date = :day")
    suspend fun getTaskByPosition(day: LocalDate, position: Int): TaskAggregated?

    @Transaction
    suspend fun moveTask(day: LocalDate, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        val oldTask = getTaskByPosition(day, fromPosition)

        requireNotNull(oldTask?.task) {
            "Task that's being moved is null for some reason"
        }

        val oldTaskEntity = oldTask!!.task

        val isDownMovement = toPosition > fromPosition
        val shift = if (isDownMovement) -1 else +1
        val tasksToShift = getTasksInRange(day, fromPosition - shift, toPosition)

        updateTaskPosition(oldTaskEntity.id, -1)

        val sortedTasks = if (isDownMovement) {
            tasksToShift.sortedBy { it.task.position }
        } else {
            tasksToShift.sortedByDescending { it.task.position }
        }

        sortedTasks
            .forEach {
                val updatedPosition = it.task.position + shift
                updateTaskPosition(it.task.id, updatedPosition)
            }

        updateTaskPosition(oldTaskEntity.id, toPosition)
    }

    @Query("SELECT COUNT(*) from tasks WHERE is_done = 1")
    suspend fun getDoneTasksCount(): Int

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksRaw(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
