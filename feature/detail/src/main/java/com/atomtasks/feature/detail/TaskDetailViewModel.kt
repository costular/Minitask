package com.atomtasks.feature.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.atomtasks.feature.detail.EditRecurringTaskResponse.THIS
import com.atomtasks.feature.detail.EditRecurringTaskResponse.THIS_AND_FUTURE_ONES
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.tasks.model.RecurrenceType
import com.costular.atomtasks.tasks.model.RecurringUpdateStrategy
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.removal.RemoveTaskConfirmationUiState
import com.costular.atomtasks.tasks.removal.RemoveTaskUseCase
import com.costular.atomtasks.tasks.usecase.AreExactRemindersAvailable
import com.costular.atomtasks.tasks.usecase.CreateTaskUseCase
import com.costular.atomtasks.tasks.usecase.EditTaskUseCase
import com.costular.atomtasks.tasks.usecase.GetTaskByIdUseCase
import com.costular.atomtasks.tasks.usecase.UpdateTaskIsDoneUseCase
import com.ramcosta.composedestinations.generated.detail.destinations.TaskDetailScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val areExactRemindersAvailable: AreExactRemindersAvailable,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val editTaskUseCase: EditTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val atomAnalytics: AtomAnalytics,
    private val updateTaskIsDoneUseCase: UpdateTaskIsDoneUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
) : MviViewModel<TaskDetailUiState>(TaskDetailUiState()) {

    private val navArgs: TaskDetailNavArgs = TaskDetailScreenDestination.argsFrom(savedStateHandle)

    init {
        checkIfExactRemindersAreAvailable()

        if (navArgs.taskId != null) {
            loadTask(navArgs.taskId)
        } else {
            setState {
                copy(
                    initialTaskState = initialTaskState.copy(date = requireNotNull(navArgs.defaultDate)),
                    taskState = taskState.copy(date = requireNotNull(navArgs.defaultDate)),
                )
            }
        }
    }

    private fun loadTask(taskId: Long) {
        viewModelScope.launch {
            getTaskByIdUseCase(GetTaskByIdUseCase.Params(taskId))
                .collectLatest {
                    it?.let { task ->
                        val taskState = TaskState(
                            name = TextFieldState(task.name),
                            date = task.day,
                            reminder = task.reminder?.time,
                            recurrenceType = task.recurrenceType,
                        )

                        setState {
                            copy(
                                initialTaskState = taskState,
                                taskState = taskState,
                                isEditMode = true,
                                isDone = task.isDone,
                            )
                        }
                    }
                }
        }
    }

    private fun checkIfExactRemindersAreAvailable() {
        viewModelScope.launch {
            val areExactRemindersEnabled = areExactRemindersAvailable.invoke(Unit)
            setState {
                copy(shouldShowExactAlarmRationale = !areExactRemindersEnabled)
            }
        }
    }

    fun onDelete() {
        val taskId = navArgs.taskId

        if (!state.value.isEditMode || taskId == null) {
            return
        }

        setState {
            copy(
                removeTaskConfirmationUiState = RemoveTaskConfirmationUiState.Shown(
                    taskId = taskId,
                    this.initialTaskState.recurrenceType != null,
                )
            )
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            removeTaskUseCase(RemoveTaskUseCase.Params(taskId))
            sendEvent(TaskDetailUiEvent.Close)
        }
    }

    fun deleteRecurringTask(id: Long, recurringRemovalStrategy: RecurringRemovalStrategy) {
        viewModelScope.launch {
            removeTaskUseCase(RemoveTaskUseCase.Params(id, recurringRemovalStrategy))
            sendEvent(TaskDetailUiEvent.Close)
        }
    }

    fun dismissDeleteConfirmation() {
        setState { copy(removeTaskConfirmationUiState = RemoveTaskConfirmationUiState.Hidden) }
    }

    fun onDateChanged(localDate: LocalDate) {
        setState {
            copy(
                taskState = taskState.copy(
                    date = localDate,
                ),
                showSetDate = false
            )
        }
    }

    fun onReminderChanged(localTime: LocalTime?) {
        setState {
            copy(
                taskState = taskState.copy(
                    reminder = localTime,
                ),
                showSetReminder = false,
            )
        }
    }

    fun onSelectDateClicked() {
        atomAnalytics.track(DetailAnalytics.DatePickerOpened)
        setState {
            copy(showSetDate = true)
        }
    }

    fun closeSelectDate() {
        setState {
            copy(showSetDate = false)
        }
    }

    fun onSelectReminderClicked() {
        atomAnalytics.track(DetailAnalytics.ReminderPickerOpened)
        setState {
            copy(showSetReminder = true)
        }
    }

    fun closeSelectReminder() {
        setState {
            copy(showSetReminder = false)
        }
    }

    fun onReminderRemoved() {
        setState {
            copy(
                taskState = taskState.copy(
                    reminder = null
                )
            )
        }
    }

    fun onSelectRecurrenceClicked() {
        atomAnalytics.track(DetailAnalytics.RecurrencePickerOpened)
        setState { copy(showSetRecurrence = true) }
    }

    fun closeSelectRecurrence() {
        setState { copy(showSetRecurrence = false) }
    }

    fun onRecurrenceChanged(
        recurrenceType: RecurrenceType?
    ) {
        setState {
            copy(
                taskState = taskState.copy(recurrenceType = recurrenceType),
                showSetRecurrence = false
            )
        }
    }

    fun clearRecurrence() {
        setState {
            copy(
                taskState = taskState.copy(recurrenceType = null),
                showSetRecurrence = false
            )
        }
    }

    fun onMarkTask(isDone: Boolean) {
        viewModelScope.launch {
            val taskId = navArgs.taskId ?: return@launch

            if (state.value.isEditMode) {
                updateTaskIsDoneUseCase.invoke(
                    UpdateTaskIsDoneUseCase.Params(
                        taskId = taskId,
                        isDone = isDone,
                    )
                ).tap {
                    setState { copy(isDone = isDone) }
                }
            } else {
                setState { copy(isDone = isDone) }
            }
        }
    }

    fun save() {
        setState {
            copy(isSaving = true)
        }

        if (state.value.isEditMode) {
            editTask(recurringUpdateStrategy = null)
        } else {
            createTask()
        }
    }

    fun navigateToExactAlarmSettings() {
        sendEvent(TaskDetailUiEvent.NavigateToExactAlarmSettings)
    }

    fun exactAlarmSettingChanged() {
        checkIfExactRemindersAreAvailable()
    }

    fun cancelRecurringEdition() {
        setState { copy(taskToSave = null) }
    }

    fun confirmRecurringEdition(response: EditRecurringTaskResponse) {
        val taskToSave = state.value.taskToSave
        requireNotNull(taskToSave) {
            "taskToSave must not be null"
        }

        val recurringUpdateStrategy = when (response) {
            THIS -> RecurringUpdateStrategy.SINGLE
            THIS_AND_FUTURE_ONES -> RecurringUpdateStrategy.SINGLE_AND_FUTURE
        }

        editTask(
            recurringUpdateStrategy = recurringUpdateStrategy,
        )
    }

    @Suppress("ForbiddenComment")
    private fun createTask() {
        viewModelScope.launch {
            val currentTaskState = state.value.taskState

            createTaskUseCase(
                CreateTaskUseCase.Params(
                    name = currentTaskState.name.text.toString(),
                    date = currentTaskState.date,
                    reminderEnabled = currentTaskState.reminder != null,
                    reminderTime = currentTaskState.reminder,
                    recurrenceType = currentTaskState.recurrenceType,
                ),
            ).fold(
                ifError = {
                    // TODO: Handle error
                },
                ifResult = {
                    atomAnalytics.track(DetailAnalytics.TaskCreated)
                    sendEvent(TaskDetailUiEvent.Close)
                }
            )
        }
    }

    fun editTask(
        recurringUpdateStrategy: RecurringUpdateStrategy?,
    ) {
        viewModelScope.launch {
            val state = state.value

            if (state.initialTaskState.recurrenceType != null && recurringUpdateStrategy == null) {
                setState {
                    copy(
                        taskToSave = TaskToSave(
                            name = taskState.name.text.toString(),
                            date = taskState.date,
                            reminder = taskState.reminder,
                            recurrenceType = taskState.recurrenceType,
                        )
                    )
                }
                return@launch
            }

            editTaskUseCase(
                EditTaskUseCase.Params(
                    taskId = requireNotNull(navArgs.taskId),
                    name = state.taskState.name.text.toString(),
                    date = state.taskState.date,
                    reminderTime = state.taskState.reminder,
                    recurrenceType = state.taskState.recurrenceType,
                    recurringUpdateStrategy = recurringUpdateStrategy,
                ),
            ).fold(
                ifError = {
                    // TODO failure
                },
                ifResult = {
                    atomAnalytics.track(DetailAnalytics.TaskEdited)
                    sendEvent(TaskDetailUiEvent.Close)
                }
            )
        }
    }

    fun onClose() {
        if (state.value.taskHasBeenEdited) {
            atomAnalytics.track(DetailAnalytics.DiscardChangesShown)
            setState {
                copy(shouldShowDiscardChangesConfirmation = true)
            }
            return
        }

        atomAnalytics.track(DetailAnalytics.Closed)
        sendEvent(TaskDetailUiEvent.Close)
    }

    fun cancelDiscardChanges() {
        atomAnalytics.track(DetailAnalytics.DiscardChangesCancel)
        setState {
            copy(shouldShowDiscardChangesConfirmation = false)
        }
    }

    fun discardChanges() {
        atomAnalytics.track(DetailAnalytics.DiscardChangesDiscard)
        setState {
            copy(shouldShowDiscardChangesConfirmation = false)
        }
        sendEvent(TaskDetailUiEvent.Close)
    }
}
