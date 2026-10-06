package com.costular.atomtasks.tasks.usecase

import com.costular.atomtasks.core.Either
import com.costular.atomtasks.core.usecase.ObservableUseCase
import com.costular.atomtasks.tasks.model.ObserveTasksError
import com.costular.atomtasks.tasks.model.Task
import com.costular.atomtasks.tasks.repository.TasksRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ObserveCompletedTasksUseCase @Inject constructor(
    private val repository: TasksRepository,
) : ObservableUseCase<Unit, Either<ObserveTasksError, List<Task>>> {
    override fun invoke(params: Unit): Flow<Either<ObserveTasksError, List<Task>>> = flow {
        repository.observeCompletedTasks().collect { emit(it) }
    }.map<List<Task>, Either<ObserveTasksError, List<Task>>> { Either.Result(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(Either.Error(ObserveTasksError.UnknownError))
        }
}
