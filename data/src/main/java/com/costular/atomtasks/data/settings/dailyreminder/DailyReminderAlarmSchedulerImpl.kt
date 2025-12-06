package com.costular.atomtasks.data.settings.dailyreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.S
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class DailyReminderAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DailyReminderAlarmScheduler {

    private val alarmManager by lazy {
        context.getSystemService<AlarmManager>()
    }

    override fun schedule(localDateTime: LocalDateTime) {
        checkNotNull(alarmManager)

        if (localDateTime.isBefore(LocalDateTime.now())) {
            return
        }

        if (Build.VERSION.SDK_INT >= S && !alarmManager!!.canScheduleExactAlarms()) {
            return
        }

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager!!,
            AlarmManager.RTC_WAKEUP,
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            buildDailyReminderPendingIntent()
        )
    }

    override fun remove() {
        alarmManager?.let { alarmManager ->
            buildDailyReminderPendingIntent().also {
                alarmManager.cancel(it)
            }
        }
    }

    private fun buildDailyReminderPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            Intent(context, DailyReminderReceiver::class.java),
            Flags,
        )

    companion object {
        const val DAILY_REMINDER_REQUEST_CODE = 400
        private val Flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }
}
