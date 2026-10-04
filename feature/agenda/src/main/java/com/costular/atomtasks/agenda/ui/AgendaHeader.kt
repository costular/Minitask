package com.costular.atomtasks.agenda.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.date.Day
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.core.ui.utils.DateUtils
import com.costular.designsystem.components.ScreenHeader
import com.costular.designsystem.components.WeekCalendar
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.costular.designsystem.util.supportWideScreen
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import kotlinx.coroutines.launch
import com.costular.atomtasks.core.ui.R.string as S

private const val DaysToShow = 365L

@Suppress("LongMethod")
@Composable
internal fun AgendaHeader(
    modifier: Modifier = Modifier,
    selectedDay: Day,
    shouldShowTodayAction: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    onClickCalendar: () -> Unit,
    onSearch: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val startDate = remember(selectedDay) { selectedDay.date.minusDays(DaysToShow) }
    val endDate = remember(selectedDay) { selectedDay.date.plusDays(DaysToShow) }

    val weekCalendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = selectedDay.date,
    )

    Surface(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val selectedDayText = DateUtils.dayAsText(selectedDay.date)

                ScreenHeader(
                    text = selectedDayText,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(TestTagHeader)
                        .padding(
                            top = AppTheme.dimens.spacingLarge,
                            bottom = AppTheme.dimens.spacingLarge,
                            start = AppTheme.dimens.spacingLarge,
                        ),
                )

                AnimatedVisibility(visible = shouldShowTodayAction) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            weekCalendarState.animateScrollToWeek(LocalDate.now())
                        }
                        onSelectToday()
                    }) {
                        Text(
                            text = stringResource(S.today),
                            style = MaterialTheme.typography.titleMedium
                                .copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }

                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, stringResource(R.string.search_tasks))
                }

                IconButton(
                    onClick = onClickCalendar,
                    modifier = Modifier
                        .padding(end = AppTheme.dimens.spacingLarge)
                        .width(40.dp)
                        .height(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(R.string.home_menu_calendar),
                    )
                }
            }

            WeekCalendar(
                selectedDay,
                onSelectDate,
                weekCalendarState,
            )
        }
    }
}

@Composable
private fun WeekCalendar(
    selectedDay: Day,
    onSelectDate: (LocalDate) -> Unit,
    weekCalendarState: WeekCalendarState = rememberWeekCalendarState(),
) {
    WeekCalendar(
        modifier = Modifier
            .supportWideScreen()
            .padding(
                start = AppTheme.dimens.spacingLarge,
                end = AppTheme.dimens.spacingLarge,
                bottom = AppTheme.dimens.spacingSmall,
            ),
        selectedDay = selectedDay,
        onSelectDay = onSelectDate,
        weekCalendarState = weekCalendarState,
    )
}

@Preview
@Composable
private fun HeaderCollapsedPreview() {
    AtomTheme {
        AgendaHeader(
            selectedDay = LocalDate.now().asDay(),
            shouldShowTodayAction = true,
            onSelectDate = {},
            onSelectToday = {},
            modifier = Modifier.fillMaxWidth(),
            onClickCalendar = {},
        )
    }
}
