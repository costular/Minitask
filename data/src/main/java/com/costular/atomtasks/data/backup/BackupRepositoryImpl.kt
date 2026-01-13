package com.costular.atomtasks.data.backup

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.data.backup.source.FileBackupProviderImpl
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val fileProvider: FileBackupProviderImpl,
) : BackupRepository {

    override suspend fun exportBackupToFile(): Either<BackupError, String> {
        return fileProvider.readBackup(null)
    }

    override suspend fun importBackupFromFile(json: String): Either<BackupError, Unit> {
        return fileProvider.writeBackup(json, null)
    }
}
