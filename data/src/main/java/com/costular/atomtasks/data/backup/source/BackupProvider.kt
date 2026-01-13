package com.costular.atomtasks.data.backup.source

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.data.backup.BackupError

interface BackupProvider {
    suspend fun writeBackup(data: String, accountName: String?): Either<BackupError, Unit>
    suspend fun readBackup(accountName: String?): Either<BackupError, String>
}
