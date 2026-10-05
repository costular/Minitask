package com.costular.atomtasks.agenda.ui

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
import androidx.compose.ui.unit.dp
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.date.Day
import com.costular.atomtasks.core.ui.utils.DateUtils
import com.costular.designsystem.components.DatePicker
import com.costular.designsystem.components.ScreenHeader
import com.costular.designsystem.components.WeekCalendar
import com.costular.designsystem.theme.AppTheme
import com.costular.designsystem.util.supportWideScreen
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import com.costular.atomtasks.core.ui.R.string as S

private const val DaysToShow = 365L
private const val HeaderAnimationMillis = 200
private const val ExpandedChevronRotation = 180f
internal const val TestTagWeekCalendar = "AgendaWeekCalendar"
internal const val TestTagMonthCalendar = "AgendaMonthCalendar"

@Suppress("LongMethod")
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

    Surface(modifier = modifier) {
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(HeaderAnimationMillis))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val selectedDayText = DateUtils.dayAsText(selectedDay.date)

                Row(
                    modifier = Modifier
                        .weight(1f)
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
                        text = selectedDayText,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = chevronRotation.value },
                    )
                }

                AnimatedVisibility(visible = shouldShowTodayAction) {
                    TextButton(onClick = onSelectToday) {
                        Text(
                            text = stringResource(S.today),
                            style = MaterialTheme.typography.titleMedium
                                .copy(color = MaterialTheme.colorScheme.onSurface)
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
                        modifier = Modifier
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
                    modifier = Modifier
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
    }
}
