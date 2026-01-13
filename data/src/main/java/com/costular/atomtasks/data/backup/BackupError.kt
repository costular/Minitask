package com.costular.atomtasks.data.backup

sealed interface BackupError {
    data object IO : BackupError
    data object Parse : BackupError
    data object Auth : BackupError
    data object Network : BackupError
    data object NotFound : BackupError
    data class Unknown(val message: String? = null) : BackupError
}
