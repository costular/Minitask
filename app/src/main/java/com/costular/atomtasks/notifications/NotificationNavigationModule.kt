package com.costular.atomtasks.notifications

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NotificationNavigationModule {

    @Binds
    fun bindNotificationNavigationIntentFactory(
        factory: DefaultNotificationNavigationIntentFactory,
    ): NotificationNavigationIntentFactory
}
