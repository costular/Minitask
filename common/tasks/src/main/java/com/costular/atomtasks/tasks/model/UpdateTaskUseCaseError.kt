package com.costular.atomtasks.tasks.model

sealed interface UpdateTaskUseCaseError {
    data object UnableToSave : UpdateTaskUseCaseError
    data object NameCannotBeEmpty : UpdateTaskUseCaseError
    data object TaskNotFound : UpdateTaskUseCaseError
    data object UnknownError : UpdateTaskUseCaseError
}
