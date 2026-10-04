package com.costular.atomtasks.core.ui.tasks.actions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TaskActionsResult(open val taskId: Long) : Parcelable {

    @Parcelize
    data class MarkAsDone(override val taskId: Long) : TaskActionsResult(taskId), Parcelable

    @Parcelize
    data class MarkAsNotDone(override val taskId: Long) : TaskActionsResult(taskId), Parcelable

    @Parcelize
    data class Edit(override val taskId: Long) : TaskActionsResult(taskId), Parcelable

    @Parcelize
    data class Remove(override val taskId: Long) : TaskActionsResult(taskId), Parcelable
}
