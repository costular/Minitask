package com.costular.atomtasks.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.costular.atomtasks.core.ui.mvi.MviViewModel
import com.costular.atomtasks.core.ui.tasks.TaskInteractionStateHolder
import com.costular.atomtasks.tasks.removal.RecurringRemovalStrategy
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.usecase.ObserveSearchTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed interface SearchResults {
    data object Idle : SearchResults
    data object Loading : SearchResults
    data object Failure : SearchResults
    data class Success(val tasks: ImmutableList<Task>) : SearchResults
}

data class SearchState(val query: String = "", val results: SearchResults = SearchResults.Idle)

private fun initialSearchState(savedState: SavedStateHandle): SearchState {
    val query = savedState.get<String>("search_query").orEmpty()
    val results = if (query.trim().length >= ObserveSearchTasksUseCase.MinimumQueryLength) {
        SearchResults.Loading
    } else {
        SearchResults.Idle
    }
    return SearchState(query, results)
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Suppress("TooManyFunctions")
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTasks: ObserveSearchTasksUseCase,
    private val savedState: SavedStateHandle,
    taskInteractionFactory: TaskInteractionStateHolder.Factory,
) : MviViewModel<SearchState>(initialSearchState(savedState)) {
    private val taskInteractions = taskInteractionFactory.create(viewModelScope)
    val taskInteractionState = taskInteractions.state

    fun onMarkTask(taskId: Long, isDone: Boolean) = taskInteractions.onMarkTask(taskId, isDone)
    fun askDelete(taskId: Long) = taskInteractions.askDelete(taskId)
    fun dismissDelete() = taskInteractions.dismissDelete()
    fun deleteTask(taskId: Long) = taskInteractions.deleteTask(taskId)
    fun deleteRecurringTask(taskId: Long, strategy: RecurringRemovalStrategy) =
        taskInteractions.deleteRecurringTask(taskId, strategy)
    fun onReviewFinished() = taskInteractions.onReviewFinished()

    private data class Request(val query: String, val version: Long = 0, val immediate: Boolean = false)

    private val requests = MutableStateFlow(Request(state.value.query.trim()))

    init {
        viewModelScope.launch {
            requests
                .debounce { if (it.immediate || it.query.length < MinimumLength) 0L else DebounceMillis }
                .flatMapLatest { request ->
                    if (request.query.length < MinimumLength) {
                        flowOf(request to SearchResults.Idle)
                    } else {
                        searchTasks(request.query).map { result ->
                            request to result.fold(
                                ifError = { SearchResults.Failure },
                                ifResult = { SearchResults.Success(it.toImmutableList()) },
                            )
                        }.onStart { emit(request to SearchResults.Loading) }
                    }
                }.collect { (request, results) ->
                    if (request == requests.value) setState { copy(results = results) }
                }
        }
    }

    fun onQueryChange(query: String) {
        savedState[QueryKey] = query
        val normalized = query.trim()
        if (normalized == requests.value.query) {
            setState { copy(query = query) }
            return
        }
        setState {
            copy(
                query = query,
                results = if (normalized.length < MinimumLength) SearchResults.Idle else SearchResults.Loading,
            )
        }
        requests.value = Request(normalized, requests.value.version + 1)
    }

    fun retry() {
        setState { copy(results = SearchResults.Loading) }
        requests.value = requests.value.copy(version = requests.value.version + 1, immediate = true)
    }

    companion object {
        private const val QueryKey = "search_query"
        private const val MinimumLength = ObserveSearchTasksUseCase.MinimumQueryLength
        private const val DebounceMillis = 300L
    }
}
