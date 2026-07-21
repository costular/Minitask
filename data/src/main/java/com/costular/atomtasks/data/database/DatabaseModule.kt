package com.costular.atomtasks.data.database

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DatabaseModule {

    @Binds
    abstract fun transactionRunner(roomTransactionRunner: RoomTransactionRunner): TransactionRunner
    companion object {
        @Singleton
        @Provides
        fun provideDatabase(@ApplicationContext context: Context): MinitaskDatabase =
            Room.databaseBuilder(context, MinitaskDatabase::class.java, "atomtasks.db")
                .addMigrations(
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                )
                .build()
    }
}
