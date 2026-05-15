package com.costular.atomtasks.notifications

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsModule {

    @Binds
    fun bindNotificationResources(
        androidNotificationResources: AndroidNotificationResources,
    ): NotificationResources

    @Binds
    fun bindNotificationNavigationIntentFactory(
        defaultNotificationNavigationIntentFactory: DefaultNotificationNavigationIntentFactory,
    ): NotificationNavigationIntentFactory

    @Binds
    fun bindTaskNotificationsManager(
        taskNotificationManagerImpl: TaskNotificationManagerImpl
    ): TaskNotificationManager

    @Binds
    fun bindDailyReminderNotificationManager(
        impl: DailyReminderNotificationManagerImpl
    ): DailyReminderNotificationManager
}
