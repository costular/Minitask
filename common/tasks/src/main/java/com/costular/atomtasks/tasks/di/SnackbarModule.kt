package com.costular.atomtasks.tasks.di

import com.costular.atomtasks.core.ui.SnackbarController
import com.costular.atomtasks.core.ui.SnackbarManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SnackbarModule {
    @Provides
    fun providesSnackbarManager(): SnackbarManager = SnackbarController
}
