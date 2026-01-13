package com.costular.atomtasks.data.backup

import com.costular.atomtasks.core.Either

interface BackupRepository {
    suspend fun exportBackupToFile(): Either<BackupError, String>
    suspend fun importBackupFromFile(json: String): Either<BackupError, Unit>
}
