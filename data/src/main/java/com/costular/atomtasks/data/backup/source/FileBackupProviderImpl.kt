package com.costular.atomtasks.data.backup.source

import androidx.room.withTransaction
import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.toError
import com.costular.atomtasks.core.toResult
import com.costular.atomtasks.data.backup.BackupDto
import com.costular.atomtasks.data.backup.BackupError
import com.costular.atomtasks.data.backup.ReminderBackupDto
import com.costular.atomtasks.data.backup.TaskBackupDto
import com.costular.atomtasks.data.database.AtomTasksDatabase
import com.costular.atomtasks.data.tasks.ReminderDao
import com.costular.atomtasks.data.tasks.ReminderEntity
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.data.tasks.TasksDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FileBackupProviderImpl @Inject constructor(
    private val tasksDao: TasksDao,
    private val reminderDao: ReminderDao,
    private val db: AtomTasksDatabase,
) : BackupProvider {

    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    override suspend fun readBackup(accountName: String?): Either<BackupError, String> {
        return try {
            val tasks = tasksDao.getAllTasksRaw().map { it.toBackupDto() }
            val reminders = reminderDao.getAllReminders().map { it.toBackupDto() }
            val backup = BackupDto(tasks = tasks, reminders = reminders)
            jsonSerializer.encodeToString(backup).toResult()
        } catch (e: Exception) {
            BackupError.Unknown(e.message).toError()
        }
    }

    override suspend fun writeBackup(data: String, accountName: String?): Either<BackupError, Unit> {
        return try {
            val backup = jsonSerializer.decodeFromString<BackupDto>(data)
            db.withTransaction {
                tasksDao.deleteAll()
                reminderDao.deleteAll()
                tasksDao.addAll(backup.tasks.map { it.toEntity() })
                reminderDao.addAll(backup.reminders.map { it.toEntity() })
            }
            Unit.toResult()
        } catch (e: Exception) {
            BackupError.Parse.toError()
        }
    }

    private fun TaskEntity.toBackupDto() = TaskBackupDto(
        id = id,
        createdAt = createdAt,
        name = name,
        day = day,
        isDone = isDone,
        position = position,
        isRecurring = isRecurring,
        recurrenceType = recurrenceType,
        recurrenceEndDate = recurrenceEndDate,
        parentId = parentId
    )

    private fun ReminderEntity.toBackupDto() = ReminderBackupDto(
        reminderId = reminderId,
        time = time,
        date = date,
        taskId = taskId
    )

    private fun TaskBackupDto.toEntity() = TaskEntity(
        id = id,
        createdAt = createdAt,
        name = name,
        day = day,
        isDone = isDone,
        position = position,
        isRecurring = isRecurring,
        recurrenceType = recurrenceType,
        recurrenceEndDate = recurrenceEndDate,
        parentId = parentId
    )

    private fun ReminderBackupDto.toEntity() = ReminderEntity(
        reminderId = reminderId,
        time = time,
        date = date,
        taskId = taskId
    )
}
