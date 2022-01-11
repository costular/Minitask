package com.costular.atomtasks.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.costular.atomtasks.R
import com.costular.atomtasks.ui.components.ActionItem
import com.costular.atomtasks.ui.theme.AlphaDivider
import com.costular.atomtasks.ui.theme.AppTheme

@Composable
fun TaskActionDialog(
    taskName: String?,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.dimens.contentMargin)
            ) {
                Spacer(Modifier.height(AppTheme.dimens.spacingLarge))

                Text(
                    text = taskName ?: "",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.dimens.contentMargin),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppTheme.dimens.spacingLarge)
                        .padding(bottom = AppTheme.dimens.spacingSmall),
                    color = MaterialTheme.colors.onSurface.copy(alpha = AlphaDivider),
                )

                ActionItem(
                    icon = Icons.Outlined.Delete,
                    text = stringResource(R.string.agenta_delete_task),
                    onClick = onDelete
                )
            }
        }
    }
}
