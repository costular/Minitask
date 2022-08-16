package com.costular.atomtasks.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.costular.atomtasks.core_ui.utils.rememberFlowWithLifecycle
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.settings.destinations.ThemeSelectorScreenDestination
import com.costular.commonui.components.AtomTopBar
import com.costular.commonui.theme.AppTheme
import com.costular.commonui.theme.AtomRemindersTheme
import com.google.accompanist.insets.statusBarsPadding
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.EmptyResultRecipient
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.getOr

interface SettingsNavigator {
    fun navigateUp()
    fun navigateToSelectTheme(theme: String)
}

object EmptySettingsNavigator : SettingsNavigator {
    override fun navigateUp() {}
    override fun navigateToSelectTheme(theme: String) {}
}

@Destination(start = true)
@Composable
fun SettingsScreen(
    navigator: SettingsNavigator,
    resultRecipient: ResultRecipient<ThemeSelectorScreenDestination, String>,
) {
    val scrollState = rememberScrollState()
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by rememberFlowWithLifecycle(viewModel.state).collectAsState(SettingsState.Empty)

    resultRecipient.onNavResult {
        it.getOr { null }?.let { theme ->
            viewModel.setTheme(Theme.fromString(theme))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AtomTopBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding(),
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

            DateTimeSection()
        }
    }
}

@Composable
private fun SectionSpacer() {
    Spacer(Modifier.height(AppTheme.dimens.spacingLarge))
}

@Composable
private fun DateTimeSection() {
    // TODO
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AtomRemindersTheme {
        SettingsScreen(
            EmptySettingsNavigator,
            EmptyResultRecipient(),
        )
    }
}