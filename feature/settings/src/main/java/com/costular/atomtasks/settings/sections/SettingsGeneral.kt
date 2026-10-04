package com.costular.atomtasks.settings.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.data.settings.Theme
import com.costular.atomtasks.data.settings.DefaultTab
import com.costular.atomtasks.settings.defaultTabLabel
import com.costular.atomtasks.settings.SettingSection
import com.costular.atomtasks.settings.components.SettingOption
import com.costular.designsystem.theme.AtomTheme

@Composable
internal fun GeneralSection(
    theme: Theme,
    defaultTab: DefaultTab,
    onSelectTheme: () -> Unit,
    onSelectDefaultTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingSection(
        stringResource(R.string.settings_general_title),
        modifier = modifier,
    ) {
        SettingOption(
            title = stringResource(R.string.settings_theme_title),
            option = parseTheme(theme),
            icon = Icons.Outlined.Palette,
            onClick = onSelectTheme,
        )
        SettingOption(
            title = stringResource(R.string.settings_default_tab_title),
            option = defaultTabLabel(defaultTab),
            icon = Icons.Outlined.Home,
            onClick = onSelectDefaultTab,
        )
    }
}

@Composable
internal fun parseTheme(theme: Theme): String = when (theme) {
    is Theme.Light -> stringResource(R.string.settings_theme_light)
    is Theme.Dark -> stringResource(R.string.settings_theme_dark)
    is Theme.System -> stringResource(R.string.settings_theme_system)
}

@Preview
@Composable
fun GeneralSectionPreview() {
    AtomTheme {
        GeneralSection(
            theme = Theme.Light,
            defaultTab = DefaultTab.Agenda,
            onSelectTheme = {},
            onSelectDefaultTab = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
