package com.costular.atomtasks.agenda.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.designsystem.theme.AtomTheme
import java.time.LocalDate

@Preview(widthDp = 360)
@Composable
private fun HeaderCollapsedPreview() {
    HeaderPreview(isExpanded = false, modifier = Modifier.fillMaxWidth())
}

@Preview(name = "Monthly", widthDp = 360)
@Preview(name = "Monthly dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Monthly narrow", widthDp = 320)
@Preview(name = "Monthly large font", widthDp = 360, fontScale = 1.5f)
@Preview(name = "Monthly tablet", widthDp = 840)
@Composable
private fun HeaderExpandedPreview() {
    HeaderPreview(isExpanded = true, modifier = Modifier.fillMaxWidth())
}

@Composable
@Suppress("MagicNumber")
private fun HeaderPreview(isExpanded: Boolean, modifier: Modifier = Modifier) {
    AtomTheme {
        AgendaHeader(
            selectedDay = LocalDate.of(2026, 8, 15).asDay(),
            isExpanded = isExpanded,
            onToggleHeader = {},
            shouldShowTodayAction = true,
            onSelectDate = {},
            onSelectToday = {},
            modifier = modifier,
        )
    }
}
