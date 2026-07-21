package com.costular.atomtasks.data.tasks

import com.costular.atomtasks.data.database.MinitaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun providesTaskDao(
        database: MinitaskDatabase,
    ): TasksDao = database.getTasksDao()

    @Provides
    fun providesRemindersDao(
        database: MinitaskDatabase,
    ): ReminderDao = database.getRemindersDao()
}
