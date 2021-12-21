package com.costular.atomreminders.ui.features.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.costular.atomreminders.ui.components.HorizontalCalendar
import java.time.LocalDate
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.costular.atomreminders.R
import com.costular.atomreminders.domain.Async
import com.costular.atomreminders.domain.model.Task
import com.costular.atomreminders.ui.components.HabitList
import com.costular.atomreminders.ui.components.ScreenHeader
import com.costular.atomreminders.ui.theme.AppTheme
import com.costular.atomreminders.ui.util.DateUtils.dayAsText
import com.costular.atomreminders.ui.util.rememberFlowWithLifecycle

@Composable
fun Agenda(
    onCreateTask: (LocalDate?) -> Unit,
) {
    val viewModel: AgendaViewModel = hiltViewModel()
    val state by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = AgendaState())

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(stringResource(R.string.agenda_create_task))
                },
                onClick = { onCreateTask(state.selectedDay) },
                icon = {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                },
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedDayText = dayAsText(state.selectedDay)
                ScreenHeader(
                    text = selectedDayText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            vertical = AppTheme.dimens.spacingXLarge,
                            horizontal = AppTheme.dimens.spacingLarge
                        )
                        .clickable {
                            // TODO: 26/6/21 open calendar
                        }
                )

                IconButton(
                    enabled = state.isPreviousDaySelected,
                    onClick = {
                        val newDay = state.selectedDay.minusDays(1)
                        viewModel.setSelectedDay(newDay)
                    },
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                }
                IconButton(
                    enabled = state.isNextDaySelected,
                    onClick = {
                        val newDay = state.selectedDay.plusDays(1)
                        viewModel.setSelectedDay(newDay)
                    },
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            HorizontalCalendar(
                from = state.calendarFromDate,
                until = state.calendarUntilDate,
                modifier = Modifier.padding(bottom = AppTheme.dimens.spacingXLarge),
                selectedDay = state.selectedDay,
                onSelectDay = {
                    viewModel.setSelectedDay(it)
                }
            )

            when (val tasks = state.tasks) {
                is Async.Success -> {
                    HabitList(
                        tasks = tasks.data,
                        onClick = {
                            // TODO()
                        },
                        onMarkHabit = { id, isMarked -> viewModel.onMarkTask(id, isMarked) },
                        modifier = Modifier.fillMaxSize(),
                        date = state.selectedDay
                    )
                }
            }
        }
    }
}