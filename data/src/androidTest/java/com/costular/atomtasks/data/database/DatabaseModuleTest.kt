package com.costular.atomtasks.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
@Module
class DatabaseModuleTest {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): MinitaskDatabase =
        Room.inMemoryDatabaseBuilder(context, MinitaskDatabase::class.java)
            .build()
}
