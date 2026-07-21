package com.costular.atomtasks.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.costular.atomtasks.data.tasks.ReminderDao
import com.costular.atomtasks.data.tasks.ReminderEntity
import com.costular.atomtasks.data.tasks.TaskEntity
import com.costular.atomtasks.data.tasks.TasksDao
import com.google.common.truth.Truth
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var db: MinitaskDatabase
    private lateinit var tasksDao: TasksDao
    private lateinit var reminderDao: ReminderDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            MinitaskDatabase::class.java,
        ).build()
        tasksDao = db.getTasksDao()
        reminderDao = db.getRemindersDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun testReminder() = runTest {
        val time = LocalTime.of(9, 0)
        val date = LocalDate.now()

        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )

        val taskId = tasksDao.addTask(task)

        val reminder = ReminderEntity(
            reminderId = 0L,
            time = time,
            date = date,
            taskId = taskId,
        )
        reminderDao.insertReminder(reminder)

        val result = tasksDao.getTaskById(taskId).first()
        Truth.assertThat(reminderDao.reminderExistForTask(taskId)).isTrue()
        Truth.assertThat(result.reminder!!.time).isEqualTo(time)
        Truth.assertThat(result.reminder!!.date).isEqualTo(date)
    }

    @Test
    fun testUpdateReminder() = runTest {
        val time = LocalTime.of(21, 0)
        val date = LocalDate.now().plusDays(1)

        val task = TaskEntity(
            id = 0L,
            createdAt = LocalDate.now(),
            name = "whatever",
            day = LocalDate.now(),
            isDone = false,
            position = 1,
        )

        val taskId = tasksDao.addTask(task)

        val reminder = ReminderEntity(
            reminderId = 0L,
            time = LocalTime.now(),
            date = LocalDate.now(),
            taskId = taskId,
        )
        reminderDao.insertReminder(reminder)
        reminderDao.updateReminder(taskId, date, time)

        val result = tasksDao.getTaskById(taskId).first()
        Truth.assertThat(result.reminder!!.time).isEqualTo(time)
        Truth.assertThat(result.reminder!!.date).isEqualTo(date)
    }
}
