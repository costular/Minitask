package com.costular.atomtasks.verticaltasks

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.costular.atomtasks.core.ui.R
import com.costular.designsystem.theme.AppTheme

@Composable
internal fun PastTasksHeader(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text(
            text = stringResource(R.string.vertical_past),
            modifier = Modifier
                .padding(AppTheme.dimens.contentMargin)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
internal fun LoadOlderTasksButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(stringResource(R.string.vertical_load_older))
    }
}

@Composable
internal fun EmptyDayRow(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.vertical_empty_day),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
