package com.costular.atomtasks.data.backup

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.usecase.UseCase
import com.costular.atomtasks.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) : UseCase<Unit, Either<BackupError, String>> {
    override suspend fun invoke(params: Unit): Either<BackupError, String> =
        backupRepository.exportBackupToFile()
}

class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) : UseCase<String, Either<BackupError, Unit>> {
    override suspend fun invoke(params: String): Either<BackupError, Unit> =
        backupRepository.importBackupFromFile(params)
}

class HasDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : UseCase<Unit, Boolean> {
    override suspend fun invoke(params: Unit): Boolean =
        settingsRepository.observeHasUserCreatedTask().first()
}
