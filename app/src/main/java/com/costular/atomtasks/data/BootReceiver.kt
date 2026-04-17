package com.costular.atomtasks.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.costular.atomtasks.tasks.worker.RestoreMissedTaskRemindersWorker
import com.costular.atomtasks.tasks.worker.SetTasksRemindersWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recurrenceScheduler: RecurrenceScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(RestoreMissedTaskRemindersWorker.start())
            workManager.enqueue(SetTasksRemindersWorker.start())
            recurrenceScheduler.initialize()
        }
    }
}
