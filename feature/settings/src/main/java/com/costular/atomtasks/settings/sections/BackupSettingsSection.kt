package com.costular.atomtasks.settings.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.settings.SettingSection
import com.costular.atomtasks.settings.components.SettingOption

@Composable
fun BackupSettingsSection(
    onBackupLocal: () -> Unit,
    onRestoreLocal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingSection(
        title = stringResource(R.string.settings_backup_title),
        modifier = modifier.fillMaxWidth(),
    ) {
        SettingOption(
            title = stringResource(R.string.settings_backup_local_export),
            option = "",
            icon = Icons.Outlined.Save,
            onClick = onBackupLocal,
        )

        SettingOption(
            title = stringResource(R.string.settings_backup_local_import),
            option = "",
            icon = Icons.Outlined.UploadFile,
            onClick = onRestoreLocal,
        )
    }
}
