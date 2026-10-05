package com.costular.atomtasks.agenda.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.date.Day
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.core.ui.utils.DateUtils
import com.costular.designsystem.components.DatePicker
import com.costular.designsystem.components.ScreenHeader
import com.costular.designsystem.components.WeekCalendar
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.theme.AtomTheme
import com.costular.designsystem.util.supportWideScreen
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import com.costular.atomtasks.core.ui.R.string as S

private const val DaysToShow = 365L
private const val HeaderAnimationMillis = 200
private const val ExpandedChevronRotation = 180f
internal const val TestTagWeekCalendar = "AgendaWeekCalendar"
internal const val TestTagMonthCalendar = "AgendaMonthCalendar"

@Composable
internal fun AgendaHeader(
    selectedDay: Day,
    isExpanded: Boolean,
    onToggleHeader: () -> Unit,
    shouldShowTodayAction: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
    val startDate = remember(selectedDay) { selectedDay.date.minusDays(DaysToShow) }
    val endDate = remember(selectedDay) { selectedDay.date.plusDays(DaysToShow) }

    val weekCalendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = selectedDay.date,
    )

    Surface(modifier = modifier) {
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(HeaderAnimationMillis))) {
            AgendaHeaderToolbar(
                selectedDay = selectedDay,
                isExpanded = isExpanded,
                onToggleHeader = onToggleHeader,
                shouldShowTodayAction = shouldShowTodayAction,
                onSelectToday = onSelectToday,
                onSearch = onSearch,
                modifier = Modifier.fillMaxWidth(),
            )
            AgendaCalendar(
                selectedDay = selectedDay,
                isExpanded = isExpanded,
                weekCalendarState = weekCalendarState,
                onSelectDate = onSelectDate,
            )
        }
    }
}

@Composable
private fun AgendaHeaderToolbar(
    selectedDay: Day,
    isExpanded: Boolean,
    onToggleHeader: () -> Unit,
    shouldShowTodayAction: Boolean,
    onSelectToday: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AgendaDateToggle(
            selectedDay = selectedDay,
            isExpanded = isExpanded,
            onToggleHeader = onToggleHeader,
            modifier = Modifier.weight(1f),
        )

        AnimatedVisibility(visible = shouldShowTodayAction) {
            TextButton(onClick = onSelectToday) {
                Text(
                    text = stringResource(S.today),
                    style = MaterialTheme.typography.titleMedium
                        .copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }
        }

        IconButton(
            onClick = onSearch,
            modifier = Modifier.padding(end = AppTheme.dimens.spacingLarge),
        ) {
            Icon(Icons.Outlined.Search, stringResource(R.string.search_tasks))
        }
    }
}

@Composable
private fun AgendaDateToggle(
    selectedDay: Day,
    isExpanded: Boolean,
    onToggleHeader: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleLabel = stringResource(
        if (isExpanded) R.string.agenda_collapse_calendar else R.string.agenda_expand_calendar,
    )
    val expansionDescription = stringResource(
        if (isExpanded) R.string.agenda_calendar_expanded else R.string.agenda_calendar_collapsed,
    )
    val chevronRotation = animateFloatAsState(
        targetValue = if (isExpanded) ExpandedChevronRotation else 0f,
        animationSpec = tween(HeaderAnimationMillis),
        label = "Agenda calendar chevron",
    )

    Row(
        modifier = modifier
            .testTag(TestTagHeader)
            .clickable(
                role = Role.Button,
                onClickLabel = toggleLabel,
                onClick = onToggleHeader,
            )
            .semantics { stateDescription = expansionDescription }
            .padding(
                top = AppTheme.dimens.spacingLarge,
                bottom = AppTheme.dimens.spacingLarge,
                start = AppTheme.dimens.spacingLarge,
                end = AppTheme.dimens.spacingSmall,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenHeader(
            text = DateUtils.dayAsText(selectedDay.date),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = chevronRotation.value },
        )
    }
}

@Composable
private fun AgendaCalendar(
    selectedDay: Day,
    isExpanded: Boolean,
    weekCalendarState: WeekCalendarState,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isExpanded) {
        key(selectedDay.date) {
            DatePicker(
                selectedDay = selectedDay.date,
                onDateSelected = onSelectDate,
                dayContent = { day, isSelected, onClick ->
                    AgendaCalendarDay(
                        day = day,
                        isSelected = isSelected,
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                    )
                },
                modifier = modifier
                    .supportWideScreen(max = 420.dp)
                    .padding(
                        start = AppTheme.dimens.spacingLarge,
                        end = AppTheme.dimens.spacingLarge,
                        bottom = AppTheme.dimens.spacingSmall,
                    )
                    .testTag(TestTagMonthCalendar),
            )
        }
    } else {
        WeekCalendar(
            selectedDay = selectedDay,
            onSelectDay = onSelectDate,
            weekCalendarState = weekCalendarState,
            modifier = modifier
                .supportWideScreen()
                .padding(
                    start = AppTheme.dimens.spacingLarge,
                    end = AppTheme.dimens.spacingLarge,
                    bottom = AppTheme.dimens.spacingSmall,
                )
                .testTag(TestTagWeekCalendar),
        )
    }
}

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
