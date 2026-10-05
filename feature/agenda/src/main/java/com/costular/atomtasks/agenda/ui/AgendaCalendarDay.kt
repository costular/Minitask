package com.costular.atomtasks.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.costular.designsystem.theme.AppTheme
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun AgendaCalendarDay(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInMonth = day.position == DayPosition.MonthDate
    val isToday = day.date == LocalDate.now()
    val colors = MaterialTheme.colorScheme
    val locale = LocalConfiguration.current.locales[0]
    val dateDescription = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    val textColor = if (isSelected) colors.onPrimary else colors.onSurface

    Box(
        modifier = modifier
            .selectable(
                selected = isSelected,
                enabled = isInMonth,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = dateDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isSelected) colors.primary else Color.Transparent, CircleShape)
                .border(1.dp, if (isToday) colors.primary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor.copy(alpha = if (isInMonth) 1f else AppTheme.DisabledAlpha),
            )
        }
    }
}
