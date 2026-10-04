package com.costular.atomtasks.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.data.settings.DefaultTab

@Composable
internal fun DefaultTabSelectorDialog(
    selectedTab: DefaultTab,
    onSelectTab: (DefaultTab) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_default_tab_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                Text(stringResource(R.string.settings_default_tab_description))
                DefaultTab.entries.forEach { tab ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tab == selectedTab,
                                role = Role.RadioButton,
                                onClick = { onSelectTab(tab) },
                            )
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = tab == selectedTab, onClick = null)
                        Text(defaultTabLabel(tab))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
@ReadOnlyComposable
internal fun defaultTabLabel(tab: DefaultTab): String = stringResource(
    when (tab) {
        DefaultTab.Agenda -> R.string.home_menu_agenda
        DefaultTab.VerticalTasks -> R.string.vertical_tasks
    },
)
