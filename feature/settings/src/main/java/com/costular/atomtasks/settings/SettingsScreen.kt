package com.costular.atomtasks.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.settings.destinations.ThemeSelectorScreenDestination
import com.costular.designsystem.components.AtomTopBar
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.getOr
import org.jetbrains.annotations.VisibleForTesting

interface SettingsNavigator {
    fun navigateUp()
    fun navigateToSelectTheme(theme: String)
}

object EmptySettingsNavigator : SettingsNavigator {
    override fun navigateUp() = Unit
    override fun navigateToSelectTheme(theme: String) = Unit
}

@Destination
@Composable
fun SettingsScreen(
    navigator: SettingsNavigator,
    resultRecipient: ResultRecipient<ThemeSelectorScreenDestination, String>,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    resultRecipient.onNavResult {
        it.getOr { null }?.let { theme ->
            viewModel.setTheme(Theme.fromString(theme))
        }
    }

    SettingsScreen(
        state = state,
        navigator = navigator,
        onUpdateAutoforwardTasks = viewModel::setAutoforwardTasksEnabled,
    )
}

@Composable
@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    scrollState: ScrollState = rememberScrollState(),
    state: SettingsState,
    navigator: SettingsNavigator,
    onUpdateAutoforwardTasks: (Boolean) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AtomTopBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                    )
                },
                windowInsets = WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding)
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
            onUpdateAutoforwardTasks = {},
        )
    }
}
