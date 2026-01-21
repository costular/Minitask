package com.costular.atomtasks.review.strategy

import com.costular.atomtasks.core.logging.atomLog
import com.costular.atomtasks.data.tasks.TasksDao
import javax.inject.Inject

class ReviewStrategyImpl @Inject constructor(
    private val tasksDao: TasksDao,
) : ReviewStrategy {
    override suspend fun shouldShowReview(): Boolean {
        val doneTasksCount = tasksDao.getDoneTasksCount()
        atomLog { "ReviewStrategy: doneTasksCount=$doneTasksCount, threshold=$MINIMUM_DONE_TASKS" }
        return doneTasksCount >= MINIMUM_DONE_TASKS
    }

    private companion object {
        const val MINIMUM_DONE_TASKS = 10
    }
}
