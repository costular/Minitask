package com.costular.atomtasks.notifications

import java.time.LocalDateTime

interface TaskNotificationManager {
    fun remindTask(taskId: Long, taskName: String, reminderDateTime: LocalDateTime)
    fun removeTaskNotification(taskId: Long)
}
