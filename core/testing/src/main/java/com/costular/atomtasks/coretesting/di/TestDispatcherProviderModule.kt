package com.costular.atomtasks.core.testing.di

import com.costular.atomtasks.core.testing.net.TestDispatcherProvider
import com.costular.core.di.DispatcherProviderModule
import com.costular.core.net.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.test.TestDispatcher

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatcherProviderModule::class],
)
object TestDispatcherProviderModule {
    @Provides
    fun provideTestDispatcherProvider(testDispatcher: TestDispatcher): DispatcherProvider =
        TestDispatcherProvider(testDispatcher)
}