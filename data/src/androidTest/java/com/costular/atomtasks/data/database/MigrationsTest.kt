package com.costular.atomtasks.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationsTest {
    private val ALL_MIGRATIONS = arrayOf(MIGRATION_4_5, MIGRATION_5_6)
    private val TEST_DB = "migration-test"
    private val LATEST_VERSION = 7

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MinitaskDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO tasks (created_at, name, date, is_done) VALUES" +
                        " ('2023-08-23', 'This is a test', '2023-08-23', 0)",
            )
            close()
        }

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MinitaskDatabase::class.java,
            TEST_DB,
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            openHelper.writableDatabase.close()
        }

        helper.runMigrationsAndValidate(TEST_DB, LATEST_VERSION, true)
    }

    @Test
    fun testMigrate4To5() = runTest {
        val db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO tasks (created_at, name, date, is_done) VALUES " +
                        "('2023-08-23', 'This is a test', '2023-08-23', 0), " +
                        "('2023-08-24', 'First task for this day', '2023-08-24', 0), " +
                        "('2023-08-23', 'Second task', '2023-08-23', 0);",
            )
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        val cursor = db.query("SELECT * FROM tasks", arrayOf())

        if (cursor.moveToFirst()) {
            val positionColumnIndex = cursor.getColumnIndex("position")

            if (positionColumnIndex != -1) {
                val positions = mutableListOf<Int>()

                do {
                    val position = cursor.getInt(positionColumnIndex)
                    positions.add(position)
                } while (cursor.moveToNext())

                Truth.assertThat(positions[0]).isEqualTo(1)
                Truth.assertThat(positions[1]).isEqualTo(2)
                Truth.assertThat(positions[2]).isEqualTo(3)
            }
        }
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigration5to6() {
        helper.createDatabase(TEST_DB, 5).use { db ->
            db.execSQL(
                "INSERT INTO tasks (created_at, name, date, is_done) VALUES " +
                        "('2023-08-23', 'This is a test', '2023-08-23', 0); "
            )
            db.execSQL(
                "INSERT INTO reminders (reminder_id, time, date, is_enabled, task_id) VALUES " +
                        "(1, '09:00', '2023-10-15', 1, 1)"
            )
        }

        helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 6,
            validateDroppedTables = true,
            migrations = arrayOf(MIGRATION_5_6),
        ).use { db ->
            db.query("SELECT * FROM reminders", arrayOf()).use { cursor ->
                if (cursor.moveToFirst()) {
                    val positionColumnIndex = cursor.getColumnIndex("time")

                    if (positionColumnIndex != -1) {
                        val reminderTimes = mutableListOf<String>()

                        do {
                            val reminderTime = cursor.getString(positionColumnIndex)
                            reminderTimes.add(reminderTime)
                        } while (cursor.moveToNext())

                        Truth.assertThat(reminderTimes.first()).isEqualTo("09:00")
                    }
                }
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigration6to7() {
        helper.createDatabase(TEST_DB, 6).use { db ->
            db.execSQL(
                "INSERT INTO tasks (created_at, name, date, is_done) VALUES " +
                        "('2023-08-23', 'This is a test', '2023-08-23', 0); "
            )
        }

        helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 7,
            validateDroppedTables = true
        ).use { db ->
            db.query("SELECT * FROM tasks", arrayOf()).use { cursor ->
                Truth.assertThat(cursor.count).isEqualTo(1)
                cursor.moveToFirst()

                val name = cursor.getString(cursor.getColumnIndex("name"))
                val isRecurring = cursor.getInt(cursor.getColumnIndex("is_recurring")) == 1

                Truth.assertThat(name).isEqualTo("This is a test")
                Truth.assertThat(isRecurring).isFalse()
            }
        }
    }
}
