package com.costular.atomtasks.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.dialogs.ExactAlarmRationale
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.settings.sections.BackupSettingsSection
import com.costular.atomtasks.settings.sections.GeneralSection
import com.costular.atomtasks.settings.sections.SettingsAboutSection
import com.costular.atomtasks.settings.sections.TasksSettingsSection
import com.costular.designsystem.components.AtomTopBar
import com.costular.designsystem.dialogs.TimePickerDialog
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.settings.destinations.ThemeSelectorScreenDestination
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.getOr
import org.jetbrains.annotations.VisibleForTesting
import java.time.LocalTime

interface SettingsNavigator {
    fun navigateUp()
    fun navigateToSelectTheme(theme: String)
}

object EmptySettingsNavigator : SettingsNavigator {
    override fun navigateUp() = Unit
    override fun navigateToSelectTheme(theme: String) = Unit
}

private const val DefaultBackupFilename = "minitask_backup.json"

@Destination<SettingsGraph>(
    start = true,
)
@Composable
fun SettingsScreen(
    navigator: SettingsNavigator,
    resultRecipient: ResultRecipient<ThemeSelectorScreenDestination, String>,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    resultRecipient.onNavResult {
        it.getOr { null }?.let { theme ->
            viewModel.setTheme(Theme.fromString(theme))
        }
    }

    SettingsEvents(viewModel, state, snackbarHostState, context)

    val (createDocumentLauncher, openDocumentLauncher) = rememberBackupLaunchers(viewModel)

    SettingsDialogs(
        state = state,
        cancelImport = viewModel::cancelImport,
        confirmImport = viewModel::confirmImport,
        dismissExactAlarmRationale = viewModel::dismissExactAlarmRationale,
        exactAlarmPermissionChanged = viewModel::exactAlarmPermissionChanged,
        dismissDailyReminderTimePicker = viewModel::dismissDailyReminderTimePicker,
        updateDailyReminderTime = viewModel::updateDailyReminderTime,
        context = context,
    )

    SettingsScreen(
        state = state,
        navigator = navigator,
        snackbarHostState = snackbarHostState,
        onUpdateAutoforwardTasks = viewModel::setAutoforwardTasksEnabled,
        onEnableDailyReminder = viewModel::updateDailyReminder,
        onClickDailyReminder = viewModel::clickOnDailyReminderTimePicker,
        onBackupLocal = { createDocumentLauncher.launch(DefaultBackupFilename) },
        onRestoreLocal = { openDocumentLauncher.launch(arrayOf("application/json")) },
    )
}

@Composable
@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    scrollState: ScrollState = rememberScrollState(),
    state: SettingsState,
    navigator: SettingsNavigator,
    snackbarHostState: SnackbarHostState,
    onUpdateAutoforwardTasks: (Boolean) -> Unit,
    onEnableDailyReminder: (Boolean) -> Unit,
    onClickDailyReminder: () -> Unit,
    onBackupLocal: () -> Unit,
    onRestoreLocal: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AtomTopBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                    )
                },
                windowInsets = WindowInsets(
                    left = 0.dp,
                    top = 0.dp,
                    right = 0.dp,
                    bottom = 0.dp
                ),
            )
        },
    ) { contentPadding ->
        SettingsContent(
            padding = contentPadding,
            scrollState = scrollState,
            state = state,
            navigator = navigator,
            onUpdateAutoforwardTasks = onUpdateAutoforwardTasks,
            onEnableDailyReminder = onEnableDailyReminder,
            onClickDailyReminder = onClickDailyReminder,
            onBackupLocal = onBackupLocal,
            onRestoreLocal = onRestoreLocal
        )
    }
}

