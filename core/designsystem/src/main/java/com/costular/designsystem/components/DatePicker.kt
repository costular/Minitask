package com.costular.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.utils.ofLocalized
import com.costular.atomtasks.core.ui.utils.rememberFirstDayOfWeek
import com.costular.atomtasks.core.util.DateTimeFormatters
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val MonthsToShow = 100L

@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    selectedDay: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
) {
    val currentMonth = remember(selectedDay) { selectedDay.yearMonth }
    val startMonth = remember { currentMonth.minusMonths(MonthsToShow) }
    val endMonth = remember { currentMonth.plusMonths(MonthsToShow) }
    val firstDayOfWeek = rememberFirstDayOfWeek()
    val coroutineScope = rememberCoroutineScope()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )
    val visibleMonth = rememberFirstCompletelyVisibleMonth(state)

    Column(
        modifier = modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutLinearInEasing,
            )
        ),
    ) {
        MonthNameWithNavigation(
            currentMonth = visibleMonth.yearMonth,
            onPreviousMonth = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                }
            },
            onNextMonth = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                }
            }
        )

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = selectedDay == day.date,
                    onClick = { onDateSelected(it.date) },
                )
            },
            monthHeader = { month ->
                Weekdays(
                    month = month,
                )
            },
        )
    }
}

@Composable
private fun MonthNameWithNavigation(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = null,
            )
        }

        Text(
            text = currentMonth.ofLocalized(DateTimeFormatters.monthFormatter),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun Weekdays(
    month: CalendarMonth,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val mediumEmphasis = MaterialTheme.colorScheme.onSurfaceVariant

        month.weekDays.first().map { it.date.dayOfWeek }.forEach { dayOfWeek ->
            CompositionLocalProvider(LocalContentColor provides mediumEmphasis) {
                Text(
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.ofLocalized(TextStyle.SHORT),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun BoxScope.Day(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: (CalendarDay) -> Unit,
) {
    val isToday = day.date == LocalDate.now()

    val surfaceElevation = LocalAbsoluteTonalElevation.current.value.dp
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(surfaceElevation)
    }
    val borderColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = contentColorFor(backgroundColor)
    val contentAlpha =
        if (day.position == DayPosition.MonthDate) 1f else AppTheme.DisabledAlpha

    Box(
        modifier = Modifier
            .padding(AppTheme.dimens.spacingSmall)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape = CircleShape)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor.copy(alpha = contentAlpha),
        )
    }
}

@Composable
private fun rememberFirstCompletelyVisibleMonth(state: CalendarState): CalendarMonth {
    val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
    // Only take non-null values as null will be produced when the
    // list is mid-scroll as no index will be completely visible.
    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo.completelyVisibleMonths.firstOrNull() }
            .filterNotNull()
            .collect { month -> visibleMonth.value = month }
    }
    return visibleMonth.value
}

private val CalendarLayoutInfo.completelyVisibleMonths: List<CalendarMonth>
    get() {
        val visibleItemsInfo = this.visibleMonthsInfo.toMutableList()
        return if (visibleItemsInfo.isEmpty()) {
            emptyList()
        } else {
            val lastItem = visibleItemsInfo.last()
            val viewportSize = this.viewportEndOffset + this.viewportStartOffset
            if (lastItem.offset + lastItem.size > viewportSize) {
                visibleItemsInfo.removeAt(visibleItemsInfo.lastIndex)
            }
            val firstItem = visibleItemsInfo.firstOrNull()
            if (firstItem != null && firstItem.offset < this.viewportStartOffset) {
                visibleItemsInfo.removeAt(0)
            }
            visibleItemsInfo.map { it.month }
        }
    }

@Preview(showBackground = true)
@Composable
fun DatePickerPreview() {
    AtomTheme {
        DatePicker(
            onDateSelected = {},
        )
    }
}
