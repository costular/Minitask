package com.costular.atomtasks.data.backup

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.toError
import com.costular.atomtasks.core.toResult
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import com.costular.atomtasks.data.settings.SettingsRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class BackupUseCasesTest {

    private val backupRepository: BackupRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()

    @Test
    fun `ExportBackupUseCase should return success from repository`() = runTest {
        val useCase = ExportBackupUseCase(backupRepository)
        val expectedJson = "{}"
        coEvery { backupRepository.exportBackupToFile() } returns expectedJson.toResult()

        val result = useCase(Unit)

        assertThat(result).isInstanceOf(Either.Result::class.java)
        result.fold(
            ifError = { },
            ifResult = { assertThat(it).isEqualTo(expectedJson) }
        )
        coVerify { backupRepository.exportBackupToFile() }
    }

    @Test
    fun `ImportBackupUseCase should return success from repository`() = runTest {
        val useCase = ImportBackupUseCase(backupRepository)
        val json = "{}"
        coEvery { backupRepository.importBackupFromFile(json) } returns Unit.toResult()

        val result = useCase(json)

        assertThat(result).isInstanceOf(Either.Result::class.java)
        coVerify { backupRepository.importBackupFromFile(json) }
    }

    @Test
    fun `ImportBackupUseCase should return error from repository`() = runTest {
        val useCase = ImportBackupUseCase(backupRepository)
        val json = "{}"
        val error = BackupError.Parse
        coEvery { backupRepository.importBackupFromFile(json) } returns error.toError()

        val result = useCase(json)

        assertThat(result).isInstanceOf(Either.Error::class.java)
        coVerify { backupRepository.importBackupFromFile(json) }
    }

    @Test
    fun `HasDataUseCase should return true when settings has user created task`() = runTest {
        val useCase = HasDataUseCase(settingsRepository)
        coEvery { settingsRepository.observeHasUserCreatedTask() } returns flowOf(true)

        val result = useCase(Unit)

        assertThat(result).isTrue()
    }

    @Test
    fun `HasDataUseCase should return false when settings has no user created task`() = runTest {
        val useCase = HasDataUseCase(settingsRepository)
        coEvery { settingsRepository.observeHasUserCreatedTask() } returns flowOf(false)

        val result = useCase(Unit)

        assertThat(result).isFalse()
    }
}
