package com.costular.commonui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.costular.commonui.R
import com.costular.commonui.theme.AppTheme
import com.costular.commonui.theme.AtomRemindersTheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClearableChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    InputChip(
        modifier = modifier,
        onClick = onClick,
        selected = isSelected,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppTheme.ChipIconSize),
            )
        },
        label = {
            Text(
                text = title,
            )
        },
        trailingIcon = {
            if (isSelected) {
                Spacer(Modifier.width(AppTheme.dimens.spacingMedium))

                CompositionLocalProvider(
                    LocalMinimumTouchTargetEnforcement provides false,
                ) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(AppTheme.ChipIconSize),
                    ) {
                        Image(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                R.string.content_description_chip_clear,
                            ),
                            modifier = Modifier
                                .size(AppTheme.ChipIconSize),
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
fun RemovableChipPreview() {
    AtomRemindersTheme {
        ClearableChip(
            title = "1 May 2022",
            icon = Icons.Default.CalendarToday,
            isSelected = false,
            onClick = {},
            onClear = {},
        )
    }
}

@Preview
@Composable
fun RemovableChipSelectedPreview() {
    AtomRemindersTheme {
        ClearableChip(
            title = "1 May 2022",
            icon = Icons.Default.CalendarToday,
            isSelected = true,
            onClick = {},
            onClear = {},
        )
    }
}
