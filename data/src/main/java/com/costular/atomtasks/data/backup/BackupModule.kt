package com.costular.atomtasks.data.backup

import com.costular.atomtasks.data.backup.source.BackupProvider
import com.costular.atomtasks.data.backup.source.FileBackupProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    abstract fun bindFileBackupProvider(impl: FileBackupProviderImpl): BackupProvider
}
