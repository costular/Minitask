package com.costular.atomtasks.core.ui.tasks

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.costular.atomtasks.core.ui.utils.DateUtils
import com.costular.designsystem.theme.AppTheme
import java.time.LocalDate

@Composable
fun TaskDayHeader(day: LocalDate, modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text(
            text = DateUtils.dayAsText(day),
            modifier = Modifier
                .padding(AppTheme.dimens.contentMargin)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