@Composable
private fun SettingsContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    scrollState: ScrollState,
    state: SettingsState,
    navigator: SettingsNavigator,
    onUpdateAutoforwardTasks: (Boolean) -> Unit,
    onEnableDailyReminder: (Boolean) -> Unit,
    onClickDailyReminder: () -> Unit,
    onBackupLocal: () -> Unit,
    onRestoreLocal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
            .padding(top = AppTheme.dimens.contentMargin)
            .padding(bottom = AppTheme.dimens.contentMargin),
    ) {
        GeneralSection(
            theme = state.theme,
            onSelectTheme = {
                navigator.navigateToSelectTheme(state.theme.asString())
            },
        )

        SectionSpacer()

        TasksSettingsSection(
            isMoveUndoneTasksTomorrowEnabled = state.moveUndoneTasksTomorrowAutomatically,
            onSetMoveUndoneTasksTomorrow = onUpdateAutoforwardTasks,
            dailyReminder = state.dailyReminder,
            onEnableDailyReminder = onEnableDailyReminder,
            onClickDailyReminder = onClickDailyReminder,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionSpacer()

        BackupSettingsSection(
            onBackupLocal = onBackupLocal,
            onRestoreLocal = onRestoreLocal,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionSpacer()

        HorizontalDivider()
        SectionSpacer()

        val context = LocalContext.current

        SettingsAboutSection(
            onOpenDonation = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.buymeacoffee.com/costular")
                )
                context.startActivity(intent)
            }
        )
    }
}


@Composable
private fun SectionSpacer() {
    Spacer(Modifier.height(16.dp))
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AtomTheme {
        SettingsScreen(
            state = SettingsState(),
            navigator = EmptySettingsNavigator,
            snackbarHostState = remember { SnackbarHostState() },
            onUpdateAutoforwardTasks = {},
            onEnableDailyReminder = {},
            onClickDailyReminder = {},
            onBackupLocal = {},
            onRestoreLocal = {},
        )
    }
}

@Composable
private fun SettingsDialogs(
    state: SettingsState,
    cancelImport: () -> Unit,
    confirmImport: () -> Unit,
    dismissExactAlarmRationale: () -> Unit,
    exactAlarmPermissionChanged: () -> Unit,
    dismissDailyReminderTimePicker: () -> Unit,
    updateDailyReminderTime: (LocalTime) -> Unit,
    context: android.content.Context,
) {
    if (state.backupProcessState is BackupProcessState.Loading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (state.isImportConfirmationVisible) {
        AlertDialog(
            onDismissRequest = cancelImport,
            title = { Text(stringResource(R.string.settings_backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.settings_backup_import_confirm_message)) },
            confirmButton = {
                TextButton(onClick = confirmImport) {
                    Text(stringResource(R.string.settings_backup_import_confirm_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = cancelImport) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.shouldShowExactAlarmRationale) {
        ExactAlarmRationale(
            onDismiss = dismissExactAlarmRationale,
            navigateToExactAlarmSettings = {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            },
            onPermissionStateChanged = exactAlarmPermissionChanged,
        )
    }

    if (state.isDailyReminderTimePickerOpen) {
        TimePickerDialog(
            onDismiss = dismissDailyReminderTimePicker,
            selectedTime = state.dailyReminder?.time ?: LocalTime.now(),
            onSelectTime = updateDailyReminderTime,
        )
    }
}

@Composable
private fun SettingsEvents(
    viewModel: SettingsViewModel,
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context,
) {
    LaunchedEffect(state.backupProcessState) {
        when (val processState = state.backupProcessState) {
            is BackupProcessState.Success -> {
                val message = when (processState.type) {
                    BackupOperationType.BACKUP -> context.getString(R.string.settings_backup_success)
                    BackupOperationType.RESTORE -> context.getString(R.string.settings_restore_success)
                }
                snackbarHostState.showSnackbar(message)
                viewModel.dismissBackupResult()
            }
            is BackupProcessState.Error -> {
                snackbarHostState.showSnackbar(processState.message ?: "Error")
                viewModel.dismissBackupResult()
            }
            else -> Unit
        }
    }
}

@Composable
private fun rememberBackupLaunchers(
    viewModel: SettingsViewModel,
): Pair<ActivityResultLauncher<String>, ActivityResultLauncher<Array<String>>> {
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.backupToLocal(it) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.requestImportLocal(it) }
    }

    return createDocumentLauncher to openDocumentLauncher
}




